package crawler.model

import crawler.enums.{QueryCond, SearchSyntax}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
case class SearchRequest(params: Seq[SearchParam]) {

  def toParam = params.map(_.toParam).mkString(" ")

}

case class SearchParam(value: String,
                       syntax: Option[SearchSyntax.Value] = None,
                       cond: Option[QueryCond.Value] = None,
                       filetypeDoc: Seq[String] = Nil,
                       strict: Boolean = true) {

  def toParam =
    syntax.map(v => if (strict) s"""$v:"$value"""" else s"$v:$value") orElse
      cond.map(v => v + value) getOrElse
      value

}
