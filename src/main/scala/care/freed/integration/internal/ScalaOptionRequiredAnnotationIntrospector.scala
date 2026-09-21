package care.freed.integration.internal

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.introspect.{AnnotatedMember, NopAnnotationIntrospector}

import java.lang

// This class is required to make data types behave properly. With this class, any data type that is not Option[_] will
// be treated as compulsory. To make something nullable, make it Option
class ScalaOptionRequiredAnnotationIntrospector extends NopAnnotationIntrospector {
  override def hasRequiredMarker(m: AnnotatedMember): lang.Boolean = {
    val prop = m.getAnnotation(classOf[JsonProperty])

    // 1. Only return TRUE if explicitly annotated with @JsonProperty(required = true)
    if (prop != null && prop.required()) {
      java.lang.Boolean.TRUE
    }
    // 2. Option[_] fields are NOT required (missing key in JSON -> Option defaults to None)
    else if (classOf[Option[_]].isAssignableFrom(m.getRawType)) {
      java.lang.Boolean.FALSE
    }
    // 3. All non-Option fields ARE required (missing key in JSON -> throws exception)
    else {
      java.lang.Boolean.TRUE
    }
  }
}
