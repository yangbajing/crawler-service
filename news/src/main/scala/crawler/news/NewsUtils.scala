package crawler.news

import java.util.concurrent.atomic.AtomicInteger

import crawler.news.model.NewsResult

/**
 * News Utils
 * Created by yangjing on 15-11-5.
 */
object NewsUtils {
  private val _nums = new AtomicInteger(0)

  type NewsResultType = Either[Exception, NewsResult]

  def getInt = _nums.getAndIncrement()
}
