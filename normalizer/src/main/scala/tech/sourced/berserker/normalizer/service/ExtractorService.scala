package tech.sourced.berserker.normalizer.service

import github.com.srcd.berserker.extractor.generated.{ExtractorServiceGrpc, Service_GetRepositoriesDataRequest}
import io.grpc.ManagedChannelBuilder
import org.apache.spark.sql.Row

class ExtractorService(host: String, port: Int, isPlainText: Boolean = true) {
  private val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(isPlainText).build()

  private val stub = ExtractorServiceGrpc.blockingStub(channel)

  def getRepositoriesData: Seq[Row] = {
    val reply = stub.serviceGetRepositoriesData(Service_GetRepositoriesDataRequest())

    reply.result1.flatMap(rd => {
      for {
        file <- rd.files
        repoId = rd.repositoryId
      } yield {
        Row(repoId,
          Option(file.hash)
            .orNull
            .toByteArray
            .map("%02x" format _)
            .mkString, file.path, file.language, file.ast)
      }
    })
  }
}

object ExtractorService {
  def apply(host: String, port: Int, isPlainText: Boolean = true): ExtractorService =
    new ExtractorService(host, port, isPlainText)
}