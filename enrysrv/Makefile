BIN_DIR := bin
LOCAL_TAG := $(shell git describe --tags --abbrev=0)
LOCAL_COMMIT := $(shell git rev-parse --short HEAD)
LOCAL_BUILD := $(shell date +"%m-%d-%Y_%H_%M_%S")
LOCAL_LDFLAGS = -s -w -X main.version=$(LOCAL_TAG) -X main.build=$(LOCAL_BUILD) -X main.commit=$(LOCAL_COMMIT)

build:
	mkdir -p $(BIN_DIR); \
        go build -o $(BIN_DIR)/enrysrv -ldflags "$(LOCAL_LDFLAGS)" cli/*.go

