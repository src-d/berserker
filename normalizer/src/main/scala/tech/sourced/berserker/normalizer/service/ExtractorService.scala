package tech.sourced.berserker.normalizer.service

import github.com.srcd.berserker.extractor.generated.{ExtractorServiceGrpc, Request}
import io.grpc.ManagedChannelBuilder
import org.apache.spark.sql.Row

class ExtractorService(host: String, port: Int, isPlainText: Boolean = true) {

  private val maxGrpcMsgSize = 100 * 1042 * 1042
  private val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(isPlainText)
    .maxInboundMessageSize(maxGrpcMsgSize)
    .build()

  private val stub = ExtractorServiceGrpc.blockingStub(channel)

  def getRepositoriesData(repoIds: Seq[String]): Seq[Row] = {
    val reply = stub.serviceGetRepositoriesData(Request(repoIds))

    reply.result1.flatMap(rd => {
      for {
        file <- rd.files
        repoId = rd.repositoryId
        repoUrl = rd.url
      } yield {
        Row(repoId, repoUrl,
          Option(file.hash)
            .orNull
            .toByteArray
            .map("%02x" format _)
            .mkString,
          file.path, file.language, file.uast.toByteArray)
      }
    })
  }
}

object ExtractorService {
  def apply(host: String, port: Int, isPlainText: Boolean = true): ExtractorService =
    new ExtractorService(host, port, isPlainText)
}