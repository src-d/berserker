package org.apache.spark.sql

import java.io.InputStream

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.scalatest.FunSuite
import tech.sourced.berserker.normalizer.service.RepositoriesDBService

import scala.io.Source

class BerserkerFunctionsSuite extends FunSuite {
  test("from_json_datatype works correctly") {
    val stream: InputStream = getClass.getResourceAsStream("/references_example.json")
    val json = Source.fromInputStream(stream).mkString

    val session = SparkSession.builder().master("local[2]").appName("testing").getOrCreate()

    val testRDD: RDD[Row] = session.sparkContext.parallelize(Row(json) :: Row(null) :: Row("") :: Nil)
    val testDF = session.createDataFrame(testRDD, StructType(StructField("references", StringType, nullable = true) :: Nil))

    val testDFParsed = testDF.withColumn("parsed",
      berserkerFunctions.from_json_datatype(testDF("references"), RepositoriesDBService.referencesSchema, Map()))

    testDFParsed.show()
    // TODO check output
  }
}
