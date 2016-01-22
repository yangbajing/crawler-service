package crawler.module.news.enums

/**
 * 新闻来源
 * Created by yangjing on 15-11-4.
 */
object ItemSource extends Enumeration {
  val baidu = Value
  val sogou = Value
  val haosou = Value
  val court = Value
  val wechat = Value

  def withToNames(source: String): Traversable[Value] =
    if (source == null || source.isEmpty) {
      ItemSource.values
    } else {
      source.split(',').toSeq.collect {
        case s if ItemSource.values.exists(_.toString == s) =>
          ItemSource.withName(s)
      }
    }
}
