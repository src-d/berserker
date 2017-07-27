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
INFO[0000] version: build:07-27-2017_11_03_07 commit:d7f4809 
INFO[0000] setting maximum size for sending and receiving messages to 209715200 
INFO[0000] binding to 0.0.0.0:9091                      
DEBU[0000] registering gRPC service                     
INFO[0000] starting gRPC server                         
INFO[0009] Incoming request: OK 320.918µs --- filename: "foo.py", language: "Python", strategy: EXTENSION 
INFO[0014] Incoming request: NEED CONTENT 210.074µs --- filename: "foo", language: "", strategy:  
INFO[0029] Incoming request: IGNORED 142.816µs --- filename: "bar.json", language: "", strategy:  
INFO[0056] Incoming request: OK 174.009µs --- filename: "foo", language: "Python", strategy: SHEBANG 
``` 

```bash
$ ./bin/enrysrv client -f foo.py
INFO[0000] version: build:07-27-2017_11_03_07 commit:d7f4809 
INFO[0000] setting maximum size for sending and receiving messages to 104857600 
INFO[0000] dialing server at 0.0.0.0:9091               
DEBU[0000] instantiating service client                 
INFO[0000] sending request                              
INFO[0000] detected language: Python                    
DEBU[0000] exiting without error 
```

For the client `-f` flag is mandatory. If the server can't detect the language only with the file name, it replies with a `need content` message:

```bash
$ ./bin/enrysrv client -f foo   
INFO[0000] version: build:07-27-2017_11_03_07 commit:d7f4809 
INFO[0000] setting maximum size for sending and receiving messages to 104857600 
INFO[0000] dialing server at 0.0.0.0:9091               
DEBU[0000] instantiating service client                 
INFO[0000] sending request                              
WARN[0000] need content file to detect language         
DEBU[0000] exiting without error
```

To send the file content to the server, you must add `-c` flag when you run the client:

```bash
$ ./bin/enrysrv client -c -f /tmp/foo
INFO[0000] version: build:07-27-2017_11_03_07 commit:d7f4809 
DEBU[0000] guessing language with content               
INFO[0000] setting maximum size for sending and receiving messages to 104857600 
INFO[0000] dialing server at 0.0.0.0:9091               
DEBU[0000] instantiating service client                 
INFO[0000] sending request                              
INFO[0000] detected language: Python                    
DEBU[0000] exiting without error  
```

If `enrysrv` detect the file as a vendor/dotfile/configuration/documentation file, it will ingore that file:

```bash
INFO[0000] version: build:07-27-2017_11_03_07 commit:d7f4809 
INFO[0000] setting maximum size for sending and receiving messages to 104857600 
INFO[0000] dialing server at 0.0.0.0:9091               
DEBU[0000] instantiating service client                 
INFO[0000] sending request                              
WARN[0000] ingored case, file is Vendor/DotFile/Documentation/Configuration 
DEBU[0000] exiting without error     
```

To launch a http server for profiling, add `--profiler` flag to the server:

```bash
$ ./bin/enrysrv server --profiler
INFO[0000] version: build:07-27-2017_11_03_07 commit:d7f4809 
INFO[0000] setting maximum size for sending and receiving messages to 209715200 
INFO[0000] binding to 0.0.0.0:9091                      
DEBU[0000] registering gRPC service                     
INFO[0000] starting gRPC server                         
DEBU[0000] Started CPU & Heap profiler at "localhost:6073" 
```