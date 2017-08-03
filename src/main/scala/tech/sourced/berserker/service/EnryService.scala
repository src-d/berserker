package tech.sourced.berserker.service

import com.google.protobuf.ByteString
import github.com.srcd.berserker.enrysrv.generated.{EnryRequest, EnryResponse, EnrysrvServiceGrpc}
import io.grpc.ManagedChannelBuilder
import org.apache.log4j.Logger

import scala.sys.process._


class EnryService(host: String, port: Int, maxMsgSize: Int) {

  private val log = Logger.getLogger(getClass.getName)

  private val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .maxInboundMessageSize(maxMsgSize)
    .build()

  private val stub = EnrysrvServiceGrpc.blockingStub(channel)

  def getLanguage(filename: String, content: Array[Byte] = Array.emptyByteArray): EnryResponse = {
    val req = if (content.isEmpty) {
      log.debug(s"Detecting lang for $filename")
      EnryRequest(fileName = filename)
    } else {
      log.debug(s"Detecting lang for $filename by content")
      EnryRequest(fileName = filename, fileContent = ByteString.copyFrom(content))
    }
    val guess = stub.getLanguage(req)
    log.info(s"Detected filename: $filename, lang: ${guess.language}, status: ${guess.status}")
    guess
  }

}

/**
  * Process lifecycle management below is kludgy.
  * It all can be removed, if Enry server is treated the same way as Bblfsh server
  * (I.e in cluster - managed by k8s)
  */
object EnryService {

  private val psCommand = "ps ax" #| "grep [e]nrysrv"
  private val killCommand = "kill -9"

  def startProcess(enrysrv: String) = {
    val log = Logger.getLogger(getClass.getName)
    log.info(s"Starting Enry server process using $enrysrv")
    val command = s"$enrysrv server"
    val out = command.lineStream
    val line = out.take(1) //block, until first line of STDIO
    log.info(s"Done. Enry server started with $line")
    new Thread(s"stdout reader for $command") {
      override def run() = {
        try {
          for (line <- out) {
            log.info(line)
          }
        } catch {
          case t: Throwable => log.error(s"Exception running Enry server: $t")
        }
      }
    }.start()
  }

  def stopProcess() = {
    val log = Logger.getLogger(getClass.getName)
    log.info("Stop all Enry server processed")
    val (running, psOut) = processIsRunning()
    if (running) {
      val pidOpt = "\\d+".r.findFirstIn(psOut)
      pidOpt.foreach { pid =>
        log.info(s"Killing enry server process $pid")
        s"$killCommand $pid".run
        log.info(s"Done. Enry server process $pid killed")
      }
    }

  }

  def processIsRunning(): (Boolean, String) = {
    val log = Logger.getLogger(getClass.getName)
    var running = false
    var out = ""
    try {
      out = (psCommand !!)
      log.info(s"Enry is running with $out")
      running = true
    } catch { //non-zero exit code
      case _: Throwable => running = false
    }
    log.info(s"Enry process is running? $running")
    (running, out)
  }

  def processIsNotRunning(): Boolean = {
    val (running, _) = processIsRunning()
    !running
  }

  def apply(host: String, port: Int, maxMsgSize: Int): EnryService =
    new EnryService(host, port, maxMsgSize)

}
