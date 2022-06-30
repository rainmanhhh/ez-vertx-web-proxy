package ez.vertx.webproxy

import ez.vertx.core.config.ConfigVerticle
import io.vertx.ext.web.client.WebClientOptions

class WebClientConfigVerticle : ConfigVerticle<WebClientConfig>() {
  override val key: String = "webClient"
  override var configValue: WebClientConfig = WebClientConfig()
}

class WebClientConfig : WebClientOptions() {
  /**
   * base uri of target server. eg: `https://www.example.com`
   * - should not be null or empty
   * - should not end with `/`
   */
  var uriRoot: String? = null
  /**
   * - true: send form to target server
   * - false: send json to target server
   */
  var useForm = false

  /**
   * if set, all requests will use the same httpMethod
   */
  var httpMethod: String? = null
}
