package tech.sourced.berserker.normalizer

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
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
  //TODO(bzz): make proper CLI args
  val numberOfWorkers = 4

  val grpcHost = conf.get("tech.sourced.berserker.grpc.host", "localhost")
  val grpcPort = conf.getInt("tech.sourced.berserker.grpc.port", 8888)
  val grpcPlainText = conf.getBoolean("tech.sourced.berserker.grpc.plaintext", defaultValue = true)
  val grpcMaxMsgSize = conf.getInt("tech.sourced.berserker.grpc.max-size", defaultValue = 400 * 1042 * 1042)
  val parquetMode = conf.get("tech.sourced.berserker.parquet.files.mode", "overwrite")
  val parquetFilename = conf.get("tech.sourced.berserker.parquet.files.name", "files")

  val spark = SparkSession.builder()
    .config(conf)
    .appName("berserker")
    .master(sparkMaster)
    .getOrCreate()

  // Start gRPC connection

  val extractorService = ExtractorService(grpcHost, grpcPort, grpcMaxMsgSize, grpcPlainText)

  val dataRDD = getFetchedRepositoriesIds()
    .repartition(numberOfWorkers)
    .mapPartitions(partition => {
      //TODO(bzz): start executor-server Golang binary
      extractorService.getRepositoriesData(partition.toSeq).toIterator
    })

  val dataDF = spark.sqlContext.createDataFrame(dataRDD, Schema.files)

  dataDF.write
    .mode(parquetMode)
    .parquet(parquetFilename)
  dataDF.show(10)


  def getFetchedRepositoriesIds(limit: Int = 0): RDD[String] = {
    import spark.implicits._
    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", "jdbc:postgresql://localhost:5432/testing")
      .option("dbtable", "repositories")
      .option("user", "testing")
      .option("password", "testing")
      .load()

    val ids = if (n > 0) {
      jdbcDF.select($"id").limit(limit)
    } else {
      jdbcDF.select($"id")
    }
    return ids.rdd.map(_.getString(0))
  }

}
