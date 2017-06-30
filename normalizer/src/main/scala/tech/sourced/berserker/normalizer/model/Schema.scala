package tech.sourced.berserker.normalizer.model

import org.apache.spark.sql.types.{StringType, StructField, StructType}

object Schema {
  val files = StructType(
    StructField("repoId", StringType, nullable = false) ::
      StructField("hash", StringType, nullable = false) ::
      StructField("path", StringType, nullable = false) ::
      StructField("lang", StringType, nullable = true) ::
      StructField("ast", StringType, nullable = true) ::
      Nil
  )
}
