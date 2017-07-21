package main

import (
	"flag"
	"net"
	"net/http"
	_ "net/http/pprof"
	"strconv"

	"github.com/src-d/berserker/extractor"

	log "github.com/inconshreveable/log15"
	"google.golang.org/grpc"
)

var (
	profiler         = flag.Bool("profiler", false, "start CPU & memeory profiler")
	limit            = flag.Uint64("limit", 0, "number of repositories, 0 = All from DB")
	maxGrpcMsgSizeMb = flag.String("max-message-size", "400", "maximum message size to send/receive to/from clients (in Mb)")
)

const GlobalMaxMsgSizeMb = 2048

func main() {
	flag.Parse()
	// TODO parametrize
	grpcAddr := "localhost:8888"
	profilerAddr := "localhost:6063"
	startHTTPProfilingMaybe(profilerAddr)

	lis, err := net.Listen("tcp", grpcAddr)
	if err != nil {
		panic(err)
	}

	maxMsgSize := parseMaxMessageSize(*maxGrpcMsgSizeMb)
	grpcServer := grpc.NewServer(grpc.MaxSendMsgSize(maxMsgSize))

	extractorService := extractor.NewExtractorServiceServer(*limit, maxMsgSize)
	extractor.RegisterExtractorServiceServer(grpcServer, extractorService)
	log.Info("server started", "address", grpcAddr)

	err = grpcServer.Serve(lis)
	if err != nil {
		log.Error("server error", "err", err)
	}
}

func parseMaxMessageSize(maxMessageSize string) int {
	r := 0
	if maxMessageSize == "" {
		return r
	}

	n, err := strconv.ParseUint(maxMessageSize, 10, 16)
	if err != nil {
		log.Error("Can not parse size in Mb", "size", n, "err", err)
		return r
	}

	if n >= GlobalMaxMsgSizeMb {
		r = GlobalMaxMsgSizeMb
	}
	r = int(n * 1024 * 1024) // Convert MB to B
	log.Debug("Max gRPC msg size set to", "maxSize", r)
	return r
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
