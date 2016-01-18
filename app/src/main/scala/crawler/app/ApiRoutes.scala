package crawler.app

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives
import crawler.app.news.NewsRoute
import crawler.app.site.SiteRoute

/**
  * ApiRoute
  * Created by yangjing on 15-11-3.
  */
object ApiRoutes extends Directives {

  def apply() =
    pathPrefix("api") {
      path("health_check") {
        (get | head) {
          complete(HttpResponse())
        }
      } ~
        NewsRoute() ~
        SiteRoute()
    }

}
