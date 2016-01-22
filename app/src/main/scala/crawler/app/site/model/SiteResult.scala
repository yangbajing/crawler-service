package crawler.app.site.model

import java.time.LocalDateTime

import crawler.enums.ItemSource

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-22.
  */
case class SiteResult(source: ItemSource.Value,
                      key: String,
                      time: LocalDateTime,
                      count: Int,
                      items: Seq[SiteItem],
                      error: Option[String] = None)
