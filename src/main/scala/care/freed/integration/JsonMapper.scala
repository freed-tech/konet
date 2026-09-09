package care.freed.integration

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JsonMapper {
  val mapper: ObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)

  def toJson(obj: AnyRef): String = mapper.writeValueAsString(obj)
}
