package crawler.util.http

import com.ning.http.client._
import com.ning.http.client.cookie.Cookie
import com.ning.http.client.multipart.Part
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

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

  def addFormParam(params: (String, String)*) = {
    params.foreach { case (key, value) => builder.addFormParam(key, value) }
    this
  }

  def setFollowRedirects(followRedirects: Boolean) = {
    builder.setFollowRedirects(followRedirects)
    this
  }

  def execute(): Future[Response] = {
    val promise = Promise[Response]()
    try {
      builder.execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          //          println(response.getStatusCode + ": " + response.getStatusText)
          promise.success(response)
        }

        override def onThrowable(t: Throwable): Unit = {
          promise.failure(t)
        }
      })
    } catch {
      case e: Throwable =>
        promise.failure(e)
    }
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

  def apply(allowRedirect: Boolean): HttpClient = {
    val builder = new AsyncHttpClientConfig.Builder()
    builder.setFollowRedirect(false)
    apply(builder.build(), Nil)
  }

  def find302Location(client: HttpClient, url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext) = {
    val promise = Promise[String]()

    def findLocation() = client.get(url).header(headers: _*).setFollowRedirects(false).execute().map(_.getHeader("Location"))

    findLocation().onComplete {
      case Success(location) => promise.success(location)
      case Failure(e) =>
        findLocation().onComplete {
          case Success(location) => promise.success(location)
          case Failure(t) => promise.failure(t)
        }
    }

    promise.future
  }

}