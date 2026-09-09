package care.freed.integration.data

import care.freed.integration.internal.JsonMapper

abstract class RequestData {
  def toJson: String = JsonMapper.toJson(this)
  def cid: Option[String]
}
