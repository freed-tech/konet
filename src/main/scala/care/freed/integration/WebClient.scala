package care.freed.integration

import com.google.inject.{Inject, Singleton}
import okhttp3.{Headers, MediaType, OkHttpClient, Request, RequestBody, Response}
import okio.Buffer

import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class WebClient(logger: WebLogger, connectionParams: ConnectionParams, clientName: String, ec: ExecutionContext) {
  private val webClient = new OkHttpClient.Builder()
    .readTimeout(connectionParams.readTimeout, TimeUnit.SECONDS)
    .writeTimeout(connectionParams.writeTimeout, TimeUnit.SECONDS)
    .connectTimeout(connectionParams.connectionTimeout, TimeUnit.SECONDS)
    .build()

  implicit val executionContext: ExecutionContext = ec // Added here to prevent the need for passing the ec everywhere

  private val JSON = MediaType.get("application/json")
  def get(simpleName: String, url: String, headerMap: Map[String, String]): Future[NetworkResponse[String]] = {
    val headers = Headers.of(headerMap.asJava)
    val request = new Request.Builder()
      .url(url)
      .headers(headers)
      .get()
      .build()

    Future(executeRequest(request, simpleName, None))
  }

  def getSync(simpleName: String, url: String, headerMap: Map[String, String]): NetworkResponse[String] = {
    val headers = Headers.of(headerMap.asJava)
    val request = new Request.Builder()
      .url(url)
      .headers(headers)
      .get()
      .build()

    executeRequest(request, simpleName, None)
  }

  def post(simpleName: String, url: String, requestData: RequestData, headerMap: Map[String, String]): Future[NetworkResponse[String]] = {
    val headers = Headers.of(headerMap.asJava)

    val requestBodyString = requestData.toJson
    val requestBody = RequestBody.create(JSON, requestBodyString)
    val request = new Request.Builder()
      .url(url)
      .headers(headers)
      .post(requestBody)
      .build()

    Future(executeRequest(request, simpleName, requestData.cid))
  }

  def postSync(simpleName: String, url: String, requestData: RequestData, headerMap: Map[String, String]): NetworkResponse[String] = {
    val headers = Headers.of(headerMap.asJava)

    val requestBodyString = requestData.toJson
    val requestBody = RequestBody.create(JSON, requestBodyString)
    val request = new Request.Builder()
      .url(url)
      .headers(headers)
      .post(requestBody)
      .build()

    executeRequest(request, simpleName, requestData.cid)
  }

  private def executeRequest(request: Request, simpleName: String, cid: Option[String]): NetworkResponse[String] = {
    val maybeRequestBodyString = Option(request.body()).map(requestBodyToString)
    val loggerReference = logger.logNetworkCallStart(clientName, simpleName, request.url().toString, maybeRequestBodyString, cid)
    val maybeResponse = Try(webClient.newCall(request).execute())
    val networkResponse = maybeResponse match {
      case Failure(exception) =>
        NetworkError(exception)
      case Success(response) =>
        val ret = processResponse(response)
        response.close()
        ret
    }
    logger.logNetworkCallEnd(networkResponse, loggerReference)
    networkResponse
  }

  private def requestBodyToString(requestBody: RequestBody): String = {
    try {
      val buffer = new Buffer()
      requestBody.writeTo(buffer)
      buffer.readUtf8()
    } catch {
      case _: Throwable =>
        ""
    }
  }

  def processResponse(response: Response): NetworkResponse[String] = {
    val responseBody = response.body().string()
    if (!response.isSuccessful) {
      return ApiError(code = response.code(), message = responseBody)
    }

    // If the response body is empty, or not returned at all, then return an empty json. This is a hack for now
    // because jackson is unable to handle empty strings, so this will allow it to serialise the string
    val sanitisedResponseBody = if (responseBody == null || responseBody.isEmpty) {
      "{}"
    } else {
      responseBody
    }
    NetworkSuccess(sanitisedResponseBody, code = response.code())
  }
}

case class ConnectionParams(connectionTimeout: Int = 30, readTimeout: Int = 30, writeTimeout: Int = 30)

@Singleton
class WebClientFactory @Inject()(logger: WebLogger) {
  def create(connectionParams: ConnectionParams, clientName: String, ec: ExecutionContext): WebClient = {
    new WebClient(logger, connectionParams, clientName, ec)
  }
}