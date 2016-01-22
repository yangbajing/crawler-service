package crawler.module.news.service

import java.util.concurrent.TimeUnit

import crawler.module.news.enums.{ItemSource, SearchMethod}
import crawler.testsuite.ServiceSpec
import crawler.util.time.TimeUtils

class NewsDBRepoTest extends ServiceSpec {

  "NewsDBRepoTest" should {
    val dbRepo = new NewsDBRepo

    "findNews" in {
      val result = dbRepo.findNews("阿里巴巴", Seq(ItemSource.baidu), SearchMethod.F, Some(TimeUtils.nowBegin()))
      val list = result.futureValue
      println(list)
      list must not be empty

      TimeUnit.SECONDS.sleep(5)
    }

  }
}
