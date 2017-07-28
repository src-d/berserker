package tech.sourced.berserker.service

import java.io.File

import github.com.srcd.berserker.enrysrv.generated.{EnryRequest, EnrysrvServiceGrpc, Status}
import io.grpc.ManagedChannelBuilder
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.sys.process._
import scala.util.Random

class EnryServiceTest extends FunSuite with BeforeAndAfterAll {
  var testPort: Int = _
  var enrySrvProc: Process = _

  override def beforeAll() = {
    testPort = 1024 + Random.nextInt( (65535 - 1024) + 1 )
    var enrysrv = "../enrysrv2/bin/enrysrv"
    if (!new File(enrysrv).exists()) { //unitTests
      enrysrv = "./enrysrv2/bin/enrysrv"
    }
    enrySrvProc = s"$enrysrv server -a 0.0.0.0:$testPort".run
  }

  override def afterAll() = {
    enrySrvProc.destroy()
  }

  test("can connect to Enry gRPC server") {
    val channel = ManagedChannelBuilder.forAddress("0.0.0.0", testPort).usePlaintext(true).build()
    val stub = EnrysrvServiceGrpc.blockingStub(channel)
    val resp = stub.getLanguage(EnryRequest(fileName = "python.py"))

    assert(resp != null)
    assert(resp.status == Status.OK)
    assert(resp.language.equalsIgnoreCase("python"))
  }

}
