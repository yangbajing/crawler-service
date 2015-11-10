package crawler.util.time

import java.time._
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * DateTimeUtils
 * Created by yangjing on 15-11-6.
 */
object DateTimeUtils {
  val ZONE_OFFSET = ZoneOffset.ofHours(8)
  val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val formatterDateMinus = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val formatterMinus = DateTimeFormatter.ofPattern("HH:mm")

  def toLocalDateTime(instant: Instant): LocalDateTime = LocalDateTime.ofInstant(instant, ZONE_OFFSET)

  def toLocalDateTime(s: String): LocalDateTime = {
    s.length match {
      case 5 =>
        LocalDateTime.parse(s, formatterMinus)
      case 16 =>
        LocalDateTime.parse(s, formatterDateMinus)
      case 19 =>
        LocalDateTime.parse(s, formatterDateTime)
      case _ =>
        LocalDateTime.parse(s)
    }
  }

  def toLocalDateTime(date: Date): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime), ZONE_OFFSET)

  def toDate(ldt: LocalDateTime): Date =
    new Date(ldt.toInstant(ZONE_OFFSET).toEpochMilli)

  def now() = LocalDateTime.now()

  /**
   * @return 一天的开始：
   */
  def nowBegin(): LocalDateTime = LocalDate.now().atTime(0, 0, 0, 0)

  /**
   * @return 一天的结尾：
   */
  def nowEnd(): LocalDateTime = LocalTime.of(23, 59, 59, 999999999).atDate(LocalDate.now())
}
