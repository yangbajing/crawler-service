package crawler.util

import crawler.util.http.HttpClient

import scala.util.Random

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
trait Crawler {
  val httpClient: HttpClient

  protected def defaultHeaders = Array(
    Seq(
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36",
      "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
      "Accept-Encoding" -> "gzip, deflate, sdch",
      "Accept-Language" -> "zh-CN,zh;q=0.8,en;q=0.6",
      "Connection" -> "keep-alive"
    ),
    Seq(
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/601.2.7 (KHTML, like Gecko) Version/9.0.1 Safari/601.2.7",
      "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    ),
    Seq(
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:39.0) Gecko/20100101 Firefox/39.0",
      "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
      "Accept-Encoding" -> "gzip, deflate",
      "Accept-Language" -> "en-US,en;q=0.5",
      "Connection" -> "keep-alive"
    )
  )

  def requestHeaders = defaultHeaders(Random.nextInt(defaultHeaders.length))

  def fetchPage(url: String) = {
    val headers = defaultHeaders(Random.nextInt(defaultHeaders.length))
    //    println("url: " + url)
    //    headers.foreach(println)

    httpClient.get(url).setFollowRedirects(true).header(headers: _*).execute()
  }

}
