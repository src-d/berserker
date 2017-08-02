# Berserker Extractor [![Build Status](https://travis-ci.org/src-d/berserker.svg?branch=master)](https://travis-ci.org/src-d/berserker)

Berserker is an Apache Spark application using it's Scala API.
It extracts UAST and other information about every file from the given set of .siva files and stores the result in Parquet format.


## Architecture

It's part of repository data collection pipeline:
 - reads the output of [Borges](https://github.com/src-d/borges)
 - uses `go-siva` to unpack .siva files to headles RootedRepository in local FS
 - uses JGit to iterate over files at HEAD of the main original repository (skip forks)
 - detects languages using [Enry](https://github.com/src-d/enry/)
 - parses every file to UAST using [Bblfsh](https://github.com/bblfsh/server)

It uses gRPC to talk to [Enry server](./enrysrv) and bblfsh/server for language detection and actual UAST parsing.


## Pre-requests
 - Bblfsh sever running
   ```
   docker run --privileged -p 9432:9432 --name bblfsh bblfsh/server:dev-<sha> --max-message-size=100
   ```
 - enrysrv binary built and running on 9091
   ```
   #make sure the Berserker clone is under $GOPATH
   cd enrysrv; ./build
   ./bin/enrysrv server
   ```
 - Scala client for Bblfsh server built (until published on sonatype.org)
   ```
   ./local-install-bblfsh-client-scala.sh
   ```

## Build

 - `./sbt compile` to compile and generate gRPC code using ScalaPB from `./enrysrv/*.proto`
 - `./sbt package` to build `spark-submit`'able .jar file
 - `./sbt assembly` to build fatJar for using `java -jar` (\w Scala and Apache Spark inside)

## Test
There are 2 types of tests: UnitTests in Scala and end-to-en integration tests. To run both do

```
./test
```

## Run

### Local mode
On local machine for to use Apache Spark in local mode
```
./berserker --help
```

### Apache Spark cluster

```
MASTER="spark-master-url" ./berserker-cluster --help
```


### Kubernetes 

For running on Apache Spark deployed on K8s

TBD

```
kubectl run ....
```
