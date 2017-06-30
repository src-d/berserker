package extractor

import (
	"fmt"
)

type Service struct {
	// TODO
}

//proteus:generate
func (*Service) GetRepositoryData(r *Request) (*RepositoryData, error) {
	// TODO
	return nil, fmt.Errorf("NOT IMPLEMENTED YET")
}

//proteus:generate
func (*Service) GetRepositoriesData() ([]*RepositoryData, error) {
	return nil, fmt.Errorf("NOT IMPLEMENTED YET")
}
