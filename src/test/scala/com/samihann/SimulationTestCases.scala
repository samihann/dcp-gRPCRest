package com.samihann

import org.scalatest.funsuite.AnyFunSuite
import com.samihann.gRpc.LogSearch.{LogSearchGrpc, LogSearchProto, SearchRequest, SearchResponse}
import com.samihann.gRpc.*
import com.samihann.rest.SearchRestClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import com.samihann.Utility.SearchClass

object SimulationTestCases extends AnyFunSuite {

  test("Test if the gRPC Request class is being initiated. (SearchRequest)") {
    val request1 = new SearchRequest("0:0:0.000","2021-11-04","1","aaa")
    assert(request1.isInstanceOf[SearchRequest])
  }

  test("Test if the gRPC Response class is being initiated. (SearchResponse)") {
    val request2 = new SearchResponse("Result")
    assert(request2.isInstanceOf[SearchResponse])
  }

  test("Test to see if SearchClass is getting initiated") {
    val request3 = new SearchClass("0:0:0.000",1,"aaa")
    assert(request3.isInstanceOf[SearchClass])
  }

  test("Test to check if GET request is done successfully.") {
    val timeout = 1800
    val requestConfig = RequestConfig.custom()
      .setConnectTimeout(timeout * 1000)
      .setConnectionRequestTimeout(timeout * 1000)
      .setSocketTimeout(timeout * 1000).build()
    val newUrl = "https://k5fwq6d2d2.execute-api.us-east-2.amazonaws.com/default/SampleLamda?time=12:59:55.923&date=2021-11-04&time_duration=0&pattern=([a-c][e-g][0-3][A-Z][5-9][f-w])"
    val client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()
    val get: HttpGet = new HttpGet(newUrl)

    val response: CloseableHttpResponse = client.execute(get)
    val entity = response.getEntity
    val str = EntityUtils.toString(entity, "UTF-8")
    assert(!str.isEmpty)
  }

  test("Test to check gRPC service returns a Response"){
    val request1 = new SearchRequest("0:0:0.000","2021-11-04","1","aaa")
    val a = new SearchGrpcService(new SearchResponse)
    val b = a.search(request1)



  }






}
