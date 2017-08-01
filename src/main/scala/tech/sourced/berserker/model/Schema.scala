package tech.sourced.berserker.model

import org.apache.spark.sql.types._

object Schema {

  val repositories = StructType(
      StructField("initHash", StringType, nullable = false) ::
      StructField("commitHash", StringType, nullable = false) ::
      StructField("repositoryId", StringType, nullable = false) ::
      StructField("repositoryUrl", StringType, nullable = true) ::
      StructField("reference", StringType, nullable = false) ::
      StructField("isMainRepository", BooleanType, nullable = false) ::
      Nil
  )

  val files = StructType(
      StructField("initHash", StringType, nullable = false) ::
      StructField("commitHash", StringType, nullable = false) ::
      StructField("blobHash", StringType, nullable = false) ::
      StructField("path", StringType, nullable = false) ::
      StructField("lang", StringType, nullable = true) ::
      Nil
  )

  val uasts = StructType(
      StructField("initHash", StringType, nullable = false) ::
      StructField("blobHash", StringType, nullable = false) ::
      StructField("lang", StringType, nullable = true) ::
      StructField("uast", BinaryType, nullable = true) ::
      Nil
  )

  val all = StructType(
    StructField("initHash", StringType, nullable = false) ::
      StructField("commitHash", StringType, nullable = false) ::
      StructField("blobHash", StringType, nullable = false) ::
      StructField("repositoryId", StringType, nullable = false) ::
      StructField("repositoryUrl", StringType, nullable = true) ::
      StructField("reference", StringType, nullable = false) ::
      StructField("isMainRepository", BooleanType, nullable = false) ::
      StructField("path", StringType, nullable = false) ::
      StructField("lang", StringType, nullable = true) ::
      StructField("uast", BinaryType, nullable = true) ::
      Nil
  )

}
