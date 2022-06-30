package ez.vertx.webproxy

import ez.vertx.core.AutoDeployVerticle
import ez.vertx.core.config.ConfigVerticle
import ez.vertx.core.message.receiveMessage
import ez.vertx.core.message.req.Req
import ez.vertx.core.message.res.SimpleRes
import ez.vertx.core.util.httpMethod
import ez.vertx.core.util.path
import ez.vertx.core.util.toMultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import org.slf4j.LoggerFactory

open class WebClientVerticle : AutoDeployVerticle, CoroutineVerticle() {
  companion object {
    private val logger = LoggerFactory.getLogger(WebClientVerticle::class.java)
    val messageSendReq = WebClientVerticle::class.java.name + ".messageSendReq"
  }

  lateinit var cfg: WebClientConfig
  lateinit var client: WebClient

  private suspend fun sendRequest(req: Req<JsonObject>): HttpResponse<Buffer> {
    val httpMethod = HttpMethod.valueOf((cfg.httpMethod ?: req.headers.httpMethod)!!)
    val uri = cfg.uriRoot!! + req.headers.path
    logger.debug("httpMethod: {}, uri: {}", httpMethod, uri)
    val request = client.requestAbs(httpMethod, uri)
    val response =
      if (cfg.useForm) request.sendForm(req.body.toMultiMap())
      else request.sendJsonObject(req.body)
    return response.await()
  }

  /**
   * return response body, or throw a [ez.vertx.core.err.HttpException]
   */
  open fun decodeRes(res: HttpResponse<Buffer>): SimpleRes<Any?> = SimpleRes<Any?>().apply {
    code = res.statusCode()
    data = res.body()
  }

  override suspend fun start() {
    val webClient: WebClientConfig by ConfigVerticle
    cfg = webClient
    client = WebClient.create(vertx, cfg)
    receiveMessage(messageSendReq) {
      it.headers.path
      val res = sendRequest(it) // uri ?
      decodeRes(res)
    }
  }
}
