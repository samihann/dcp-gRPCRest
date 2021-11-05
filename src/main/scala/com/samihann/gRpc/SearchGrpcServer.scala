package com.samihann.gRpc

/***
 * Written by Samihan Nandedkar
 * CS441
 * Fall 2021
 *
 * gRPC server.
 */

import com.samihann.gRpc.LogSearch.{LogSearchGrpc, LogSearchProto, SearchRequest, SearchResponse}
import com.google.api.Http
import com.samihann.Utility.CreateLogger
import io.grpc.stub.StreamObserver
import io.grpc.{Server, ServerBuilder}

import scala.io.Source
import scala.concurrent.ExecutionContext
import org.slf4j.{Logger, LoggerFactory}
import com.typesafe.config.{Config, ConfigFactory}


// Defining Server class for gRPC
class SearchGrpcServer(server: Server) {
  val config: Config = ConfigFactory.load("configuration")
  val logger = CreateLogger(classOf[SearchGrpcServer])
  // Server start function.
  def start(): Unit = {
    server.start()
    println(s"Server started, listening on ${server.getPort}")
    logger.info(s"Server started, listening on ${server.getPort}")
    sys.addShutdownHook {
      // Use stderr here since the logger may has been reset by its JVM shutdown hook.
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      logger.info(s"Server shutting down")
      stop()
      System.err.println("*** server shut down")
    }
    ()
  }

  // Defininf the server stop function.
  def stop(): Unit = {
    server.shutdown()
  }

  def blockUntilShutdown(): Unit = {
    server.awaitTermination()
  }
}

// The main Server object which will be executed when the server is run.
object SearchGrpcServer extends App {
  val logger = CreateLogger(classOf[SearchGrpcServer])
  // Create an instance of Server class defined above.
  logger.info("Created an instance of SearchGrpcServer class with passing all the parameters")
  val server = new SearchGrpcServer(
    ServerBuilder
      .forPort(8980)
      .addService(
        LogSearchGrpc.bindService(
          new SearchGrpcService(new SearchResponse),
          scala.concurrent.ExecutionContext.global
        )
      )
      .build()
  )

  // Starting the server
  logger.info("Starting the server")
  server.start()
  // This will keep the serve alive until it is killed by passing the input from keyboard

  server.blockUntilShutdown()
  logger.info("Shutting down the server once interupt is received from keyboard")

}