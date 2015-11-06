package crawler.news.service

import crawler.news.enums.{SearchMethod, NewsSource}
import org.scalatest.{MustMatchers, WordSpec}

class NewsDBRepoTest extends WordSpec with MustMatchers {

  "NewsDBRepoTest" should {
    val dbRepo = new NewsDBRepo

    "findNews" in {
      val results = dbRepo.findNews("杭州卷瓜网络有限公司", Seq(NewsSource.BAIDU), SearchMethod.F)
      results must not be empty
    }

  }
}
