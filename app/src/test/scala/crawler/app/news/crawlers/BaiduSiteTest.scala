package crawler.app.news.crawlers

import akka.util.Timeout
import crawler.app.site.BaiduSite
import crawler.common.JsonSupport.formats
import crawler.common.SearchSyntax
import crawler.model.{SearchParam, SearchRequest}
import crawler.testsuite.ServiceSpec
import crawler.util.http.HttpClient
import org.json4s.Extraction
import org.json4s.jackson.Serialization

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
class BaiduSiteTest extends ServiceSpec {

  implicit val timeout = Timeout(30.seconds)

  "BaiduSiteTest" should {

    "fetchItemList" in {
      val baidu = new BaiduSite(HttpClient(), true)
      val request = SearchRequest(
        //        SearchParam("江苏华米数码科技有限公司", Some(SearchSyntax.Intitle)) ::
        SearchParam("阿里巴巴kakakakaak", Some(SearchSyntax.Intitle)) ::
          SearchParam("失信", syntax = Some(SearchSyntax.Intitle), strict = false) ::
          Nil
      )

      val jv = Extraction.decompose(request)
      println(Serialization.write(jv))

      val key = request.toParam
      val f = baidu.fetchItemList(key)
      val result = Await.result(f, timeout.duration)
      result.news.foreach(println)
      println(result.source + " " + result.key)
      println(result.news.size)
      result.news must not be empty
    }

  }

}