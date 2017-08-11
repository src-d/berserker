package tech.sourced.berserker.service

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import tech.sourced.enry.Enry

class EnryServiceTest extends FunSuite with BeforeAndAfterAll {

  test("can call libEnry through JNI") {
    val resp = Enry.getLanguageByFilename("python.py")

    assert(resp != null)
    assert(resp.language.equalsIgnoreCase("python"))
  }

}
