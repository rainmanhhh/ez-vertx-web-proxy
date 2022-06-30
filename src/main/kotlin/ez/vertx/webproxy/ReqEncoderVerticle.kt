package ez.vertx.webproxy

import ez.vertx.core.busi.BusiVerticle
import ez.vertx.core.config.ConfigVerticle
import ez.vertx.core.config.HttpServerConfig
import ez.vertx.core.message.res.SimpleRes
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject

abstract class ReqEncoderVerticle<Req>: BusiVerticle<SimpleRes<Req>>() {
  lateinit var busiAddress: String

  override suspend fun start() {
    super.start()
    val httpServer: HttpServerConfig by ConfigVerticle
    busiAddress = httpServer.busiAddress
    if (busiAddress.isEmpty()) throw IllegalArgumentException("busiAddress should not be empty")
  }

  override fun path(): String = busiAddress

  abstract fun encodeReq(httpMethod: HttpMethod?, path: String?, params: JsonObject): Req

  final override fun serve(httpMethod: HttpMethod?, path: String?, params: JsonObject): SimpleRes<Req> {
    return SimpleRes.continueTo(WebClientVerticle.messageSendReq, encodeReq(httpMethod, path, params))
  }
}
