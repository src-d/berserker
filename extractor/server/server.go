package main

import (
	"flag"
	"net"
	"net/http"
	_ "net/http/pprof"

	"github.com/src-d/berserker/extractor"

	log "github.com/inconshreveable/log15"
	"google.golang.org/grpc"
)

var profiler = flag.Bool("profiler", false, "start CPU & memeory profiler")
var limit = flag.Uint64("limit", 0, "number of repositories, 0 = All from DB")

func main() {
	flag.Parse()
	// TODO parametrize
	profilerAddr := "localhost:6063"
	grpcAddr := "localhost:8888"

	startHTTPProfilingMaybe(profilerAddr)

	lis, err := net.Listen("tcp", grpcAddr)
	if err != nil {
		panic(err)
	}

	grpcServer := grpc.NewServer()

	extractor.RegisterExtractorServiceServer(grpcServer, extractor.NewExtractorServiceServer(*limit))
	log.Info("server started", "address", grpcAddr)

	err = grpcServer.Serve(lis)
	if err != nil {
		log.Error("server error", "err", err)
	}
}

func startHTTPProfilingMaybe(addr string) {
	if *profiler {
		go func() {
			log.Debug("Started CPU & Heap profiler at", "address", addr)
			err := http.ListenAndServe(addr, nil)
			if err != nil {
				log.Warn("Profiler failed to listen and serve", "address", addr, "err", err)
			}
		}()
	}
}
