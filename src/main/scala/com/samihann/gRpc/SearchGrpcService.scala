package com.samihann.gRpc

/***
 * Written by Samihan Nandedkar
 * CS441
 * Fall 2021
 *
 * Defines the task executed by gRPC server when made request to.
 *
 */

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import com.samihann.gRpc.LogSearch.{LogSearchGrpc, LogSearchProto, SearchRequest, SearchResponse}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.*
import org.apache.http.*
import org.apache.http.client.*
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import com.google.gson.Gson
import com.samihann.Utility.CreateLogger
import org.apache.http.client.config.RequestConfig
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

import scala.concurrent.Future
import scala.io.Source
import org.slf4j.{Logger, LoggerFactory}

// Defining the service class.
class SearchGrpcService(searchResponse: SearchResponse) extends LogSearchGrpc.LogSearch {
  val config: Config = ConfigFactory.load("configuration")

  val logger = CreateLogger(classOf[SearchGrpcService])
  // Override the default search function defined by protobuf.
  logger.info("Executing Service class.")
  override def search(request: SearchRequest): Future[SearchResponse] = {
    // Received time and time_duration from Client.
    val time = request.time
    val pattern = request.pattern
    val date = request.date
    val time_duration = request.deltaTime
    logger.info(s"Executing the search function for the input values -> time: $time, deltaTime = $time_duration")

    // Performing a Get request to AWS Lamda API Gateway by passing the values through query parameters.
    logger.info("Performing the GET request to AWS Lamda function.")
    val url = config.getString("configuration.lambdaApiUrl")

    val newUrl = s"$url?time=$time&date=$date&time_duration=$time_duration&pattern=$pattern"

    // add name value pairs to a post object
    val timeout = 1800
    val requestConfig = RequestConfig.custom()
      .setConnectTimeout(timeout * 1000)
      .setConnectionRequestTimeout(timeout * 1000)
      .setSocketTimeout(timeout * 1000).build()

    val client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()
    val get: HttpGet = new HttpGet(newUrl)

    logger.info(s"GET request is made to: $newUrl ")
    val response: CloseableHttpResponse = client.execute(get)
    val entity = response.getEntity
    val str = EntityUtils.toString(entity, "UTF-8")
    //val responseJsonFromAWSAPIGateway = scala.io.Source.fromURL(s"$url?time=$time&date=$date&time_duration=$time_duration&pattern=$pattern").mkString
    //Sending back the received response to Client
    logger.info(s"Sending back the receive response: $str")
    Future.successful(SearchResponse(str))
  }
}

