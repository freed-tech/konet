package care.freed.integration.internal

import com.typesafe.config.{Config, ConfigFactory}

import java.io.File
import java.util.concurrent.{ExecutorService, LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

private[integration] object HoconExecutionContextFactory {
  def create(dispatcherPath: String, fallbackPath: String = "common-client-dispatcher"): ExecutionContextExecutorService = {
    val config = ConfigFactory.parseFile(new File("/conf/application.conf"))
    val targetPath = if (config.hasPath(dispatcherPath)) dispatcherPath else fallbackPath

    val dispatcherConfig = if (config.hasPath(targetPath)) {
      config.getConfig(targetPath)
    } else {
      ConfigFactory.empty()
    }

    val executor = createExecutorService(dispatcherConfig)
    ExecutionContext.fromExecutorService(executor)
  }

  private def createExecutorService(config: Config): ExecutorService = {
    val cores = Runtime.getRuntime.availableProcessors()

    val rawThreads = if (config.hasPath("fixed-pool-size")) {
      config.getInt("fixed-pool-size")
    } else if (config.hasPath("parallelism-factor")) {
      val factor = config.getDouble("parallelism-factor")
      math.round(cores * factor).toInt
    } else {
      cores
    }

    val minThreads = if (config.hasPath("min-threads")) config.getInt("min-threads") else 1
    val maxThreads = if (config.hasPath("max-threads")) config.getInt("max-threads") else Int.MaxValue

    val boundedThreads = math.max(minThreads, math.min(maxThreads, rawThreads))

    new ThreadPoolExecutor(boundedThreads, boundedThreads, 60, TimeUnit.SECONDS, new LinkedBlockingQueue[Runnable]())
  }
}