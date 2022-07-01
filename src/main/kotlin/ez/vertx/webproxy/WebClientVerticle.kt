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
import io.vertx.kotlin.coroutines.await
import org.slf4j.LoggerFactory

open class WebClientVerticle : AutoDeployVerticle, ConfigVerticle<WebClientConfig>() {
  companion object {
    private val logger = LoggerFactory.getLogger(WebClientVerticle::class.java)
    val messageSendReq = WebClientVerticle::class.java.name + ".messageSendReq"
  }

  lateinit var client: WebClient

  private suspend fun sendRequest(req: Req<JsonObject>): HttpResponse<Buffer> {
    val httpMethod = HttpMethod.valueOf((configValue.httpMethod ?: req.headers.httpMethod)!!)
    val uri = configValue.uriRoot!! + (configValue.uriFixedPath ?: req.headers.path)
    logger.info("sendRequest: {} {}", httpMethod, uri)
    val request = client.requestAbs(httpMethod, uri)
    val response =
      if (configValue.useForm) request.sendForm(req.body.toMultiMap())
      else request.sendJsonObject(req.body)
    return response.await()
  }

  /**
   * return response body, or throw a [ez.vertx.core.err.HttpException].
   * default: decode response body as json
   */
  open fun decodeRes(res: HttpResponse<Buffer>): SimpleRes<Any?> = SimpleRes<Any?>().apply {
    code = res.statusCode()
    if (isSuccess()) data = res.bodyAsBuffer()?.toJson()
    else message = res.bodyAsString(configValue.responseCharset)
  }

  override val key: String = "webClient"
  override var configValue: WebClientConfig = WebClientConfig()

  override suspend fun afterConfig() {
    configValue.uriRoot ?: throw NullPointerException("uriRoot is null")
    client = WebClient.create(vertx, configValue)
    receiveMessage(messageSendReq) {
      it.headers.path
      val res = sendRequest(it) // uri ?
      decodeRes(res)
    }
  }
}
