import java.time.{LocalDateTime, LocalTime, Instant}

import crawler.util.time.TimeUtils

val s = "vrTimeHandle552write('1444547463')"

val matches = """'(\d+)'""".r.findAllMatchIn(s).map(_.matched).toList
val t = matches.head.replace("'", "").toInt

val ins = Instant.ofEpochSecond(1447057907)
LocalDateTime.ofInstant(ins, TimeUtils.ZONE_OFFSET)

//LocalTime.ofSecondOfDay(t)
