package care.freed.integration

import com.google.inject.AbstractModule

class WebClientModule extends AbstractModule {
  override def configure(): Unit = {
    // Registers the object singleton for static injection
    requestStaticInjection(WebClientRegistry.getClass)
  }
}