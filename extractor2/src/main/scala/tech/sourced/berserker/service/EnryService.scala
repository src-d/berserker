package tech.sourced.berserker.service

import com.google.protobuf.ByteString
import github.com.srcd.berserker.enrysrv.generated.{EnryRequest, EnrysrvServiceGrpc}
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

  def getLanguage(filename: String, content: Array[Byte] = Array.emptyByteArray) = {
    val req = if (content.isEmpty) {
      log.info(s"Detecting lang for $filename")
      EnryRequest(fileName = filename)
    } else {
      log.info(s"Detecting lang for $filename by content")
      EnryRequest(fileName = filename, fileContent = ByteString.copyFrom(content))
    }
    val guess = stub.getLanguage(req)
    log.info(s"Detected filename: $filename, lang: ${guess.language}, status: ${guess.status}")
    guess
  }

}

object EnryService {
  def apply(host: String, port: Int, maxMsgSize: Int): EnryService =
    new EnryService(host, port, maxMsgSize)
}
