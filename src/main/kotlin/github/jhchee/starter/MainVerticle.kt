package github.jhchee.starter

import io.vertx.core.*
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainVerticle : CoroutineVerticle() {
  override fun start(startFuture: Promise<Void>?) {
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)
    router.get("/delay/ms/:milliSeconds").coroutineHandler { ctx ->
      val milliSeconds = ctx.pathParam("milliSeconds").toLong()
      delay(milliSeconds)
      ctx.response().end("Delay for $milliSeconds ms")
    }

    server.requestHandler(router).listen(8888)
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


//fun main() {
//  val vertx = Vertx.vertx()
//  vertx.deployVerticle(
//    "github.jhchee.starter.vertx.MainVerticle",
//    DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE)
//  )
//  println("Server running...")
//}
