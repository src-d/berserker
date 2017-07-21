package extractor

// proteus:generate
type Request struct {
	RepositoryIDs  RepositoryIDs
	RootCommitHash []byte
	Reference      string
}

type RepositoryIDs []string

// proteus:generate
type RepositoryData struct {
	RepositoryID string
	URL          string
	Files        []File
}

type File struct {
	Language string
	Path     string
	UAST     []byte
	Hash     string
}
