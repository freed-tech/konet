package care.freed.integration

import com.google.inject.Inject

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext

object WebClientRegistry {
  @Inject
  private var webClientFactory: WebClientFactory = _ // This has been made var so that Guice can inject the instance in it

  private val registry: TrieMap[String, WebClient] = TrieMap.empty

  def getOrCreateClient(clientName: String, connectionParams: ConnectionParams, ec: ExecutionContext): WebClient = {
    registry.getOrElseUpdate(clientName, webClientFactory.create(connectionParams, clientName, ec))
  }
}
