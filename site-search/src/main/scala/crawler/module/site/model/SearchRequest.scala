package crawler.module.site.model

import crawler.module.site.QueryCond

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
case class SearchRequest(params: Seq[SearchParam], followUrl: Boolean = true) {

  def toParam = params.map(_.toParam).mkString(" ")

}

case class SearchParam(value: String,
                       syntax: Option[String] = None,
                       cond: Option[QueryCond.Value] = None,
                       filetypeDoc: Seq[String] = Nil,
                       strict: Boolean = true) {

  def toParam =
    syntax.map(v => if (strict) s"""$v:"$value"""" else s"$v:$value") orElse
      cond.map(v => v + value) getOrElse
      value

}
