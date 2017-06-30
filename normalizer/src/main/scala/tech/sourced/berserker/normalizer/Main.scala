package tech.sourced.berserker.normalizer

import org.apache.spark.sql.SparkSession
import tech.sourced.berserker.normalizer.model.Schema
import tech.sourced.berserker.normalizer.service.ExtractorService

object Main extends App {
  // TODO parametrize
  var masterUrl = "local[2]"
  var appName = "normalizer"
  var grpcHost = "localhost"
  var grpcPort = 8888
  var grpcPlainText = true
  var parquetCompression = "none"
  var parquetMode = "overwrite"
  var parquetFilename = "parquet"

  val spark = SparkSession.builder()
    .appName(appName)
    .master(masterUrl)
    .getOrCreate()

  // Start gRPC connection
  val extractorService = ExtractorService(grpcHost, grpcPort, grpcPlainText)

  val dataRDD = spark.sparkContext.parallelize(extractorService.getRepositoriesData)
  val dataDF = spark.sqlContext.createDataFrame(dataRDD, Schema.files)

  dataDF.write.option("compression", parquetCompression)
    .mode(parquetMode)
    .parquet(parquetFilename)
  dataDF.show(10)
}
