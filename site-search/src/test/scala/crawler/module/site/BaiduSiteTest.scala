package crawler.module.site

import akka.util.Timeout
import crawler.module.site.model.{SearchParam, SearchRequest}
import crawler.testsuite.ServiceSpec
import crawler.util.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
class BaiduSiteTest extends ServiceSpec {

  implicit val timeout = Timeout(30.seconds)

  "BaiduSiteTest" should {

    "fetchItemList" in {
      val requestParams = SearchRequest(
        SearchParam("晋渝地产", Some(SearchSyntax.Intitle)) ::
          //        SearchParam("阿里巴巴kakakakaak", Some(SearchSyntax.Intitle)) ::
          //          SearchParam("失信", syntax = Some(SearchSyntax.Intitle), strict = false) ::
          Nil
      )
      val baidu = new BaiduSite(HttpClient(), requestParams)

      val key = requestParams.toParam
      val f = baidu.fetchItemList()
      val result = Await.result(f, timeout.duration)
      result.items.foreach(v => println(v.jsonPretty))
      println(result.items.size)
      result.items must not be empty
    }

  }

}
