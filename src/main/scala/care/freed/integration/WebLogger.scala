package care.freed.integration

import care.freed.integration.data.NetworkResponse

trait WebLogger {
  def logNetworkCallStart(clientName: String, simpleName: String, url: String, requestData: Option[String], cid: Option[String]): String
  def logNetworkCallEnd(networkResponse: NetworkResponse[_], requestReference: String): Unit
}
