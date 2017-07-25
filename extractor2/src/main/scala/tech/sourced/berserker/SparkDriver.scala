package tech.sourced.berserker


//import org.apache.hadoop.fs.{FileSystem, Path}

import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.eclipse.jgit.lib.FileMode
import org.eclipse.jgit.treewalk.TreeWalk

import scala.util.Properties


object SparkDriver {

  //pre-requests (going to be automated in subsequent PRs)
  // copy a .siva file(s) i.e from Staging cluster
  //  kubectl cp borges-consumer-3831673438-h9zv7:/borges/root-repositories/ffb696c97d8c2fdf52bdaf9f637658d2df5e16fc.siva ffb696c97d8c2fdf52bdaf9f637658d2df5e16fc.siva
  // make sure go-siva is installed
  //  go get -u github.com/bzz/go-siva/...
  // un-pack it to local FS
  //  siva unpack - < ffb696c97d8c2fdf52bdaf9f637658d2df5e16fc.siva <path-to-new-unpack-dir>

  //run
  // ./sbt assembly
  // java -jar target/scala-2.11/berserker-assembly-1.0.jar <path-to-new-unpack-dir>

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val sparkMaster = Properties.envOrElse("MASTER", "local[*]")

    val spark = SparkSession.builder()
      .config(conf)
      .appName("berserker")
      .master(sparkMaster)
      .getOrCreate()
    val sc = spark.sparkContext

    //TODO(bzz): iterate all *.siva files in HDFS
    /*println(s"Reading all *.siva files in $sivaFilePaths")
    val sivaFilePaths = new Path(args(0))
    val sivaFiles = FileSystem.get(sc.hadoopConfiguration).listFiles(sivaFilePaths, false)
    */

    // path to *.siva files `file://` or `hdfs://`
    val sivaUnpackedFiles = args(0)

    val driverLog = Logger.getLogger(getClass.getName)
    driverLog.info(s"Processing all .siva files in $sivaUnpackedFiles")

    //TODO(bzz): copy .siva files from HDFS, unpack using `go-siva`

    // iterate every un-packed .siva
    val intermediatePerFile = sc.parallelize(Seq(sivaUnpackedFiles), 1)
      .map { sivaUnpacked =>
        val log = Logger.getLogger("Stage: process single repo")
        log.info(s"Processing repository in $sivaUnpacked")

        val treeWalk: TreeWalk = RootedRepo.gitTree(sivaUnpacked)
        while (treeWalk.next()) { // iterate every file using JGit
            val mode = treeWalk.getFileMode(0)
            if(mode == FileMode.REGULAR_FILE || mode == FileMode.EXECUTABLE_FILE) {
              val path = treeWalk.getPathString
              println(s"$path")
            }
        }

        //TODO(bzz): detect language using enry-server
        //TODO(bzz): parse to UAST using bblfsh/server

        //TODO(bzz): cleanup .siva and unpacked files
        log.info(s"Cleaning up .siva file/unpackSiva dir for $sivaUnpacked")
        treeWalk.close()
      }

    intermediatePerFile.collect().foreach(println)

    //TODO(bzz) produce tables: files, UAST and repositories from intermediatePerFile

    //TODO(bzz) de-duplicate/repartition final tables

    //TODO(bzz): print counters - performance accumulators, errors

  }

}