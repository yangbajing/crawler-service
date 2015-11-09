package crawler.util.time

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.Date

/**
 * DateTimeUtils
 * Created by yangjing on 15-11-6.
 */
object DateTimeUtils {

  val zoneOffset = ZoneOffset.ofHours(8)
  val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val formatterDateMinus = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val formatterMinus = DateTimeFormatter.ofPattern("HH:mm")

  def toLocalDateTime(s: String): LocalDateTime =
    s.length match {
      case 5 => LocalDateTime.parse(s, formatterMinus)
      case 16 => LocalDateTime.parse(s, formatterDateMinus)
      case 20 => LocalDateTime.parse(s, formatterDateTime)
      case _ => LocalDateTime.parse(s)
    }

  def toLocalDateTime(date: Date): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime), zoneOffset)

  def toDate(ldt: LocalDateTime): Date =
    new Date(ldt.toInstant(zoneOffset).toEpochMilli)

  def now() = LocalDateTime.now()
}
