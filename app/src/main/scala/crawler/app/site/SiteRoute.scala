package crawler.app.site

import crawler.SystemUtils
import crawler.common.BaseRoute
import crawler.app.news.model.SearchRequest

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
object SiteRoute extends BaseRoute {

  def apply() =
    pathPrefix("site") {
      path("baidu") {
        post {
          entity(as[SearchRequest]) { searchRequest =>
            val baidu = new BaiduSite(SystemUtils.httpClient, searchRequest)
            complete(baidu.fetchItemList())
          }
        }
      }
    }

}
