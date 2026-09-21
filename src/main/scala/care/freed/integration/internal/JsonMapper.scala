package care.freed.integration.internal

import com.fasterxml.jackson.databind.{AnnotationIntrospector, DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JsonMapper {
  val mapper: ObjectMapper = {
    val objectMapper = new ObjectMapper()
      .registerModule(DefaultScalaModule)

    val combinedIntrospector = AnnotationIntrospector.pair(
      new ScalaOptionRequiredAnnotationIntrospector,
      objectMapper.getDeserializationConfig.getAnnotationIntrospector
    )
    objectMapper
      .setAnnotationIntrospector(combinedIntrospector)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      // This is set to false on purpose to ensure that in case of missing Option[_] properties, jackson doesn't
      // break since it doesn't understand that Option is a special data type
      .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
  }

  def toJson(obj: AnyRef): String = mapper.writeValueAsString(obj)
}
