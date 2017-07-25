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

  def gitTree(sivaUnpacked: String) = {
    val log = Logger.getLogger("Stage:JGitFileIteration")

    val repository = new FileRepositoryBuilder()
      .readEnvironment()
      .setGitDir(new File(sivaUnpacked))
      .setBare()
      .build()

    val git = new Git(repository)
    val revWalk = new RevWalk(git.getRepository)

    val noneForkfOrigHeadRef = findNoneForkOrigRepoHeadRef(git.getRepository.getAllRefs())

    val objectId = noneForkfOrigHeadRef.getObjectId //git.getRepository.resolve(noneForkfOrigHeadRef)
    val revCommit = revWalk.parseCommit(objectId)
    revWalk.close()

    val treeWalk = new TreeWalk(git.getRepository)
    treeWalk.setRecursive(true)
    treeWalk.addTree(revCommit.getTree)
    log.info(s"Walking a tree of $sivaUnpacked repository at ${noneForkfOrigHeadRef.getName}")

    treeWalk
  }

  def findNoneForkOrigRepoHeadRef(allRefs: util.Map[String, Ref]): Ref = {
    // TODO(bzz): pick ref to non-fork orig repo HEAD
    //  right now we do not have orig HEAD refs in RootedRepos, AKA https://github.com/src-d/borges/issues/116
    //    so we always pick 'refs/heads/master' instead
    //  right now `is_fork` is not in RootedRepo config AKA https://github.com/src-d/borges/issues/117
    //    so we sort all refs and always pick first one (will be: filtered by is_fork + heuristics)

    //TODO(bzz):
    // sort allRefs
    // pick first one
    return allRefs.get(allRefs.keySet().iterator().next())
  }

}
