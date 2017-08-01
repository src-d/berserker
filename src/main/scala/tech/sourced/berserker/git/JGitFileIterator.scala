package tech.sourced.berserker.git

import org.apache.hadoop.conf.Configuration
import org.apache.log4j.Logger
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.treewalk.TreeWalk
import tech.sourced.berserker.FsUtils

/**
  * Iterates every file in given repo dotGit dir.
  * Deletes dotGit dir from filesystem at the end of iteration
  *
  * @param sivaUnpackedDir
  * @param sivaFileName
  * @param hadoopConf
  */
class JGitFileIterator(sivaUnpackedDir: String, sivaFileName: String, hadoopConf: Configuration)
    extends Iterator[(String, TreeWalk, Ref)] {
    private val (treeWalk, ref) = RootedRepo.gitTree(sivaUnpackedDir)
    private val log = Logger.getLogger("JGitIterator")
    private var wasAdvanced = false

    override def hasNext: Boolean = {
      if (wasAdvanced == true) {
        log.debug(s"JGitIterator:hasNext() == true, WITHOUT advancing, for $sivaFileName")
        return true
      }
      val result = if (treeWalk.next()) {
        true
      } else {
        treeWalk.close()
        FsUtils.rm(hadoopConf, sivaUnpackedDir)
        log.info(s"Cleaned up $sivaFileName.siva and unpacked repo from: $sivaUnpackedDir")
        false
      }
      log.debug(s"JGitIterator:hasNext() == $result, advanced:$wasAdvanced for $sivaFileName")
      wasAdvanced = true
      result
    }
    override def next() = {
      log.debug(s"JGitIterator:next() advanced:$wasAdvanced, for $sivaFileName")
      if (!wasAdvanced) {
        treeWalk.next()
      }
      wasAdvanced = false
      (sivaFileName, treeWalk, ref)
    }
    // can not skip detecting lang for whole `./vendor/*` if `guessed.status == Status.IGNORED`
  }

  object JGitFileIterator {
    def apply(sivaUnpackedDir: String, sivaFileName: String, hadoopConf: Configuration): JGitFileIterator =
      new JGitFileIterator(sivaUnpackedDir, sivaFileName, hadoopConf)
  }
