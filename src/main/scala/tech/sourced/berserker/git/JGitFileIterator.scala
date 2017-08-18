package tech.sourced.berserker.git

import org.apache.hadoop.conf.Configuration
import org.apache.log4j.Logger
import org.eclipse.jgit.lib.{Config, Ref}
import org.eclipse.jgit.treewalk.TreeWalk
import tech.sourced.berserker.FsUtils

/**
  * Iterates every file in given bare repo (dotGit dir).
  * Deletes dotGit dir from filesystem at the end of iteration.
  *
  * @param sivaUnpackedDir
  * @param initHash
  * @param hadoopConf
  */
class JGitFileIterator(sivaUnpackedDir: String, initHash: String, hadoopConf: Configuration)
    extends Iterator[(String, TreeWalk, Ref, Config)] {
    private val (treeWalk, ref, config) = RootedRepo.gitTree(sivaUnpackedDir)
    private val log = Logger.getLogger("JGitIterator")
    private var wasAdvanced = false

    override def hasNext: Boolean = {
      if (wasAdvanced == true) {
        log.debug(s"JGitIterator:hasNext() == true, WITHOUT advancing, for $initHash.siva")
        return true
      }
      val result = if (treeWalk.next()) {
        true
      } else {
        treeWalk.close()
        FsUtils.rm(hadoopConf, sivaUnpackedDir)
        log.info(s"Cleaned up $initHash.siva and unpacked repo from: $sivaUnpackedDir")
        false
      }
      log.debug(s"JGitIterator:hasNext() == $result, advanced:$wasAdvanced for $initHash.siva")
      wasAdvanced = true
      result
    }
    override def next(): (String, TreeWalk, Ref, Config) = {
      log.debug(s"JGitIterator:next() advanced:$wasAdvanced, for $initHash.siva")
      if (!wasAdvanced) {
        treeWalk.next()
      }
      wasAdvanced = false
      (initHash, treeWalk, ref, config)
    }
    // can not skip detecting lang for whole `./vendor/*` if `guessed.status == Status.IGNORED`
  }

  object JGitFileIterator {
    def apply(sivaUnpackedDir: String, sivaFileName: String, hadoopConf: Configuration): JGitFileIterator =
      new JGitFileIterator(sivaUnpackedDir, sivaFileName, hadoopConf)
  }
