# Berserker Normalizer

Berserker Normalizer is an Apache Spark application talking to Extractor though gRPC.
It receives information about every file and stores it in Parquet format.

## How to install

- ScalaPB generates code from `.proto` on every `./sbt compile`
  `~~`./sbt protoc-generate` to regenerate gRPC client from `.proto` files~~
- `./sbt package` to build `spark-submit`'able .jar file
- `./sbt TODO(bzz)` to build fat standalone Jar (\w Spark inside)


