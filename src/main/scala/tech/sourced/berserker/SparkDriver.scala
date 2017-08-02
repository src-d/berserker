package tech.sourced.berserker

import github.com.srcd.berserker.enrysrv.generated.Status
import org.apache.hadoop.fs.Path
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.bblfsh.client.BblfshClient
import org.eclipse.jgit.lib.FileMode
import org.eclipse.jgit.treewalk.TreeWalk
import tech.sourced.berserker.git.{JGitFileIterator, RootedRepo}
import tech.sourced.berserker.model.Schema
import tech.sourced.berserker.service.EnryService
import tech.sourced.berserker.spark.SerializableConfiguration

import scala.util.Properties


object SparkDriver {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sparkMaster = Properties.envOrElse("MASTER", "local[*]")

    val cli = new CLI(args)
    if (args.length < 1) {
      cli.printHelp()
      System.exit(1)
    }
    cli.verify()
    val grpcMaxMsgSize = cli.grpcMaxMsgSize() //working around https://github.com/scallop/scallop/issues/137
    val enryHost = cli.enryHost()
    val enryPort = cli.enryPort()
    val bblfshHost = cli.bblfshHost()
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
    //TODO(bzz) extract enrysrv     binary from Jar and as.addFile() it
    //TODO(bzz) extract siva-unpack binary from Jar and as.addFile() it

    // list .siva files (on Driver)
    var sivaFiles = FsUtils.collectSivaFilePaths(sc.hadoopConfiguration, driverLog, sivaFilesPath)
    if (cli.sivaFileLimit() > 0) {
      sivaFiles = sivaFiles.take(Math.max(cli.sivaFileLimit(), sivaFiles.length))
    }

    val actualNumWorkers = Math.min(cli.numberPartitions(), sivaFiles.length)
    val remoteSivaFiles = sc.parallelize(sivaFiles, actualNumWorkers)
    driverLog.info(s"Processing ${sivaFiles.length} .siva files in $actualNumWorkers partitions")

    // copy from HDFS and un-pack in tmp dir using go-siva (on Workers)
    val unpacked = remoteSivaFiles
      .map(sivaFile => {
        FsUtils.copyFromHDFS(confBroadcast.value.value, sivaFile)
      })
      .pipe("./siva-unpack-mock") //RDD["sivaFile sivaUnpackDir"]

    // iterate every un-packed .siva
    val intermediatePerFile = unpacked
      .mapPartitions(partition => {
        //TODO(bzz): start enrysrv process using binary from SparkFiles
        val enryService = EnryService(enryHost, enryPort, grpcMaxMsgSize)
        val bblfshService = BblfshClient(bblfshHost, bblfshPort, grpcMaxMsgSize)

        partition
          .flatMap { sivaFileNameAndUnpackedDir =>
            val log = Logger.getLogger(s"Stage: process single repo '$sivaFileNameAndUnpackedDir'")
            val (sivaFileName, sivaUnpackDir) = split(sivaFileNameAndUnpackedDir)

            log.info(s"Processing repository in $sivaUnpackDir")
            //TODO(bzz): wrap repo processing logic in `try {} catch {}`

            JGitFileIterator(sivaUnpackDir, sivaFileName, confBroadcast.value.value)
          }
          .filter { case (_, treeWalk, _) =>
            treeWalk.getFileMode(0) == FileMode.REGULAR_FILE || treeWalk.getFileMode(0) == FileMode.EXECUTABLE_FILE
          }
          .map { case (initHash, treeWalk, ref) =>
            val path = treeWalk.getPathString
            //TODO(bzz): skip big well-known binaries .apk and .jar

            //detect language using enry server
            var content = Array.emptyByteArray
            var guessed = enryService.getLanguage(path)
            if (guessed.status == Status.NEED_CONTENT) {
              content = RootedRepo.readFile(treeWalk.getObjectId(0), treeWalk.getObjectReader)
              guessed = enryService.getLanguage(path, content)
            }
            (initHash, treeWalk, ref, path, content, guessed)
          }
          .filter { case (_,_,_,_,_, guessed) =>
            guessed.language.equalsIgnoreCase("python") || guessed.language.equalsIgnoreCase("java")
          }
          .flatMap { case (initHash, treeWalk, ref, path, cachedContent, guessed) =>
            val log = Logger.getLogger(getClass.getName)
            val content = readIfNotCached(treeWalk, cachedContent)

            val parsed = bblfshService.parse(path, content, guessed.language)
            log.info(s"Parsed $path - size:${content.length} bytes, status:${parsed.status}")

            val row = if (parsed.errors.isEmpty) {
              Seq(Row(initHash,
                ref.getObjectId.name,           //commit_hash
                treeWalk.getObjectId(0).name,   //blob_hash
                ref.getName.split('/').last,    //repository_id
                "",                             //repository_url (?from unpackDir/config)
                ref.getName.substring(0, ref.getName.lastIndexOf('/')), //reference
                true,                           //is_main_repository
                path, guessed.language,
                parsed.uast.get.toByteArray))
            } else {
              log.info(s"Parsed $path - errors:${parsed.errors}")
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