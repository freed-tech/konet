# konet

A Scala library for building clients against external vendor HTTP APIs. It wraps OkHttp with JSON serialization and deserialization, a typed response model, per-client thread pools configured through HOCON, and pluggable request logging. Dependency injection is handled with Guice.

Package: `care.freed.integration`

## Details

| | |
|---|---|
| Group ID | `com.github.freed-tech` |
| Artifact ID | `konet` |
| Version | `1.0.2` |
| Scala | 2.12.10 |
| sbt | 1.3.3 |
| License | Proprietary (internal) |

The artifact is built without a Scala version suffix (`crossPaths := false`), so it is referenced with a single `%` in sbt.

## Installation

konet is published to GitHub Packages. Reading from the registry requires a GitHub token with the `read:packages` scope.

```scala
resolvers += "GitHub Packages" at "https://maven.pkg.github.com/freed-tech/konet"

credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "<github-username>",
  sys.env("GITHUB_TOKEN")
)

libraryDependencies += "com.github.freed-tech" % "konet" % "1.0.2"
```

## Concepts

**VendorClient.** Abstract base class. Each vendor integration extends it and provides a client name, default headers, and connection settings. It exposes protected `get`, `post`, `getSync`, and `postSync` methods that return a `NetworkResponse[T]`.

**RequestData.** Abstract base class for POST bodies. Subclasses are serialized to JSON with `toJson` and must define `cid`, an optional correlation id passed to the logger.

**NetworkResponse[T].** A sealed trait with these cases:

| Case | Meaning |
|---|---|
| `NetworkSuccess(data, code)` | 2xx response, body deserialized into `T` |
| `ApiError(code, message)` | Non-2xx response; `message` is the response body |
| `NetworkError(ex)` | The request failed before a response was received |
| `DeserializationError(code, reason, rawResponse)` | 2xx response whose body could not be deserialized into `T` |
| `AuthFailure(ex)` | Defined for use by callers; konet does not produce it itself |

`NetworkResponse` also provides `map`, `exception`, and `toTry`.

**WebLogger.** Trait with `logNetworkCallStart` and `logNetworkCallEnd`. `logNetworkCallStart` returns a reference string that is passed back to `logNetworkCallEnd`. A default implementation, `care.freed.integration.defaults.FileLogger`, writes both calls to standard output with `println`.

**ConnectionParams.** Connect, read, and write timeouts in seconds. All default to 30.

## Setup

konet requires a Guice binding for `WebLogger` and an eagerly created `InjectorLoader`. `InjectorLoader` hands the injector to the internal client registry, which uses it to build clients. If it is never instantiated, the first request fails.

```scala
import com.google.inject.AbstractModule
import care.freed.integration.{InjectorLoader, WebLogger}
import care.freed.integration.defaults.FileLogger

class IntegrationModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[WebLogger]).to(classOf[FileLogger])
    bind(classOf[InjectorLoader]).asEagerSingleton()
  }
}
```

Replace `FileLogger` with your own `WebLogger` implementation to route logs elsewhere.

## Writing a client

```scala
import care.freed.integration.VendorClient
import care.freed.integration.data.{ConnectionParams, NetworkResponse, RequestData}
import scala.concurrent.Future

case class CreateOrderRequest(item: String, quantity: Int, cid: Option[String]) extends RequestData
case class CreateOrderResponse(orderId: String, status: Option[String])

class OrderVendorClient extends VendorClient {
  override protected val clientName: String = "order-vendor"

  override protected def buildHeaders: Map[String, String] =
    Map("Authorization" -> s"Bearer $token")

  override protected def webClientConfig: ConnectionParams =
    ConnectionParams(connectionTimeout = 10, readTimeout = 20, writeTimeout = 20)

  def createOrder(request: CreateOrderRequest): Future[NetworkResponse[CreateOrderResponse]] =
    post[CreateOrderResponse]("create-order", s"$baseUrl/orders", request)

  def getOrder(id: String): NetworkResponse[CreateOrderResponse] =
    getSync[CreateOrderResponse]("get-order", s"$baseUrl/orders/$id")
}
```

The first argument to each request method (`simpleName`) is a short label for the call and is passed to the logger. Every request method accepts an optional `headers` argument that replaces the result of `buildHeaders` for that call.

`buildHeaders` must not call `get`, `post`, or the sync variants, since those call `buildHeaders` internally.

## Behavior to be aware of

**JSON mapping.** Jackson is configured with the Scala module. Any field that is not an `Option` is treated as required, and a missing required field produces a `DeserializationError`. Fields typed `Option` may be absent and become `None`. Unknown properties in a response are ignored. A field annotated with `@JsonProperty(required = true)` is always required.

**Empty response bodies.** A successful response with an empty body is replaced with `{}` before deserialization.

**Request bodies.** `RequestData.toJson` serializes the whole object, so a `cid` declared as a case class field is included in the request body.

**Client reuse.** One `WebClient` is created per `clientName` and cached for the life of the JVM. The `ConnectionParams` and thread pool from the first request are the ones that stay in use.

## Thread pool configuration

Each client runs its asynchronous calls on a dedicated fixed-size thread pool. The pool is configured from `conf/application.conf`, read from the process working directory (not the classpath).

The dispatcher key is the client name, lowercased with whitespace removed, followed by `-dispatcher`. For the example above that is `order-vendor-dispatcher`. If that key is absent, the library uses `common-client-dispatcher`. If neither exists, it uses one thread per available processor.

```hocon
order-vendor-dispatcher {
  fixed-pool-size = 8
  min-threads = 2
  max-threads = 16
}

common-client-dispatcher {
  parallelism-factor = 2.0
}
```

| Key | Description |
|---|---|
| `fixed-pool-size` | Exact thread count. Takes precedence over `parallelism-factor`. |
| `parallelism-factor` | Thread count as a multiple of available processors, rounded. |
| `min-threads` | Lower bound on the thread count. Defaults to 1. |
| `max-threads` | Upper bound on the thread count. Defaults to unbounded. |

Synchronous methods (`getSync`, `postSync`) run on the calling thread and do not use the pool.

## Building

```
sbt compile
sbt package
```

`package` produces the binary jar along with source and Javadoc jars. The repository does not currently contain tests.

## Publishing

Publishing targets `https://maven.pkg.github.com/freed-tech/konet`. The build reads the token from the `GITHUB_TOKEN` environment variable, which must have the `write:packages` scope.

```
export GITHUB_TOKEN=<token>
sbt publish
```

Update `version` in `build.sbt` before publishing a new release. GitHub Packages does not allow overwriting an existing version.

## Contact

Freed Tech Team, techsupport@freed.care