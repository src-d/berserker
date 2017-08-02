package tech.sourced.berserker

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import tech.sourced.berserker.spark.Utils

import scala.collection.mutable

object FsUtils {

  val thisStage = "Stage: copy .siva files"
  val sivaFilesNamePrefix = "siva"

  /**
    * Deletes given path recursively.
    * Takes precaution not to be `rm -rf /`:
    *   - ONLY delete things under jvm tmp dir
    *   - ONLY delete things that were created by FsUtils (name includes `sivaFilesNamePrefix`)
    *
    * @param hadoopConf FS Configuration to use (S3, HDFS, GCS, etc)
    * @param path       a path to be deleted
    */
  def rm(hadoopConf: Configuration, path: String) = {
    val log = Logger.getLogger(FsUtils.thisStage)
    log.info(s"Was asked to delete $path")

    val fs = FileSystem.get(hadoopConf)
    val dst = new Path(path)

    val jvmTempPath = System.getProperty("java.io.tmpdir")
    val implSpecificPrefix = s"${FsUtils.sivaFilesNamePrefix}-"
    if (path.contains(jvmTempPath) && dst.getName().contains(implSpecificPrefix)) {
      //path.startsWith(jvmTempPath) can not be used on some OSes
      fs.delete(dst, true)
      log.info(s"$dst deleted ")
    } else {
      log.info(s"Skip $dst without deleting anything as it looks like it was not created by us. " +
        s"Either not under jvmTempPath:'$jvmTempPath' or name:'${dst.getName()}' doesn't start with '$implSpecificPrefix'")
    }
  }

  /**
    * Copies given file from remote FS to a temp path in local FS.
    * Returns string, suitable for using as input for `./siva-unpack`
    *
    * @param hadoopConf     FS Configuration to use (S3, HDFS, GCS, etc)
    * @param remoteSivaFile path to single .siva file
    * @return result, suitable for stdio input as for `./siva-unpack < result`
    */
  def copyFromHDFS(hadoopConf: Configuration, remoteSivaFile: String): String = {
    val localUnpackDir = Utils.createTempDir(namePrefix = FsUtils.sivaFilesNamePrefix).getCanonicalPath
    val sivaFilename = copyFromHDFS(hadoopConf, remoteSivaFile, localUnpackDir)

    val unpackArgs = s"$localUnpackDir/$sivaFilename $localUnpackDir"
    unpackArgs
  }

  def copyFromHDFS(hadoopConf: Configuration, sivaFile: String, toLocalPath: String): String = {
    val log = Logger.getLogger(FsUtils.thisStage)
    log.info(s"Copying 1 file from: $sivaFile to: $toLocalPath")

    val fs = FileSystem.get(new URI(sivaFile), hadoopConf)
    val src = new Path(sivaFile)
    val dst = new Path(toLocalPath)
    fs.copyToLocalFile(src, dst)

    val sivaFilename = sivaFile.split('/').last
    log.info(s"$sivaFilename copied")
    sivaFilename
  }

  def collectSivaFilePaths(hadoopConfig: Configuration, log: Logger, sivaFilesPath: Path) = {
    log.info(s"Listing all *.siva files in $sivaFilesPath")
    val sivaFilesIterator = FileSystem.get(new URI(sivaFilesPath.toString), hadoopConfig).listFiles(sivaFilesPath, false)
    val sivaFiles: mutable.ArrayBuffer[String] = mutable.ArrayBuffer()
    while (sivaFilesIterator.hasNext) {
      sivaFiles.append(sivaFilesIterator.next().getPath().toString)
    }
    log.info(s"Done, ${sivaFiles.length} .siva files found under $sivaFilesPath")
    sivaFiles
  }

}
