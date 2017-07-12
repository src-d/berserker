package tech.sourced.berserker.normalizer

import org.apache.spark.sql.SparkSession
import tech.sourced.berserker.normalizer.model.Schema
import tech.sourced.berserker.normalizer.service.{ExtractorService, RepositoriesDBService}

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
  val jdbcConnection = "jdbc:postgresql://localhost:5432/testing"
  val user = "testing"
  val password = "testing"
  val tableName = "repositories"

  val spark = SparkSession.builder()
    .appName(appName)
    .master(masterUrl)
    .getOrCreate()

  // Start gRPC connection
  val extractorService = ExtractorService(grpcHost, grpcPort, grpcPlainText)
  val reposService = RepositoriesDBService(jdbcConnection, user, password, spark)

  val dataRDD = spark.sparkContext.parallelize(extractorService.getRepositoriesData)
  val dataDF = spark.sqlContext.createDataFrame(dataRDD, Schema.files)

  dataDF.write.option("compression", parquetCompression)
    .mode(parquetMode)
    .parquet(parquetFilename)
  dataDF.show(10)
}
