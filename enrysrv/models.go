package enrysrv

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
	// Error status code. It is replied when the language couldn't be detected.
	Error
)

//proteus:generate
func GetLanguage(req *EnryRequest) (*EnryResponse, error) {
	res := &EnryResponse{}
	languages := getLanguagesByFilenameStrategies(req.FileName)
	if len(languages) == 1 {
		res.Language = languages[0]
		res.Status = Ok
		return res, nil
	}

	if req.FileContent == nil {
		res.Status = NeedContent
		return res, nil
	}

	languages = getLanguagesByContentStrategies(req.FileName, req.FileContent, languages)
	if len(languages) == 0 {
		res.Status = Error
		return res, nil
	}

	res.Language = languages[0]
	res.Status = Ok
	return res, nil
}
