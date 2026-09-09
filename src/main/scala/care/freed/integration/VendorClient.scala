package care.freed.integration

import care.freed.integration.data.{ApiError, AuthFailure, ConnectionParams, DeserializationError, NetworkError, NetworkResponse, NetworkSuccess, RequestData}
import care.freed.integration.internal.{HoconExecutionContextFactory, JsonMapper, WebClient, WebClientRegistry}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class VendorClient {

  /**
   * Must be in kebab-case
   */
  protected val clientName: String

  private lazy val executionContext: ExecutionContext = {
    val dispatcherName = s"${clientName.toLowerCase.replaceAll("\\s+", "")}-dispatcher"
    HoconExecutionContextFactory.create(dispatcherName)
  }

  /**
   * Makes a get request to the URL. Uses the headers received from `getHeaders` method to generate the headers.
   *
   * @param url : The URL to get
   * @tparam T : The return object
   * @return Eventual network response, deserialized into T
   */
  protected final def get[T <: AnyRef: ClassTag](simpleName: String, url: String, headers: Option[Map[String, String]] = None): Future[NetworkResponse[T]] = {
    val rawResponse = webClient.get(simpleName, url, headers.getOrElse(buildHeaders))
    rawResponse.map(tryParse[T])(executionContext)
  }

  protected final def post[T <: AnyRef: ClassTag](simpleName: String, url: String, requestData: RequestData, headers: Option[Map[String, String]] = None): Future[NetworkResponse[T]] = {
    val rawResponse = webClient.post(simpleName, url, requestData, headers.getOrElse(buildHeaders))
    rawResponse.map(tryParse[T])(executionContext)
  }

  protected final def getSync[T <: AnyRef: ClassTag](simpleName: String, url: String, headers: Option[Map[String, String]] = None): NetworkResponse[T] = {
    val rawResponse = webClient.getSync(simpleName, url, headers.getOrElse(buildHeaders))
    tryParse[T](rawResponse)
  }

  protected final def postSync[T <: AnyRef: ClassTag](simpleName: String, url: String, requestData: RequestData, headers: Option[Map[String, String]] = None): NetworkResponse[T] = {
    val rawResponse = webClient.postSync(simpleName, url, requestData, headers.getOrElse(buildHeaders))
    tryParse[T](rawResponse)
  }

  private def webClient: WebClient = {
    WebClientRegistry.getOrCreateClient(clientName, webClientConfig, executionContext)
  }

  private def tryParse[T <: AnyRef: ClassTag](networkResponse: NetworkResponse[String]): NetworkResponse[T] = {
    networkResponse match {
      case NetworkSuccess(jsonData, code) =>
        try {
          val mapper = JsonMapper.mapper
          val clazz = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
          val deserializedObject = mapper.readValue(jsonData, clazz)
          NetworkSuccess(deserializedObject, code)
        } catch {
          case e: Exception =>
            DeserializationError(code, e.getMessage, jsonData)
        }
      case failure: AuthFailure => failure
      case failure: DeserializationError => failure
      case failure: NetworkError => failure
      case failure: ApiError => failure
    }
  }

  /**
   * Called whenever making a get/post request with the default headers.
   *
   * WARNING: This method must not call the get/post default headers method itself since they will internally call this
   * causing an infinite loop.
   *
   * @return A map of key value pairs representing the headers to be sent with the request
   */
  protected def buildHeaders: Map[String, String]

  protected def webClientConfig: ConnectionParams
}
