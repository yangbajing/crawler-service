import java.time.{LocalDateTime, LocalTime, Instant}

import crawler.util.time.DateTimeUtils

val s = "vrTimeHandle552write('1444547463')"

val matches = """'(\d+)'""".r.findAllMatchIn(s).map(_.matched).toList
val t = matches.head.replace("'", "").toInt

val ins = Instant.ofEpochSecond(1447057907)
LocalDateTime.ofInstant(ins, DateTimeUtils.ZoneOffset)

//LocalTime.ofSecondOfDay(t)
