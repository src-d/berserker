package main

import (
	"net"
	"net/http"

	"github.com/src-d/berserker/enrysrv"

	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
)

type serverCmd struct {
	commonCmd
	Address      string `short:"a" long:"address" description:"server address to bind to" default:"0.0.0.0:9091"`
	Profiler     bool   `long:"profiler" description:"start CPU & memeory profiler"`
	ProfilerAddr string `long:"profiler-addr" description:"profiler address" default:"localhost:6073"`
}

func (c *serverCmd) Execute(args []string) error {
	if err := c.exec(args); err != nil {
		return err
	}

	return c.startServer()
}

func (c *serverCmd) startServer() error {
	c.startHTTPProfilingMaybe()

	maxMessageSize, err := c.parseMaxMessageSize()
	if err != nil {
		logrus.Warnf("wrong --max-message-size value, it will be set to a default value")
	}

	serverOpts := []grpc.ServerOption{}
	if maxMessageSize != 0 {
		logrus.Infof("setting maximum size for sending and receiving messages to %d", maxMessageSize)
		serverOpts = append(serverOpts, grpc.MaxRecvMsgSize(maxMessageSize))
		serverOpts = append(serverOpts, grpc.MaxSendMsgSize(maxMessageSize))
	}

	logrus.Infof("binding to %s", c.Address)
	lis, err := net.Listen("tcp", c.Address)
	if err != nil {
		return err
	}

	grpcServer := grpc.NewServer(serverOpts...)
	logrus.Debug("registering gRPC service")
	enrysrv.RegisterEnrysrvServiceServer(grpcServer, enrysrv.NewEnrysrvServiceServer())

	logrus.Info("starting gRPC server")
	return grpcServer.Serve(lis)
}

func (c *serverCmd) startHTTPProfilingMaybe() {
	if c.Profiler {
		go func() {
			logrus.Debugf("Started CPU & Heap profiler at %q", c.ProfilerAddr)
			err := http.ListenAndServe(c.ProfilerAddr, nil)
			if err != nil {
				logrus.Warnf("Profiler failed to listen and serve %q: %v", c.ProfilerAddr, err)
			}
		}()
	}
}
