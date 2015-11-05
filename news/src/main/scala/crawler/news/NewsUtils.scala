package crawler.news

import java.util.concurrent.atomic.AtomicInteger

/**
 * News Utils
 * Created by yangjing on 15-11-5.
 */
object NewsUtils {
  private val _nums = new AtomicInteger(0)

  def getIndent = _nums.getAndIncrement()
}
