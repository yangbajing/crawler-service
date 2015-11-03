package crawler.news.routes

import akka.http.scaladsl.server.Directives._

/**
 * 新闻路由
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object NewsRoute {
  def apply(pathname: String) =
    path(pathname) {
      get {
        parameters('company, 'method ? "A") { (company, method) =>
          complete("")
        }
      }
    }

}
