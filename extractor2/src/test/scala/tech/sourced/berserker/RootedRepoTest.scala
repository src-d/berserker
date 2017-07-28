package tech.sourced.berserker

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import tech.sourced.berserker.git.RootedRepo


class RootedRepoTest extends FunSuite with BeforeAndAfter {

  test("findHeadRefUsingHeuristics picks first element") {
    // given
    val refs = Seq("refs/heads/master/a","refs/heads/master/b")

    // when
    val origRef = RootedRepo.findHeadRefUsingHeuristics(refs)

    //then
    assert(origRef == "refs/heads/master/a")
  }

}
