package tech.sourced.berserker

import java.io.File

import org.apache.hadoop.fs.Path
import org.apache.log4j.Logger
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{Row, SparkSession}
import org.eclipse.jgit.lib.FileMode
import org.eclipse.jgit.treewalk.TreeWalk
import tech.sourced.berserker.git.{JGitFileIterator, RootedRepo}
import tech.sourced.berserker.model.Schema
import tech.sourced.berserker.spark.{MapAccumulator, SerializableConfiguration, Utils}
import tech.sourced.enry.{Enry, Guess}
import tech.sourced.siva.SivaUnpacker

import scala.util.Properties

object ExtractReposLangs {

  def main(args: Array[String]): Unit = {
    val log = Logger.getLogger(getClass.getName)

    val sparkMaster = Properties.envOrElse("MASTER", "local[*]")
    val sivaFilesPath = if (args.length > 0) { args(0) } else "hdfs://hdfs-namenode/borges/root-repositories/"
    val sivaFileLimit = if (args.length > 1) { args(1).toInt } else 10
    val numPartitions = if (args.length > 2) { args(2).toInt } else 3
    val outputPath = if (args.length > 3) { args(3) } else "lang.files"
    log.info(s"Processing up to $sivaFileLimit .siva files from $sivaFilesPath " +
      s"using $numPartitions partitions and saving results to $outputPath")

    val spark = SparkSession.builder()
      .config(new SparkConf())
      .appName("extract-language-per-file")
      .master(sparkMaster)
      .getOrCreate()
    val sc = spark.sparkContext
    val skippedFiles = mapAccumulator(sc, "skipped files")
    val skippedRepos = mapAccumulator(sc, "skipped repos")

    // list .siva files (on Driver)
    val confBroadcast = sc.broadcast(new SerializableConfiguration(sc.hadoopConfiguration))
    var sivaFiles = FsUtils.collectSivaFilePaths(sc.hadoopConfiguration, log, new Path(sivaFilesPath))
    if (sivaFileLimit > 0) {
      sivaFiles = sivaFiles.take(Math.min(sivaFileLimit, sivaFiles.length))
    }

    val actualNumWorkers = Math.min(numPartitions, sivaFiles.length)
    val remoteSivaFiles = sc.parallelize(sivaFiles, actualNumWorkers)
    log.info(s"Actual processing: ${sivaFiles.length} .siva files in $actualNumWorkers partitions")

    // copy from HDFS and un-pack in tmp dir using siva-java (on Workers)
    val unpacked = remoteSivaFiles
      .flatMap { sivaFile =>
        copyFromHDFS(sivaFile, confBroadcast, Some(skippedRepos))
      }
      .flatMap { case (sivaFile, localUnpackDir) =>
        sivaUnpack(sivaFile, localUnpackDir, confBroadcast, Some(skippedRepos))
      }

    // iterate every un-packed .siva
    val intermediatePerFile = unpacked
      .flatMap { case (sivaFileName, sivaUnpackDir) =>
        val log = Logger.getLogger(s"Stage: process single repo")
        log.info(s"Processing repository in $sivaUnpackDir")
        JGitFileIterator(sivaUnpackDir, sivaFileName, confBroadcast.value.value, skippedRepos)
      }
      .filter { case (_, tree, _, _) =>
        tree.getFileMode(0) == FileMode.REGULAR_FILE || tree.getFileMode(0) == FileMode.EXECUTABLE_FILE
        //TODO(bzz): skip big well-known binaries .apk and .jar
      }
      .flatMap { case (initHash, tree, ref, config) =>
        val (langName, langBytes) = guessLang(tree, Some(skippedFiles))

        val repoUUID = ref.getName.split('/').last
        val repoIsFork = config.getString("remote", repoUUID, "isfork")
        val repoUrls = config.getStringList("remote", repoUUID, "url")
        val mainRepoUrl = if (repoUrls.isEmpty) "" else repoUrls.head

        Seq(Row(mainRepoUrl, repoIsFork.toBoolean, initHash, tree.getPathString, langName, langBytes))
      }

    val intermediatePerFileDF = spark.sqlContext.createDataFrame(intermediatePerFile, Schema.filesLang)
    intermediatePerFileDF.write
      .mode("overwrite")
      .parquet(outputPath)

    log.info(s"Parquet saved.")
    log.info(s"\tRepos skipped: ${skippedRepos.value.size}")
    skippedRepos.value foreach { case (key, value) => log.info(s"\t\t$key --> $value") }

    log.info(s"\tFiles skipped: ${skippedFiles.value.size}")
    skippedFiles.value foreach { case (key, value) => log.info(s"\t\t$key --> $value") }

    val parquetAllDF = spark.read
      .parquet(outputPath)
      .show(10)
  }

  def copyFromHDFS(sivaFile: String, conf: Broadcast[SerializableConfiguration], skippedRepos: Option[MapAccumulator] = None) = {
    val localUnpackDir = Utils.createTempDir(namePrefix = FsUtils.sivaFilesNamePrefix).getCanonicalPath
    try {
      val localSivaFile = FsUtils.copyFromHDFS(conf.value.value, sivaFile, localUnpackDir)
      Seq(localSivaFile)
    } catch {
      case e: Exception => Logger
        .getLogger(s"Failed to copy .siva file: ")
        .error(s"${e.getClass.getSimpleName} skipping repo ${sivaFile}", e)

      //local fs cleaned-up
      FsUtils.rm(conf.value.value, localUnpackDir)
      skippedRepos.foreach(_.add(e.getClass.getSimpleName -> 1))
      Seq()
    }
  }

  def sivaUnpack(sivaFile: String, localUnpackDir: String, conf: Broadcast[SerializableConfiguration], skippedRepos: Option[MapAccumulator] = None) = {
    try {
      new SivaUnpacker(new File(s"$localUnpackDir/$sivaFile").getAbsolutePath).unpack(localUnpackDir)
      val fileNameInitCommit = sivaFile.substring(0, sivaFile.lastIndexOf('.'))
      Seq((fileNameInitCommit, localUnpackDir))
    } catch {
      case e: Exception => Logger
        .getLogger(s"Failed to unpack .siva file: ")
        .error(s"${e.getClass.getSimpleName} skipping repo ${sivaFile}", e)

      //local fs cleaned-up
      FsUtils.rm(conf.value.value, localUnpackDir)
      skippedRepos.foreach(_.add(e.getClass.getSimpleName -> 1))
      Seq()
    }
  }

  def guessLang(tree: TreeWalk, skippedFiles: Option[MapAccumulator] = None): (String, Int) = {
    var content = Array.emptyByteArray
    val path = tree.getPathString

    var guessed = Enry.getLanguageByFilename(path)
    if (!guessed.safe) {
      content = try {
        RootedRepo.readFile(tree.getObjectId(0), tree.getObjectReader)
      } catch {
        case e: Exception => Logger
          .getLogger(s"Failed to detect language: ")
          .error(s"${e.getClass.getSimpleName} skipping file ${tree.getPathString}", e)
        skippedFiles.foreach(_.add(e.getClass.getSimpleName -> 1))
        Array.emptyByteArray
      }

      guessed = if (content.isEmpty) {
        Enry.unknownLanguage
      } else {
        new Guess(Enry.getLanguage(path, content), true)
      }
    }
    (guessed.language, content.length)
  }

  def mapAccumulator(sc: SparkContext, name: String): MapAccumulator = {
    val acc = new MapAccumulator
    sc.register(acc, name)
    acc
  }

}
