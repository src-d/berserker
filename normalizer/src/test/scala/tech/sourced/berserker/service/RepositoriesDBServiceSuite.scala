package tech.sourced.berserker.service

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import tech.sourced.berserker.normalizer.service.RepositoriesDBService

class RepositoriesDBServiceSuite extends FunSuite with BeforeAndAfterAll {

  // TODO mock using directly the class instead of the companion object
  private var service: RepositoriesDBService = null

  override protected def beforeAll(): Unit = {
    val spark = SparkSession.builder().appName("analyzer").master("local[8]").getOrCreate()
    // TODO parametrize
    service = RepositoriesDBService("jdbc:postgresql://localhost:5432/testing", "testing", "testing", spark)
  }

  override protected def afterAll(): Unit = {
    service = null
  }

  test("getAllRepositories") {
    service.getAllRepositories.show()
    // TODO check output
  }

  test("findAllFetchedRepositoriesWithMasterRef") {
    service.findAllFetchedRepositoriesWithMasterRef.show()
    // TODO check output
  }

}
