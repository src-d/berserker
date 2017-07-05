package tech.sourced.berserker.normalizer.service

import java.util.Properties

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession, berserkerFunctions}
import tech.sourced.berserker.normalizer.service.RepositoriesDBService._
import berserkerFunctions._

class RepositoriesDBService(reposDF: DataFrame) {

  /**
    * Get all repositories from repositories table with no filters
    *
    * @return DataFrame containing all the repositories with no filters,
    *         containing a special column called 'parsedReferences',
    *         an structured column data obtained from '_references' json column.
    *
    */
  def getAllRepositories: DataFrame = {
    reposDF.withColumn(ParsedReferencesCol,
      berserkerFunctions.from_json_datatype(
        reposDF(ReferencesCol),
        referencesSchema,
        Map.empty[String, String])
    )
  }

  /**
    * Get all master references for each repository, if any.
    *
    * @return DataFrame containing in each row the master reference for each
    *         repository, if any.
    */
  def findAllFetchedRepositoriesWithMasterRef: DataFrame = {
    val fetchedDF = this.getAllRepositories
      .filter(reposDF(StatusCol).equalTo(FetchedStatus))

    // Create one row per element into references struct
    val referencesDF =
      fetchedDF.withColumn("reference", explode(fetchedDF(ParsedReferencesCol)))

    // Filter all references that are not master
    val frDF =
      referencesDF.filter(referencesDF(ReferenceNameCol).equalTo(lit(MasterBranch)))

    frDF.select(
      frDF(IdCol).as("repository_id"),
      frDF(EndpointsCol).getItem(0).as("url"),
      frDF(ReferenceNameCol).as("ref_name"),
      bytearray_to_hex(frDF(ReferenceInitCol)).as("ref_hash")
    )
  }
}

object RepositoriesDBService {
  val RepositoriesTableName = "repositories"
  val StatusCol = "status"
  val ReferencesCol = "_references"
  val ParsedReferencesCol = "parsedReferences"
  val ReferenceNameCol = "reference.Name"
  val ReferenceInitCol = "reference.Init"
  val EndpointsCol = "endpoints"
  val IdCol = "id"

  val FetchedStatus = "fetched"
  val MasterBranch = "refs/heads/master"

  def apply(jdbcConnection: String, user: String, password: String, session: SparkSession): RepositoriesDBService = {
    val props = new Properties()
    props.setProperty("user", user)
    props.setProperty("password", password)

    val repositoriesDF = session.read.jdbc(jdbcConnection, RepositoriesDBService.RepositoriesTableName, props)

    new RepositoriesDBService(repositoriesDF)
  }

  val referencesSchema = ArrayType(
    StructType(
      StructField("Hash", ArrayType(ByteType), nullable = true) ::
        StructField("Init", ArrayType(ByteType), nullable = true) ::
        StructField("Name", StringType, nullable = true) ::
        StructField("Roots", ArrayType(ArrayType(ByteType)), nullable = true) ::
        StructField("CreatedAt", DateType, nullable = true) ::
        StructField("UpdatedAt", DateType, nullable = true) ::
        Nil
    )
  )
}
