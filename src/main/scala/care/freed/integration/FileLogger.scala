package care.freed.integration

import java.util.UUID

class FileLogger extends WebLogger {

  override def logNetworkCallStart(clientName: String, simpleName: String, url: String, requestData: Option[String], cid: Option[String]): String = {
    println(s"$clientName | $simpleName | $url | $requestData | $cid")
    UUID.randomUUID().toString
  }

  override def logNetworkCallEnd(networkResponse: NetworkResponse[_], requestReference: String): Unit = {
    println(s"$networkResponse | $requestReference")
  }
}
