import java.util.stream.Collectors
import crawler.util.http.HttpClient
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
"""\d+""".r.findAllMatchIn("234234 927 wf lksdfj 9823").map(_.matched).toList

//val timeStr = "2015年10月23日 14:33"
val timeStr = "32小时前"
timeStr.replaceAll( """年|月""", "-").replace("日", "") + ".00"

"腾讯科技  13小时前".split("  ")

val matcher = """(\d+)小时前""".r.pattern.matcher(timeStr)

matcher.matches()
//matcher.groupCount()
matcher.group(1)


"""(\d+)小时前""".r.findAllMatchIn(timeStr).toList

HttpClient()


Jsoup.connect("http://it.21cn.com/prnews/a/2015/1103/19/30229642.shtml").execute().parse()
