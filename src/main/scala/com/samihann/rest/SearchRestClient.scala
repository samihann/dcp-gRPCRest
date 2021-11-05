package com.samihann.rest

/***
 * Written by Samihan Nandedkar
 * CS441
 * Fall 2021
 *
 * Client program to make request to AWS Lamda API through POST and GET.
 */

import org.apache.commons.*
import org.apache.http.*
import org.apache.http.client.*
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import com.google.gson.Gson
import org.apache.http.client.config.RequestConfig
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import com.samihann.Utility.{CreateLogger, SearchClass}

import scala.io.{Source, StdIn}
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}

// Client for making rest request to AWS Lambda function.
class SearchRestClient

object SearchRestClient extends App {
    val config: Config = ConfigFactory.load("configuration")
    val logger = CreateLogger(classOf[SearchRestClient])

  /***
   * Requirement for var
   *
   * To keep track of the input from the user and to keep the function running a while loop is used
   * var stop is used to exit from the loop whenever the user request it.
   */
    var stop = false
    logger.info("The SearchRestClient has started execution.")
    // Provide input option to user to make POST or GET request.
    logger.info("Taking input from USER")

    try {
      while (!stop) {
        println("Choose one of the following:")
        println(" 1 - POST Request")
        println(" 2 - GET Request")
        println(" q - Quit")
        StdIn.readChar() match {
          case 'q' => {
            logger.info("User has choosen Option q")
            stop = true
          }
          case '1' => {
            logger.info("User has choosen Option 1")
            postRequest
          }
          case '2' => {
            logger.info("User has choosen Option 2")
            getRequest
          }
          case _ => ()
        }
      }
    }
    catch{
      case _ => ()
    }

    // Function to make post request to user.
    def postRequest: Unit = { // create our object as a json string
      logger.info("Started execution of postRequest function")
      val time = config.getString("configuration.datetime")
      val time_duration = config.getInt("configuration.deltaTime")
      val pattern = config.getString("configuration.pattern")
      val url = config.getString("configuration.lambdaApiUrl")
      logger.info("Got parameters from configuratuion")
      val payload = new SearchClass(time, time_duration, pattern)
      val payloadAsJson = new Gson().toJson(payload)


      // add name value pairs to a post object
      val timeout = 1800
      val requestConfig = RequestConfig.custom()
        .setConnectTimeout(timeout * 1000)
        .setConnectionRequestTimeout(timeout * 1000)
        .setSocketTimeout(timeout * 1000).build()

      val client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()

      val post: HttpPost = new HttpPost(url)
      post.addHeader("Content-Type", "application/json")
      post.setEntity(new StringEntity(payloadAsJson))

      logger.info("making the post request.")
      val response: CloseableHttpResponse = client.execute(post)
      val entity = response.getEntity
      val str = EntityUtils.toString(entity, "UTF-8")
      logger.info(s"Post request is made to: $url, With following payload: $payloadAsJson")
      logger.info("Response is " + str)
      println(s"Post request is made to: $url  \n With following payload: $payloadAsJson")
      println("\nResponse is " + str + "\n\n")
    }

    // function to make get request to AWS Lambda function.
    def getRequest: Unit = {
      logger.info("Started the execution of getRequest function.")
      val time = config.getString("configuration.time")
      val date = config.getString("configuration.date")
      val pattern = config.getString("configuration.pattern")
      val time_duration = config.getInt("configuration.deltaTime")
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

      logger.info("Response is " + str)
      println(s"GET request is made to: $newUrl")
      println("\nResponse is " + str+ "\n\n")

    }

}
