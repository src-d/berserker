package tech.sourced.berserker.normalizer.service

import com.google.protobuf
import github.com.srcd.berserker.extractor.generated.{ExtractorServiceGrpc, Request, Service_GetRepositoriesDataRequest}
import io.grpc.ManagedChannelBuilder
import org.apache.spark.sql.Row

import scalapb.descriptors.ScalaType.ByteString

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
        repoUrl = rd.url
      } yield {
        Row(repoId,
          repoUrl,
          Option(file.hash)
            .orNull
            .toByteArray
            .map("%02x" format _)
            .mkString, file.path, file.language, file.uast)
      }
    })
  }

  // TODO finish
  //  def getRepositoryData(
  //                         repositoryId: String,
  //                         rootCommitHash: String,
  //                         referenceName: String): Unit = {
  //    val reply = stub.serviceGetRepositoryData(
  //      Request(
  //        repositoryId,
  //        rootCommitHash.asInstanceOf[protobuf.ByteString],
  //        referenceName
  //      )
  //    )
  //  }
}

object ExtractorService {
  def apply(host: String, port: Int, isPlainText: Boolean = true): ExtractorService =
    new ExtractorService(host, port, isPlainText)
}