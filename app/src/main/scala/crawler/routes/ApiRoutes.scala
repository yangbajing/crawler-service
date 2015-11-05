package crawler.routes

import akka.http.scaladsl.server.Directives._
import crawler.news.routes.NewsRoute

/**
 * ApiRoute
 * Created by yangjing on 15-11-3.
 */
object ApiRoutes {

  def apply(pathname: String) =
    pathPrefix(pathname) {
      NewsRoute("news")
    }

}
