package extractor

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/bblfsh/sdk/protocol"
	"google.golang.org/grpc"

	"gopkg.in/src-d/go-git.v4"                 // git.Open
	"gopkg.in/src-d/go-git.v4/plumbing"        // Hash, Repository
	"gopkg.in/src-d/go-git.v4/plumbing/object" // object.File
	"gopkg.in/src-d/go-git.v4/storage"

	"gopkg.in/src-d/core-retrieval.v0" // core_retrieval.RootTransactioner
	"gopkg.in/src-d/core.v0"           // core.ModelRepositoryStore
	"gopkg.in/src-d/core.v0/model"     // model.Repository, model.Reference
	"gopkg.in/src-d/enry.v1"           // lang detection
)

type Service struct {
	bblfshClient protocol.ProtocolServiceClient
}

func NewService() *Service {
	conn, err := grpc.Dial("0.0.0.0:9432", grpc.WithTimeout(time.Second*2), grpc.WithInsecure())
	client := protocol.NewProtocolServiceClient(conn)
	checkIfError(err)

	return &Service{bblfshClient: client}
}

//proteus:generate
func (s *Service) GetRepositoryData(r *Request) (*RepositoryData, error) {
	// TODO
	return nil, fmt.Errorf("NOT IMPLEMENTED YET")
}

//proteus:generate
func (s *Service) GetRepositoriesData() ([]*RepositoryData, error) {
	n, err := core.ModelRepositoryStore().Count(model.NewRepositoryQuery().FindByStatus(model.Fetched))
	checkIfError(err)
	fmt.Printf("Iterating over %d 'fetched' repositories in DB:\n", n)

	master := "refs/heads/master"
	result := make([]*RepositoryData, n)

	reposNum := 0
	totalFilesNum := 0
	for masterRefInit, repoID := range findAllFetchedReposWithRef(master) {
		fmt.Printf("Repo: %v", repoID)
		repo := &RepositoryData{
			RepositoryID: repoID,
			URL:          "", //TODO(bzz): add repo url!
			Files:        make([]File, 100),
		}

		rootedTransactioner := core_retrieval.RootedTransactioner()
		tx, err := rootedTransactioner.Begin(plumbing.Hash(masterRefInit))
		if err != nil {
			fmt.Printf("Failed to begin a tx for repo:%v, hash:%v. %v\n", repoID, masterRefInit, err)
			continue
		}

		tree, err := gitOpenGetTree(tx.Storer(), repoID, masterRefInit, master)
		if err != nil {
			fmt.Printf("Failed to open&get tree for Git repo:%v, hash:%v. %v\n", repoID, masterRefInit, err)
			_ = tx.Rollback()
			continue
		}

		skpFilesNum := 0
		sucFilesNum := 0
		errFilesNum := 0
		err = tree.Files().ForEach(func(f *object.File) error {
			i := (skpFilesNum + sucFilesNum + errFilesNum) % 1000
			batch := (skpFilesNum + sucFilesNum + errFilesNum) / 1000
			if i == 0 && batch != 0 {
				fmt.Printf("\t%d000 files...\n", batch)
			}

			// discard vendoring with enry
			if enry.IsVendor(f.Name) || enry.IsDotFile(f.Name) ||
				enry.IsDocumentation(f.Name) || enry.IsConfiguration(f.Name) {
				skpFilesNum++
				return nil
			} //TODO(bzz): filter binaries like .apk and .jar

			// detect language with enry
			fContent, err := f.Contents()
			if err != nil {
				fmt.Printf("\tFailed to read file %s, %s\n", f.Name, err)
				errFilesNum++
				return nil
			}

			fLang := enry.GetLanguage(f.Name, []byte(fContent))
			if err != nil {
				fmt.Printf("\tFailed to detect language for %s, %s\n", f.Name, err)
				errFilesNum++
				return nil
			}
			//fmt.Printf("\t%-9s blob %s    %s\n", fLang, f.Hash, f.Name)

			// Babelfish -> UAST (Python, Java)
			if strings.EqualFold(fLang, "java") || strings.EqualFold(fLang, "python") {

				//TODO(bzz): reply as protobuf
				uast, err := parseToUast(s.bblfshClient, f.Name, strings.ToLower(fLang), fContent)
				if err != nil {
					errFilesNum++
					return nil
				}

				sucFilesNum++
				file := File{
					Language: fLang,
					Path:     f.Name,
					UAST:     string(*uast),
				}
				repo.Files = append(repo.Files, file)
			}
			return nil
		})
		checkIfError(err)
		result = append(result, repo)

		fmt.Printf("Done. Repository encoded, success:%d, fail:%d, skipped:%d files.\n", sucFilesNum, errFilesNum, skpFilesNum)
		reposNum++
		totalFilesNum = totalFilesNum + sucFilesNum + errFilesNum + skpFilesNum

		tx.Rollback()
	}
	fmt.Printf("Done. Total %d repository, %d files encoded\n", reposNum, totalFilesNum)
	return result, nil
}

func gitOpenGetTree(txStorer storage.Storer, repoID string, masterRefInit model.SHA1, master string) (*object.Tree, error) {
	rr, err := git.Open(txStorer, nil)
	if err != nil {
		return nil, err
	}

	// look for the reference to orig repo `refs/heads/master/<model.Repository.ID>`
	origHeadOfMaster := plumbing.ReferenceName(fmt.Sprintf("%s/%s", master, repoID))
	branch, err := rr.Reference(origHeadOfMaster, false)
	if err != nil {
		return nil, err
	}
	fmt.Printf(", master:%+v\n", branch)

	// retrieve the commit that the reference points to
	commit, err := rr.CommitObject(branch.Hash())
	if err != nil {
		return nil, err
	}

	// iterate over files in that commit
	return commit.Tree()
}

func parseToUast(client protocol.ProtocolServiceClient, fName string, fLang string, fContent string) (*[]byte, error) {
	fName = filepath.Base(fName)
	//fmt.Printf("\t\tParsing %s to UAST in %s\n\n", fName, fLang)

	//TODO(bzz): take care of non-UTF8 things, before sending them
	//  - either encode in utf8
	//  - or convert to base64() and set encoding param
	req := &protocol.ParseUASTRequest{
		Content:  fContent,
		Language: fLang}
	resp, err := client.ParseUAST(context.TODO(), req)
	if err != nil {
		fmt.Printf("\t\tError - ParseUAST failed, error:%v for %s\n", err, fName)
		return nil, err
	} else if resp == nil {
		fmt.Printf("\t\tNo error, but - ParseUAST failed, response is nil\n")
		return nil, err
	} else if resp.Status != protocol.Ok {
		fmt.Printf("\t\tNo error, but - ParseUAST failed:%s, %d errors:%v for %s\n", resp.Status, len(resp.Errors), resp.Errors, fName)
		return nil, errors.New(resp.Errors[0])
	}

	//TODO(bzz): change to Protobuf
	//data, err := resp.UAST.Marshal()
	data, err := json.Marshal(resp.UAST)
	if err != nil {
		fmt.Printf("\t\tError: failed to save UAST to ProtoBuff format for %s: %v\n", fName, err)
		return nil, err
	}
	return &data, nil
}

// Collects all Repository metadata in-memory
func findAllFetchedReposWithRef(refText string) map[model.SHA1]string {
	repoStorage := core.ModelRepositoryStore()
	q := model.NewRepositoryQuery().FindByStatus(model.Fetched)
	rs, err := repoStorage.Find(q)
	if err != nil {
		fmt.Printf("Fained to query to DB %v\n", err)
		return nil
	}

	repos := make(map[model.SHA1]string)
	for rs.Next() { // for each Repository
		repo, err := rs.Get()
		if err != nil {
			fmt.Printf("Failed to retrive next row from DB %v\n", err)
			continue
		}
		var masterRef *model.Reference // find "refs/heads/master".Init
		for _, ref := range repo.References {
			if strings.EqualFold(ref.Name, refText) {
				masterRef = ref
				break
			}
		}
		if masterRef == nil { // skipping repos \wo it
			fmt.Printf("\t  Repo: %v does not have master ref\n", repo.Endpoints)
			continue
		}
		repos[masterRef.Init] = repo.ID.String()
	}
	return repos
}

func checkIfError(err error) {
	if err == nil {
		return
	}

	fmt.Printf("error: %s\n", err)
	os.Exit(1)
}
