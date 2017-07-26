package tech.sourced.berserker


import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.eclipse.jgit.lib.FileMode
import org.eclipse.jgit.treewalk.TreeWalk
import tech.sourced.berserker.spark.SerializableConfiguration

import scala.collection.mutable
import scala.util.Properties


object SparkDriver {

  //pre-requests (going to be automated in subsequent PRs)
  // copy a .siva file(s) i.e from Staging cluster
  //  kubectl cp borges-consumer-3831673438-h9zv7:/borges/root-repositories/ffb696c97d8c2fdf52bdaf9f637658d2df5e16fc.siva ffb696c97d8c2fdf52bdaf9f637658d2df5e16fc.siva
  // make sure go-siva is installed
  //  go get -u github.com/bzz/go-siva/...

  //run
  // ./sbt assembly
  // java -jar target/scala-2.11/berserker-assembly-1.0.jar <path-to-new-unpack-dir>

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sparkMaster = Properties.envOrElse("MASTER", "local[*]")
    val numWorkers = 4

    if (args.length < 1) {
      println("Mandatory CLI argument is missing: path to dir with .siva files")
      System.exit(1)
    }

    val spark = SparkSession.builder()
      .config(conf)
      .appName("berserker")
      .master(sparkMaster)
      .getOrCreate()
    val sc = spark.sparkContext
    val driverLog = Logger.getLogger(getClass.getName)

    val confBroadcast = sc.broadcast(new SerializableConfiguration(sc.hadoopConfiguration))

    // list .siva files (on Driver)
    val sivaFilesPath = new Path(args(0))
    val sivaFiles = collectSivaFilePaths(sc.hadoopConfiguration, driverLog, sivaFilesPath)

    val actualNumWorkers = Math.min(numWorkers, sivaFiles.length)
    val remoteSivaFiles = sc.parallelize(sivaFiles, actualNumWorkers)
    driverLog.info(s"Processing ${sivaFiles.length} .siva files in $actualNumWorkers partitions")

    // copy from HDFS and un-pack in tmp dir using go-siva (on Workers)
    val unpacked = remoteSivaFiles
      .map(sivaFile => {
        FsUtils.copyFromHDFS(confBroadcast.value.value, sivaFile)
      })
      .pipe("./siva-unpack-mock") //RDD["sivaUnpackDir"]

    // iterate every un-packed .siva
    val intermediatePerFile = unpacked
      .map { sivaUnpackedDir =>
        val log = Logger.getLogger("Stage: process single repo")
        log.info(s"Processing repository in $sivaUnpackedDir")

        // iterate every file using JGit
        val treeWalk: TreeWalk = RootedRepo.gitTree(sivaUnpackedDir)
        while (treeWalk.next()) {
          val mode = treeWalk.getFileMode(0)
          if (mode == FileMode.REGULAR_FILE || mode == FileMode.EXECUTABLE_FILE) {
            val path = treeWalk.getPathString
            println(s"$path")
          }
        }

        //TODO(bzz): detect language using enry-server
        //TODO(bzz): parse to UAST using bblfsh/server

        log.info(s"Cleaning up .siva and unpacked Siva from dir: $sivaUnpackedDir")
        FsUtils.rm(confBroadcast.value.value, sivaUnpackedDir)
        treeWalk.close()
      }

    intermediatePerFile.collect().foreach(println)

    //TODO(bzz) produce tables: files, UAST and repositories from intermediatePerFile

    //TODO(bzz) de-duplicate/repartition final tables

    //TODO(bzz): print counters - performance accumulators, errors
  }

  def collectSivaFilePaths(hadoopConfig: Configuration, log: Logger, sivaFilesPath: Path) = {
    log.info(s"Listing all *.siva files in $sivaFilesPath")
    val sivaFilesIterator = FileSystem.get(hadoopConfig).listFiles(sivaFilesPath, false)
    val sivaFiles: mutable.ArrayBuffer[String] = mutable.ArrayBuffer()
    while (sivaFilesIterator.hasNext) {
      sivaFiles.append(sivaFilesIterator.next().getPath().toString)
    }
    log.info(s"Done, ${sivaFiles.length} .siva files found under $sivaFilesPath")
    sivaFiles
  }


}