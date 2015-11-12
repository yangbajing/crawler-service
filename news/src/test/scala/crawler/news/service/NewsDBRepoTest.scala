package crawler.news.service

import java.util.concurrent.TimeUnit

import crawler.news.enums.{SearchMethod, NewsSource}
import crawler.testsuite.ServiceSpec
import crawler.util.time.DateTimeUtils
import org.scalatest.{MustMatchers, WordSpec}

class NewsDBRepoTest extends ServiceSpec {

  import system.dispatcher

  "NewsDBRepoTest" should {
    val dbRepo = new NewsDBRepo

    "findNews" in {
      val result = dbRepo.findNews("阿里巴巴", Seq(NewsSource.baidu), SearchMethod.F, Some(DateTimeUtils.nowBegin()))
      val list = result.futureValue
      println(list)
      list must not be empty

      TimeUnit.SECONDS.sleep(5)
    }

  }
}
