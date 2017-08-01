package enrysrv

import (
	"golang.org/x/net/context"
)

type enrysrvServiceServer struct {
}

func NewEnrysrvServiceServer() *enrysrvServiceServer {
	return &enrysrvServiceServer{}
}
func (s *enrysrvServiceServer) GetLanguage(ctx context.Context, in *EnryRequest) (result *EnryResponse, err error) {
	result = new(EnryResponse)
	result, err = GetLanguage(in)
	return
}
