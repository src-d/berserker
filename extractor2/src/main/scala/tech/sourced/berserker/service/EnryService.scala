package tech.sourced.berserker.service

import github.com.srcd.berserker.enrysrv.generated.{EnryRequest, EnrysrvServiceGrpc, Status}
import io.grpc.ManagedChannelBuilder
import org.apache.log4j.Logger


class EnryService(host: String, port: Int, maxMsgSize: Int) {

  private val log = Logger.getLogger(getClass.getName)

  private val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .maxInboundMessageSize(maxMsgSize)
    .build()

  private val stub = EnrysrvServiceGrpc.blockingStub(channel)

  def getLanguage(filename: String): (String, Status)= {
    log.info(s"Detecting lang for $filename")
    val lang = stub.getLanguage(EnryRequest(fileName = filename))
    log.info(s"Detected filename: $filename, lang: ${lang.language}, status: ${lang.status}")
    (lang.language, lang.status)
  }


}

object EnryService {
  def apply(host: String, port: Int, maxMsgSize: Int): EnryService =
    new EnryService(host, port, maxMsgSize)
}
