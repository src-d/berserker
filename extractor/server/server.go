package main

import (
	"net"

	"github.com/src-d/berserker/extractor"

	log "github.com/inconshreveable/log15"
	"google.golang.org/grpc"
)

func main() {
	// TODO parametrize
	url := "localhost:8888"

	lis, err := net.Listen("tcp", url)
	if err != nil {
		panic(err)
	}

	grpcServer := grpc.NewServer()

	extractor.RegisterExtractorServiceServer(grpcServer, extractor.NewExtractorServiceServer())
	log.Info("server started", "URL", url)
	log.Error("server error", "err", grpcServer.Serve(lis))
}
