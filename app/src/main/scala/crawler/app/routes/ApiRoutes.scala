package crawler.app.routes

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives

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
