package tech.sourced.berserker

import java.io.File
import java.util

import org.apache.log4j.Logger
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk


object RootedRepo {
  val thisStage = "Stage:JGitFileIteration"

  def gitTree(dotGit: String) = {
    val log = Logger.getLogger(thisStage)
    log.info(s"Reading bare .git repository from $dotGit")

    val repository = new FileRepositoryBuilder()
      .readEnvironment()
      .setGitDir(new File(dotGit))
      .setMustExist(true)
      .setBare()
      .build()

    val git = new Git(repository)
    val revWalk = new RevWalk(git.getRepository)

    val noneForkfOrigHeadRef = findNoneForkOrigRepoHeadRef(git.getRepository.getAllRefs())

    val objectId = noneForkfOrigHeadRef.getObjectId
    val revCommit = revWalk.parseCommit(objectId)
    revWalk.close()

    val treeWalk = new TreeWalk(git.getRepository)
    treeWalk.setRecursive(true)
    treeWalk.addTree(revCommit.getTree)
    log.info(s"Walking a tree of $dotGit repository at ${noneForkfOrigHeadRef.getName}")

    treeWalk
  }

  def findNoneForkOrigRepoHeadRef(allRefs: util.Map[String, Ref]): Ref = {
    val log = Logger.getLogger(thisStage)
    log.info(s"${allRefs.size()} refs found. Picking a ref to head of original none-fork repo")

    // TODO(bzz): pick ref to non-fork orig repo HEAD
    //  right now we do not have orig HEAD refs in RootedRepos, AKA https://github.com/src-d/borges/issues/116
    //    so we always pick 'refs/heads/master' instead
    //  right now `is_fork` is not in RootedRepo config AKA https://github.com/src-d/borges/issues/117
    //    so we sort all refs and always pick first one (will be: filtered by is_fork + heuristics)

    //TODO(bzz):
    // sort allRefs
    // pick first one
    val origNoneForkHead = allRefs.get(allRefs.keySet().iterator().next())

    log.info(s"Done. ${origNoneForkHead} ref picked")
    return origNoneForkHead
  }

}
