package crawler.util.http

import scala.collection.JavaConverters._

import com.ning.http.client._
import com.ning.http.client.cookie.Cookie
import com.ning.http.client.multipart.Part
import com.typesafe.config.Config

import scala.concurrent.{Future, Promise}

class HttpClientBuilder(builder: AsyncHttpClient#BoundRequestBuilder) {

  def queryParam(params: (String, String)*) = {
    params.foreach { case (name, value) => builder.addQueryParam(name, value) }
    this
  }

  def header(headers: (String, String)*) = {
    headers.foreach { case (name, value) => builder.addHeader(name, value) }
    this
  }

  def cookie(cookie: Cookie) = {
    builder.addCookie(cookie)
    this
  }

  def part(part: Part) = {
    builder.addBodyPart(part)
    this
  }

  def addFormParam(key: String, value: String) = {
    builder.addFormParam(key, value)
    this
  }

  def execute(): Future[Response] = {
    val promise = Promise[Response]()
    builder.execute(new AsyncCompletionHandler[Unit] {
      override def onCompleted(response: Response): Unit = {
        promise.success(response)
      }

      override def onThrowable(t: Throwable): Unit = {
        promise.failure(t)
      }
    })
    promise.future
  }

}

/**
 * HttpClient
 * Created by yangjing on 15-11-3.
 */
class HttpClient private(config: AsyncHttpClientConfig,
                         defaultHeaders: Iterable[(String, String)]) {

  private val client = new AsyncHttpClient(config)

  def close() = client.close()

  def get(url: String) = new HttpClientBuilder(client.prepareGet(url))

  def post(url: String) = new HttpClientBuilder(client.preparePost(url))

  def delete(url: String) = new HttpClientBuilder(client.prepareDelete(url))

  def put(url: String) = new HttpClientBuilder(client.preparePut(url))
}

object HttpClient {
  def apply(): HttpClient = apply(Nil)

  def apply(config: Config): HttpClient = {
    // TODO 解析config to AsyncHttpClientConfig
    apply(Nil)
  }

  def apply(defaultHeaders: Iterable[(String, String)]): HttpClient =
    apply(new AsyncHttpClientConfig.Builder().build, defaultHeaders)

  def apply(config: AsyncHttpClientConfig, defaultHeaders: Iterable[(String, String)]): HttpClient =
    new HttpClient(config, defaultHeaders)
}