package crawler.util

import java.lang.management.ManagementFactory
import java.nio.charset.Charset

import crawler.util.time.TimeUtils

/**
 * Utils
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-12-03.
 */
object Utils {
  val CHARSET = Charset.forName("UTF-8")

  def getPid = {
    val runtime = ManagementFactory.getRuntimeMXBean
    runtime.getName.split('@')(0)
  }

  def lastYearPeriods(): Seq[Int] = {
    val now = TimeUtils.now()
    val (curMonth, curYear, preYear) = (now.getMonthValue, now.getYear * 100, now.getYear * 100 - 100)
    (curMonth + 1 to 12).map(preYear + _) ++ (1 to curMonth).map(curYear + _)
  }
}
