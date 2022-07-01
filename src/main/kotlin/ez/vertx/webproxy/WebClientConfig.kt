package ez.vertx.webproxy

import io.vertx.ext.web.client.WebClientOptions

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

  /**
   * target server response charset
   */
  var responseCharset = "UTF-8"
}
