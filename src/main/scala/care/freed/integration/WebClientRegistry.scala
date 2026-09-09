package care.freed.integration

import com.google.inject.{Inject, Injector}

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext

object WebClientRegistry {
  private var injector: Injector = _

  private val registry: TrieMap[String, WebClient] = TrieMap.empty

  def setInjector(injector: Injector): Unit = {
    this.injector = injector
  }

  def getOrCreateClient(clientName: String, connectionParams: ConnectionParams, ec: ExecutionContext): WebClient = {
    registry.getOrElseUpdate(clientName, injector.getInstance(classOf[WebClientFactory]).create(connectionParams, clientName, ec))
  }
}
