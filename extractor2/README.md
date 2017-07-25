# Berserker Normalizer

Berserker Extractor is an Apache Spark application in Scala API. 
It extracts information about every file from the given set of .siva files and stores it in Parquet format.
It uses gRPC to talk to enry-server and bblfsh/server for language detection and parsing. 

## Pre-requests
 - Bblfsh sever running
   ```
   docker run --privileged -p 9432:9432 --name bblfsh bblfsh/server:dev-<sha> --max-message-size=100
   ```
 - enry-server built

## How to install

 - ScalaPB generates code from `../enry-server/*.proto` on every `./sbt compile`
 - `./sbt package` to build `spark-submit`'able .jar file
 - `./sbt assembly` to build fatJar for using `java -jar` (\w Scala and Apache Spark inside)


