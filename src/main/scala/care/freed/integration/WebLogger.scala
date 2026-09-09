package care.freed.integration

trait WebLogger {
  def logNetworkCallStart(clientName: String, simpleName: String, url: String, requestData: Option[String], cid: Option[String]): String
  def logNetworkCallEnd(networkResponse: NetworkResponse[_], requestReference: String): Unit
}
