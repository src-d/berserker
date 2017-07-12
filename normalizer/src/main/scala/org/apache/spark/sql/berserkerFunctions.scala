package org.apache.spark.sql

import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.functions.udf

object berserkerFunctions {
  private def withExpr(expr: Expression): Column = Column(expr)

  /**
    * (Scala-specific) Parses a column containing a JSON string into a `DataType` with the
    * specified schema. Returns `null`, in the case of an unparseable string.
    *
    * @param e       a string column containing JSON data.
    * @param schema  the schema to use when parsing the json string
    * @param options options to control how the json is parsed. accepts the same options and the
    *                json data source.
    */
  def from_json_datatype(e: Column, schema: DataType, options: Map[String, String]): Column = withExpr {
    JsonToStructFromDatatype(schema, options, e.expr)
  }

  private val toHex = (in: Seq[Byte]) => Option(in).orNull.map("%02x" format _).mkString

  /**
    * Transform a byte array representation to an Hex String
    *
    * @return
    */
  def bytearray_to_hex: UserDefinedFunction = udf(toHex)

}
