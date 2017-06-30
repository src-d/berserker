package extractor

import (
	"golang.org/x/net/context"
)

type extractorServiceServer struct {
	Service *Service
}

func NewExtractorServiceServer() *extractorServiceServer {
	return &extractorServiceServer{}
}
func (s *extractorServiceServer) Service_GetRepositoriesData(ctx context.Context, in *Service_GetRepositoriesDataRequest) (result *Service_GetRepositoriesDataResponse, err error) {
	result = new(Service_GetRepositoriesDataResponse)
	result.Result1, err = s.Service.GetRepositoriesData()
	return
}
func (s *extractorServiceServer) Service_GetRepositoryData(ctx context.Context, in *Request) (result *RepositoryData, err error) {
	result = new(RepositoryData)
	result, err = s.Service.GetRepositoryData(in)
	return
}
