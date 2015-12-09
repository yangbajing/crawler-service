package crawler.news.enums

/**
 * 新闻来源
 * Created by yangjing on 15-11-4.
 */
object NewsSource extends Enumeration {
  val baidu = Value
  val sogou = Value
  val haosou = Value
  val court = Value
  val wechat = Value

  def withToNames(source: String): Seq[Value] =
    if (source.isEmpty) {
      NewsSource.values.toSeq
    } else {
      source.split(',').toSeq.collect {
        case s if NewsSource.values.exists(_.toString == s) =>
          NewsSource.withName(s)
      }
    }
}
