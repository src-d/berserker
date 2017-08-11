package tech.sourced.berserker

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import tech.sourced.enry.Enry

class EnryTest extends FunSuite with BeforeAndAfterAll {

  test("can call libEnry through JNI") {
    val resp = Enry.getLanguageByFilename("pom.xml")
    println(s"$resp - lang:${resp.language}, safe:${resp.safe}")

    assert(resp != null)
    assert(resp.language.contains("Maven"))
  }

}
