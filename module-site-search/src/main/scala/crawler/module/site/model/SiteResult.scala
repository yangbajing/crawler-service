package crawler.module.site.model

import java.time.LocalDateTime

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-22.
  */
case class SiteResult(source: String,
                      key: String,
                      time: LocalDateTime,
                      count: Int,
                      items: Seq[SiteItem],
                      error: Option[String] = None)
