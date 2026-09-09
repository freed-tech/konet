package care.freed.integration

abstract class RequestData {
  def toJson: String = JsonMapper.toJson(this)
  def cid: Option[String]
}
