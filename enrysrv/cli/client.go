package main

import (
	"context"
	"io/ioutil"

	"github.com/src-d/berserker/enrysrv"

	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
)

type clientCmd struct {
	commonCmd
	Address     string `short:"a" long:"address" description:"server addres to connect to" default:"0.0.0.0:9091"`
	File        string `short:"f" long:"file" description:"file to perform language detection" required:"true"`
	WithContent bool   `short:"c" long:"content" description:"set to include content in language detection"`
}

func (c *clientCmd) Execute(args []string) error {
	if err := c.exec(args); err != nil {
		return err
	}

	return c.GetLanguage(c.File)
}

func (c *clientCmd) GetLanguage(filename string) error {
	req := &enrysrv.EnryRequest{FileName: filename}
	if c.WithContent {
		content, err := ioutil.ReadFile(filename)
		if err != nil {
			return err
		}

		logrus.Debug("guessing language with content")
		req.FileContent = content
	}

	res, err := c.runClient(req)
	if err != nil {
		return err
	}

	switch res.Status {
	case enrysrv.Ok:
		logrus.Debugf("detected language: %v", res.Language)
	case enrysrv.NeedContent:
		logrus.Debug("need content file to detect language")
	case enrysrv.Error:
		logrus.Debug("couldn't detect language")
	}

	return nil
}

func (c *clientCmd) runClient(req *enrysrv.EnryRequest) (*enrysrv.EnryResponse, error) {
	maxMessageSize, err := c.parseMaxMessageSize()
	if err != nil {
		logrus.Warnf("wrong --max-message-size value, it will be set to a default value")
	}

	callOpts := []grpc.CallOption{}
	if maxMessageSize != 0 {
		logrus.Debugf("setting maximum size for sending and receiving messages to %d", maxMessageSize)
		callOpts = append(callOpts, grpc.MaxCallRecvMsgSize(maxMessageSize))
		callOpts = append(callOpts, grpc.MaxCallSendMsgSize(maxMessageSize))
	}

	logrus.Debugf("dialing server at %s", c.Address)
	conn, err := grpc.Dial(c.Address, grpc.WithInsecure(), grpc.WithDefaultCallOptions(callOpts...))
	if err != nil {
		return nil, err
	}

	logrus.Debug("instantiating service client")
	client := enrysrv.NewEnrysrvServiceClient(conn)

	logrus.Debug("sending request")
	return client.GetLanguage(context.TODO(), req)
}
