package care.freed.integration

import com.google.inject.{Inject, Injector, Singleton}

@Singleton
class InjectorLoader @Inject()(injector: Injector) {
  WebClientRegistry.setInjector(injector)
}
