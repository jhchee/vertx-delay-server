package github.jhchee.delayserver

import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(this::class.java)

  override fun start(startFuture: Promise<Void>?) {
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)
    router.get("/delay/ms/:milliseconds").coroutineHandler { ctx ->
      val milliseconds = ctx.pathParam("milliseconds").toLong()
      log.info("Delay for $milliseconds milliseconds")
      delay(milliseconds)
      ctx.end("Delay for $milliseconds milliseconds")
    }
    server.requestHandler(router)
    server.listen(Integer.getInteger("http.port"), System.getProperty("http.address", "0.0.0.0"))
  }

  private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit): Route {
    return handler { ctx ->
      launch(ctx.vertx().dispatcher()) {
        try {
          fn(ctx)
        } catch (e: Exception) {
          ctx.fail(e)
        }
      }
    }
  }
}
