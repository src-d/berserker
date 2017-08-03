package tech.sourced.berserker

import org.rogach.scallop.ScallopConf


/**
  * Berserker Command Line Interface definition
  **/
class CLI(arguments: Seq[String]) extends ScallopConf(arguments) {
  version("Berserker - large scale UAST extractor, by Source{d}")
  banner(
    """Usage: berserker [OPTION]...
      |Berserker extracts UAST from given .siva files
      |Options:
      |""".stripMargin)
  footer("\nFor the details, consult https://github.com/src-d/berserker#architecture")

  val input = opt[String](name = "input", noshort = true, descr = "Input path to read .siva files from",
    default = Some("/tmp/rooted-repos"))

  val output = opt[String](name = "output", noshort = true, descr = "Output path to write .parquet files to",
    default = Some("all.files"))

  val numberPartitions = opt[Int](name = "numberPartitions", noshort = true,
    descr = "Divide all .siva files in number of partitions",
    default = Some(4))

  val grpcMaxMsgSize = opt[Int](name = "grpcMaxMsgSize", noshort = true,
    descr = "Maximum size of gRPC message, in bytes",
    default = Some(100 * 1024 * 1024))

  val sivaFileLimit = opt[Int](name = "sivaFileLimit", noshort = true,
    descr = "Maximum number of .siva files to process",
    default = Some(0))

  val enryHost = opt[String](name = "enryHost", noshort = true,
    descr = "Host where Enry server is running",
    default = Some("0.0.0.0"))

  val enryPort = opt[Int](name = "enryPort", noshort = true,
    descr = "Port where Enry server is running",
    default = Some(9091))

  val bblfshHost = opt[String](name = "bblfshHost", noshort = true, descr = "Host where Babelfish server is running",
    default = Some("0.0.0.0"))

  val bblfshPort = opt[Int](name = "bblfshPort", noshort = true, descr = "Port where Babelfish server is running",
    default = Some(9432))

  //TODO(bzz): add deserialize() with SerializationProxy
  //https://github.com/scallop/scallop/issues/137#issuecomment-319676687
}
