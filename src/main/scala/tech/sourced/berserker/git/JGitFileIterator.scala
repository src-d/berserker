package tech.sourced.berserker.git

import org.apache.hadoop.conf.Configuration
import org.apache.log4j.Logger
import org.apache.spark.util.LongAccumulator
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
  * @param skippedRepos: Accumulator to count number of skipped repositories due to JGit exceptions
  */
class JGitFileIterator(sivaUnpackedDir: String, initHash: String, hadoopConf: Configuration, skippedRepos: Option[LongAccumulator])
    extends Iterator[(String, TreeWalk, Ref, Config)] {
    private val (treeWalkOpt, ref, config) = RootedRepo.gitTree(sivaUnpackedDir, skippedRepos)
    private val log = Logger.getLogger("JGitIterator")
    private var wasAdvanced = false

    override def hasNext: Boolean = {
      if (!treeWalkOpt.isDefined) {
        return false
      }

      if (wasAdvanced) {
        log.debug(s"JGitIterator:hasNext() == true, WITHOUT advancing, for $initHash.siva")
        return true
      }

      treeWalkOpt.exists { treeWalk =>
        val hasNext = treeWalk.next()
        wasAdvanced = true
        if (!hasNext) {
          treeWalk.close()
          FsUtils.rm(hadoopConf, sivaUnpackedDir)
          log.info(s"Cleaned up $initHash.siva and unpacked repo from: $sivaUnpackedDir")
        }
        hasNext
      }
    }

    override def next(): (String, TreeWalk, Ref, Config) = {
      log.debug(s"JGitIterator:next() advanced:$wasAdvanced, for $initHash.siva")
      if (!wasAdvanced) {
        treeWalkOpt.foreach(_.next())
      }
      wasAdvanced = false
      (initHash, treeWalkOpt.get, ref, config)
    }
    // can not skip detecting lang for whole `./vendor/*` if `guessed.status == Status.IGNORED`
  }

  object JGitFileIterator {
    def apply(sivaUnpackedDir: String, sivaFileName: String, hadoopConf: Configuration, skippedRepos: LongAccumulator): JGitFileIterator =
      new JGitFileIterator(sivaUnpackedDir, sivaFileName, hadoopConf, Some(skippedRepos))

    def apply(sivaUnpackedDir: String, sivaFileName: String, hadoopConf: Configuration): JGitFileIterator =
      new JGitFileIterator(sivaUnpackedDir, sivaFileName, hadoopConf, None)

  }
