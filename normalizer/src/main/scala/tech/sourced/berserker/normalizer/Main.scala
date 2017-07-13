package tech.sourced.berserker.normalizer

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import tech.sourced.berserker.normalizer.model.Schema
import tech.sourced.berserker.normalizer.service.ExtractorService

/**
  * Start Berserker application.
  *
  * Some useful variables:
  * - spark.sql.parquet.compression.codec: uncompressed, snappy, gzip, lzo
  * - tech.sourced.berserker.grpc.host: grpc service hostname or ip, localhost
  * by default
  * - tech.sourced.berserker.grpc.port: grpc service port
  * - tech.sourced.berserker.grpc.plaintext: if true, messages over grpc will
  * be sent in plain text
  * - tech.sourced.berserker.parquet.files.mode: parquet mode used to write
  * the 'files' output
  * - tech.sourced.berserker.parquet.files.name: parquet file name for 'files'
  * output
  */
object Main extends App {
  var conf = new SparkConf()
  conf.setAppName("berserker")
  var grpcHost = conf.get("tech.sourced.berserker.grpc.host", "localhost")
  var grpcPort = conf.getInt("tech.sourced.berserker.grpc.port", 8888)
  var grpcPlainText = conf.getBoolean("tech.sourced.berserker.grpc.plaintext",
    defaultValue = true)
  var parquetMode = conf.get("tech.sourced.berserker.parquet.files.mode", "overwrite")
  var parquetFilename = conf.get("tech.sourced.berserker.parquet.files.name", "files")

  val spark = SparkSession.builder().config(conf).getOrCreate()

  // Start gRPC connection
  val extractorService = ExtractorService(grpcHost, grpcPort, grpcPlainText)

  val dataRDD = spark.sparkContext.parallelize(extractorService.getRepositoriesData)
  val dataDF = spark.sqlContext.createDataFrame(dataRDD, Schema.files)

  dataDF.write
    .mode(parquetMode)
    .parquet(parquetFilename)
  dataDF.show(10)
}
