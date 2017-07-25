# Enry Server

Enry Server run as a gRPC service which expose `GetLanguage` to detect the programming language a file is written in.

## How to install

- To generate the vendor folder use `glide install` or `glide up` if you want to update the dependencies.
- To regenerate `.proto` files, execute `go generate`
- To build a binary `make build`. It generates `./bin/enrysrv`

## Usage

`enrysrv` can be launched as server or client. Client can choose if send the file content to the server.

```bash
$ ./bin/enrysrv server --max-message-size 200
DEBU[0000] version: build:07-25-2017_12_09_04 commit:695073a 
DEBU[0000] setting maximum size for sending and receiving messages to 209715200 
DEBU[0000] binding to 0.0.0.0:9091                      
DEBU[0000] registering gRPC service                     
DEBU[0000] starting gRPC server     
``` 

```bash
$ ./bin/enrysrv client -f foo.py
DEBU[0000] version: build:07-25-2017_12_09_04 commit:695073a 
DEBU[0000] setting maximum size for sending and receiving messages to 104857600 
DEBU[0000] dialing server at 0.0.0.0:9091               
DEBU[0000] instantiating service client                 
DEBU[0000] sending request                              
DEBU[0000] detected language: Python                    
DEBU[0000] exiting without error 
```

For the client `-f` flag is mandatory. If the server can't detect the language only with the file name, it replies with a `need content` message:

```bash
$ ./bin/enrysrv client -f /tmp/foo   
DEBU[0000] version: build:07-25-2017_12_09_04 commit:695073a 
DEBU[0000] setting maximum size for sending and receiving messages to 104857600 
DEBU[0000] dialing server at 0.0.0.0:9091               
DEBU[0000] instantiating service client                 
DEBU[0000] sending request                              
DEBU[0000] need content file to detect language         
DEBU[0000] exiting without error  
```

To send the file content to the server, you must add `-c` flag when you run the client:

```bash
$ ./bin/enrysrv client -c -f /tmp/foo
DEBU[0000] version: build:07-25-2017_12_09_04 commit:695073a 
DEBU[0000] guessing language with content               
DEBU[0000] setting maximum size for sending and receiving messages to 104857600 
DEBU[0000] dialing server at 0.0.0.0:9091               
DEBU[0000] instantiating service client                 
DEBU[0000] sending request                              
DEBU[0000] detected language: Python                    
DEBU[0000] exiting without error     
```

To launch a http server for profiling, add `--profiler` flag to the server:

```bash
$ ./bin/enrysrv server --profiler
DEBU[0000] version: build:07-25-2017_12_37_44 commit:695073a 
DEBU[0000] setting maximum size for sending and receiving messages to 104857600 
DEBU[0000] binding to 0.0.0.0:9091                      
DEBU[0000] registering gRPC service                     
DEBU[0000] starting gRPC server                         
DEBU[0000] Started CPU & Heap profiler at "localhost:6073"
```