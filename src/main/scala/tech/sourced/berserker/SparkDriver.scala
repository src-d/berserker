package tech.sourced.berserker

import java.io.File

import github.com.bblfsh.sdk.protocol.generated.ParseResponse
import org.apache.hadoop.fs.Path
import org.apache.log4j.Logger
import org.apache.spark.{SparkConf, SparkFiles}
import org.apache.spark.sql.{Row, SparkSession}
import org.bblfsh.client.BblfshClient
import org.eclipse.jgit.lib.FileMode
import org.eclipse.jgit.treewalk.TreeWalk
import tech.sourced.berserker.git.{JGitFileIterator, RootedRepo}
import tech.sourced.berserker.model.Schema
import tech.sourced.berserker.spark.SerializableConfiguration
import tech.sourced.enry.{Enry, Guess}
import tech.sourced.siva.SivaUnpacker

import scala.util.Properties


object SparkDriver {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sparkMaster = Properties.envOrElse("MASTER", "local[*]")

    val cli = new CLI(args)
    if (args.length < 1) {
      cli.printHelp(  )
      System.exit(0)
    }
    cli.verify()
    val grpcMaxMsgSize = cli.grpcMaxMsgSize() //working around https://github.com/scallop/scallop/issues/137
    val bblfshHost = Properties.envOrElse("BBLFSH_SERVER_SERVICE_HOST", cli.bblfshHost())
    val bblfshPort = cli.bblfshPort()
    val sivaFilesPath = new Path(cli.input())

    val spark = SparkSession.builder()
      .config(conf)
      .appName("berserker")
      .master(sparkMaster)
      .getOrCreate()
    val sc = spark.sparkContext
    val driverLog = Logger.getLogger(getClass.getName)

    val confBroadcast = sc.broadcast(new SerializableConfiguration(sc.hadoopConfiguration))

    // list .siva files (on Driver)
    var sivaFiles = FsUtils.collectSivaFilePaths(sc.hadoopConfiguration, driverLog, sivaFilesPath)
    if (cli.sivaFileLimit() > 0) {
      sivaFiles = sivaFiles.take(Math.min(cli.sivaFileLimit(), sivaFiles.length))
    }

    val actualNumWorkers = Math.min(cli.numberPartitions(), sivaFiles.length)
    val remoteSivaFiles = sc.parallelize(sivaFiles, actualNumWorkers)
    driverLog.info(s"Processing ${sivaFiles.length} .siva files in $actualNumWorkers partitions")

    val workersNum = sc.getExecutorMemoryStatus.size-1
    driverLog.info(s"Cluster have $workersNum workers")

    // copy from HDFS and un-pack in tmp dir using go-siva (on Workers)
    val unpacked = remoteSivaFiles
      .map { sivaFile =>
        FsUtils.copyFromHDFS(confBroadcast.value.value, sivaFile)
      }
      .map { case (sivaFile, unpackDir) =>
        val log = Logger.getLogger(s"Stage: unpack siva file")
        val siva = new File(s"$unpackDir/$sivaFile")
        log.info(s"${siva.getAbsolutePath} exists: ${siva.exists} canRead:{${siva.canRead}}")
        new SivaUnpacker(siva.getAbsolutePath).unpack(unpackDir)
        (sivaFile.substring(0, sivaFile.lastIndexOf('.')), unpackDir)
      } //RDD["sivaInitHash sivaUnpackDir"]

    // iterate every un-packed .siva
    val intermediatePerFile = unpacked
      .mapPartitions(partition => {
        val log = Logger.getLogger(s"Stage: single partition")
        val bblfshService = BblfshClient(bblfshHost, bblfshPort, grpcMaxMsgSize)
        log.info(s"Connecting to Bblfsh server: $bblfshHost:$bblfshPort")

        partition
          .flatMap { case (initHash, sivaUnpackDir) =>
            val log = Logger.getLogger(s"Stage: process single")
            log.info(s"Processing repository in $sivaUnpackDir")
            JGitFileIterator(sivaUnpackDir, initHash, confBroadcast.value.value)
          }
          .filter { case (_, treeWalk, _, _) =>
            treeWalk.getFileMode(0) == FileMode.REGULAR_FILE || treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE
            //TODO(bzz): skip big well-known binaries .apk and .jar
          }
          .map { case (initHash, tree, ref, config) =>
            val log = Logger.getLogger(s"Stage: detecting a language")
            val path = tree.getPathString

            var content = Array.emptyByteArray
            var guessed = Enry.getLanguageByFilename(path)
            if (!guessed.safe) {
              content = RootedRepo.readFile(tree.getObjectId(0), tree.getObjectReader)
              log.info(s"Detecting lang for: $path using content size:${content.length}")
              guessed = if (content.isEmpty) {
                new Guess("", false) //Enry.unknownLanguage
              } else {
                Enry.getLanguageByContent(path, content)
              }
            }
            (initHash, tree, ref, config, path, content, guessed)
          }
          .filter { case (_,_,_,_,_,_, guessed) =>
            guessed != null && (guessed.language.equalsIgnoreCase("python") || guessed.language.equalsIgnoreCase("java"))
          }
          .flatMap { case (initHash, tree, ref, config, path, cachedContent, guessed) =>
            val log = Logger.getLogger(getClass.getName)
            val content = readIfNotCached(tree, cachedContent)

            var parsed: ParseResponse = ParseResponse.defaultInstance
            try { // detect language using enry server
              parsed = bblfshService.parse(path, content, guessed.language)
              log.info(s"Parsed $path - size:${content.length} bytes, status:${parsed.status}")
            }  catch {
              case e: Throwable => log.info(s"Could not parse UAST for $path, $e")
            }

            val row = if (parsed.errors.isEmpty) {
              //TODO(bzz): add repo URL from config
              Seq(Row(initHash,
                ref.getObjectId.name,           //commit_hash
                tree.getObjectId(0).name,       //blob_hash
                ref.getName.split('/').last,    //repository_id
                "",                             //repository_url (?from unpackDir/config)
                ref.getName.substring(0, ref.getName.lastIndexOf('/')), //reference
                true,                           //is_main_repository
                path, guessed.language,
                parsed.uast.get.toByteArray))
            } else {
              log.info(s"Did not finish parsing $path - errors:${parsed.errors}")
              Seq()
            }
            row
          }
      })
    //TODO(bzz): add accumulators: repos/files process/skipped (+ count for each error type)

    val intermediatePerFileDF = spark.sqlContext.createDataFrame(intermediatePerFile, Schema.all)
    intermediatePerFileDF.write
      .mode("overwrite")
      .parquet(cli.output())

    val parquetAllDF = spark.read
      .parquet(cli.output())
      .show(10)

    //TODO(bzz) produce tables: files, UAST and repositories from intermediatePerFile
    //TODO(bzz) de-duplicate/repartition final tables
    //TODO(bzz): print counters - performance accumulators, errors
  }


  def split(both: String): (String, String) = {
    val fileNameAndUnpackedDir = both.split(' ')
    val sivaFileName = fileNameAndUnpackedDir(0).split('/').last.split('.')(0)
    val sivaUnpackedDir = fileNameAndUnpackedDir(1)
    (sivaFileName, sivaUnpackedDir)
  }

  def readIfNotCached(treeWalk: TreeWalk, cachedContent: Array[Byte]) = {
    val content = if (cachedContent.isEmpty) {
      RootedRepo.readFile(treeWalk.getObjectId(0), treeWalk.getObjectReader)
    } else {
      cachedContent
    }
    //TODO(bzz): handle non-utf-8
    val encodedContent = new String(content, "utf-8")
    encodedContent
  }


}