package enrysrv

import (
	"time"

	"github.com/sirupsen/logrus"
)

//go:generate proteus -f proto -p github.com/src-d/berserker/enrysrv --verbose

// proteus:generate
type EnryRequest struct {
	FileName    string
	FileContent []byte
}

// proteus:generate
type EnryResponse struct {
	Language string
	Status   Status
}

// Status is the status of a response.
//proteus:generate
type Status byte

const (
	// Ok status code. It is replied when a language is detected.
	Ok Status = iota
	// NeedContent status code. It is replied when the file content is needed to detect the language.
	NeedContent
	// Ignored status code. It is replied when a file is Vendor/DotFile/Documentation/Configuration
	Ignored
	// Error status code. It is replied when the language couldn't be detected.
	Error
)

var statusToString = map[Status]string{
	Ok:          "OK",
	NeedContent: "NEED CONTENT",
	Ignored:     "IGNORED",
	Error:       "ERROR",
}

//proteus:generate
func GetLanguage(req *EnryRequest) (*EnryResponse, error) {
	start := time.Now()
	res := &EnryResponse{}
	var languages []string
	var strategyName string
	defer func() {
		elapsed := time.Since(start)
		logRequest(req.FileName, res, strategyName, elapsed)
	}()

	if isIgnored(req.FileName) {
		res.Status = Ignored
		return res, nil
	}

	languages, strategyName = getLanguagesByFilenameStrategies(req.FileName)
	if len(languages) == 1 {
		res.Language = languages[0]
		res.Status = Ok
		return res, nil
	}

	if req.FileContent == nil {
		res.Status = NeedContent
		return res, nil
	}

	languages, strategyName = getLanguagesByContentStrategies(req.FileName, req.FileContent, languages)
	if len(languages) == 0 {
		res.Status = Error
		return res, nil
	}

	res.Language = languages[0]
	res.Status = Ok
	return res, nil
}

func logRequest(filename string, res *EnryResponse, strategyName string, elapsed time.Duration) {
	const message = "Incoming request: %s %s --- filename: %q, language: %q, strategy: %s"
	logrus.Infof(message, statusToString[res.Status], elapsed, filename, res.Language, strategyName)
}
