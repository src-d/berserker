# Berserker Extractor

Berserker Extractor is in charge of obtain all the files from specific repositories, reading rooted repositories in siva files. It sends the data to the Spark processes using gRPC.

## How to install

- To generate the vendor folder use `glide install` or `glide up` if you want to update the dependencies.
- To regenerate `.proto` files, execute `proteus -p github.com/src-d/berserker/extractor -f proto`
- To build a gRPC server `go build -o extractor-server ./server/*.go`
- To build a CLI (usefull for debuging&profiling) `go build -o extractor ./cli/*.go`

