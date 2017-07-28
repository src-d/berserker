package org.bblfsh.client

import github.com.bblfsh.sdk.protocol.generated.{Encoding, ParseRequest, ProtocolServiceGrpc}
import io.grpc.ManagedChannelBuilder


class BblfshClient(host: String, port: Int, maxMsgSize: Int) {

  private val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .maxInboundMessageSize(maxMsgSize)
    .build()

  private val stub = ProtocolServiceGrpc.blockingStub(channel)

  def parse(name: String, content: Array[Byte], lang: String = "", encoding: Encoding = Encoding.UTF8) = {
    //TODO(bzz): assume content is already encoded?
    val encodedContent = new String(content, "utf-8")
    val req = ParseRequest(filename = name, content = encodedContent, language = lang.toLowerCase, encoding = encoding)
    val parsed = stub.parse(req)
    parsed
  }
}

object BblfshClient {
  val DEFAULT_MAX_MSG_SIZE = 100 * 1024 * 1024
  def apply(host: String, port: Int, maxMsgSize: Int = DEFAULT_MAX_MSG_SIZE): BblfshClient =
    new BblfshClient(host, port, maxMsgSize)
}

