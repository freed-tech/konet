package care.freed.integration

import care.freed.integration.internal.WebClientRegistry
import com.google.inject.{Inject, Injector, Singleton}

@Singleton
class InjectorLoader @Inject()(injector: Injector) {
  WebClientRegistry.setInjector(injector)
}
