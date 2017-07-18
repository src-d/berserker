package tech.sourced.berserker.normalizer

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import tech.sourced.berserker.normalizer.model.Schema
import tech.sourced.berserker.normalizer.service.ExtractorService

import scala.util.Properties

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
  val conf = new SparkConf()
  val sparkMaster = Properties.envOrElse("MASTER", "local[*]")
  val numberOfWorkers = 4

  val grpcHost = conf.get("tech.sourced.berserker.grpc.host", "localhost")
  val grpcPort = conf.getInt("tech.sourced.berserker.grpc.port", 8888)
  val grpcPlainText = conf.getBoolean("tech.sourced.berserker.grpc.plaintext", defaultValue = true)
  val parquetMode = conf.get("tech.sourced.berserker.parquet.files.mode", "overwrite")
  val parquetFilename = conf.get("tech.sourced.berserker.parquet.files.name", "files")

  val spark = SparkSession.builder()
    .config(conf)
    .appName("berserker")
    .master(sparkMaster)
    .getOrCreate()

  // Start gRPC connection
  val extractorService = ExtractorService(grpcHost, grpcPort, grpcPlainText)

  val repoIds = queryMetadataDbForAllFetchRepos()
  val dataRDD = spark.sparkContext
    .parallelize(repoIds, numberOfWorkers)
    .mapPartitions(partition => {
      //TODO(bzz): start executor-server Golang binary
      extractorService.getRepositoriesData(partition.toSeq).toIterator
    })

  val dataDF = spark.sqlContext.createDataFrame(dataRDD, Schema.files)

  dataDF.write
    .mode(parquetMode)
    .parquet(parquetFilename)
  dataDF.show(10)


  def queryMetadataDbForAllFetchRepos(): Seq[String] = {
    //TODO(bzz): jdbc to query DB
    return Seq("")
  }

}
