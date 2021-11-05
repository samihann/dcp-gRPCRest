package com.samihann.gRpc

/***
 * Written by Samihan Nandedkar
 * CS441
 * Fall 2021
 *
 * This file defines the gRPC client for the Log Search Task
 */

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import com.samihann.gRpc.LogSearch.{LogSearchGrpc, LogSearchProto, SearchRequest, SearchResponse}
import com.samihann.gRpc.SearchGrpcService

import com.samihann.Utility.CreateLogger
import java.util.concurrent.{CountDownLatch, TimeUnit}
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.duration.*
import scala.util.Random


class SearchGrpcClient(host: String, port: Int) {
  val config: Config = ConfigFactory.load("configuration")
  val logger = CreateLogger(classOf[SearchGrpcClient])
  // Create a channel for the RPC requests
  logger.info("Creating channel")
  val channel =
    ManagedChannelBuilder
      .forAddress(host, port)
      .usePlaintext()
      .build()

  // Defining blocking stub for the RPC calls.
  val blockingStub = LogSearchGrpc.blockingStub(channel)
  logger.info("Create a blocking stub by passing the channel created to LogSearchGrpc.")

  def shutdown(): Unit = channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)

  import io.grpc.StatusRuntimeException

  // The reach fuction to send request to server and await for the response of the same.
  def search(time: String, date: String, deltaTime: String, pattern: String): Unit = {
    // Create request.
    val request = SearchRequest(time, date, deltaTime, pattern)
    try {
      // Sending request to server.
      logger.info("Send the gRPC request to the server.")
      val searchResponse = blockingStub.search(request)
      // Prining the response received.
      logger.info(s"The reponse received from Server: ${searchResponse.result}")
      println("Response Received: ")
      println(searchResponse.result)
    } catch {
      // Catching any error
      case e: StatusRuntimeException =>
        logger.error(s"RPC failed with:${e.getStatus}")
        println(s"RPC failed:${e.getStatus}")
    }

  }}


  // Main Client object.
  object SearchGrpcClient extends App {
    val logger = CreateLogger(classOf[SearchGrpcClient])
    logger.info("Starting the gRPC client.")

    // Load the values from config
    val config: Config = ConfigFactory.load("configuration")

    // Define instance of SearchGrpcClient class.
    // Client will run on localhost with port 8980

    val client = new SearchGrpcClient(config.getString("configuration.clientHost"), config.getInt("configuration.clientPort"))
    logger.info("Defined an instance of SearchGrpcClient")

    //Call the search function of the above defined class.
    try {
      client.search(config.getString("configuration.time"), config.getString("configuration.date"), config.getString("configuration.deltaTime"), config.getString("configuration.pattern"))
    }

//    // Shutdown the Client server.
//    logger.info("Shutting down the Client Server");
    finally client.shutdown()
  }