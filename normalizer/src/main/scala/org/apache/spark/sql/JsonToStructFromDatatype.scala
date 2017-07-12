package org.apache.spark.sql

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.TypeCheckResult
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.catalyst.expressions.{ExpectsInputTypes, Expression, UnaryExpression}
import org.apache.spark.sql.catalyst.util.{GenericArrayData, ParseModes}
import org.apache.spark.sql.types._
import org.apache.spark.sql.catalyst.json._
import org.apache.spark.unsafe.types.UTF8String

// XXX use this https://github.com/apache/spark/pull/16929/files in new spark version
/**
  * Converts an json input string to a [[DataType]] with the specified schema.
  */
case class JsonToStructFromDatatype(schema: DataType, options: Map[String, String], child: Expression)
  extends UnaryExpression with CodegenFallback with ExpectsInputTypes {
  override def nullable: Boolean = true

  @transient
  lazy val parser =
    new JacksonParser(
      rowSchema,
      "invalid", // Not used since we force fail fast.  Invalid rows will be set to `null`.
      new JSONOptions(options ++ Map("mode" -> ParseModes.FAIL_FAST_MODE)))

  override def dataType: DataType = schema

  override def nullSafeEval(json: Any): Any = {
    // When input is,
    //   - `null`: `null`.
    //   - invalid json: `null`.
    //   - empty string: `null`.
    //
    // When the schema is array,
    //   - json array: `Array(Row(...), ...)`
    //   - json object: `Array(Row(...))`
    //   - empty json array: `Array()`.
    //   - empty json object: `Array(Row(null))`.
    //
    // When the schema is a struct,
    //   - json object/array with single element: `Row(...)`
    //   - json array with multiple elements: `null`
    //   - empty json array: `null`.
    //   - empty json object: `Row(null)`.

    // We need `null` if the input string is an empty string. `JacksonParser` can
    // deal with this but produces `Nil`.
    if (json.toString.trim.isEmpty) return null

    try {
      converter(parser.parse(
        json.asInstanceOf[UTF8String].toString
      ))
    } catch {
      case _: SparkSQLJsonProcessingException => null
    }
  }

  override def inputTypes: Seq[AbstractDataType] = StringType :: Nil

  override def checkInputDataTypes(): TypeCheckResult = schema match {
    case _: StructType | ArrayType(_: StructType, _) =>
      super.checkInputDataTypes()
    case _ => TypeCheckResult.TypeCheckFailure(
      s"Input schema ${schema.simpleString} must be a struct or an array of structs.")
  }

  @transient
  private lazy val rowSchema = schema match {
    case st: StructType => st
    case ArrayType(st: StructType, _) => st
  }

  // This converts parsed rows to the desired output by the given schema.
  @transient
  private lazy val converter = schema match {
    case _: StructType =>
      (rows: Seq[InternalRow]) => if (rows.length == 1) rows.head else null
    case ArrayType(_: StructType, _) =>
      (rows: Seq[InternalRow]) => new GenericArrayData(rows)
  }


}