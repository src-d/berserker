package extractor

// proteus:generate
type Request struct {
	RepositoryID   string
	RootCommitHash []byte
	Reference      string
}

// proteus:generate
type RepositoryData struct {
	RepositoryID   string
	Files []File
}

type File struct {
	Language string
	Path     string
	AST      string
	Hash     []byte
}
