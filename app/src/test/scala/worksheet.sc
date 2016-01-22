import java.time.LocalDateTime

import crawler.app.site.BaiduSite
//BaiduSite.dealTime("2015年1月13日")
//BaiduSite.dealTime("2015年1月1日")
//BaiduSite.dealTime("2015年11月13日")
//BaiduSite.dealTime("2015年11月3日")

"www.runoob.com/kjlsdf/sdf/".take("www.runoob.com/kjlsdf/sdf/".indexOf('/'))

val TIME_PATTERN = """(\d{4})年(\d{1,2})月(\d{1,2})日""".r
def parseTime(s: String) = s.substring(0, s.indexOf('日')+1) match {
  case TIME_PATTERN(year, month, day) => LocalDateTime.of(year.toInt, month.toInt, day.toInt, 0, 0)
  case _ => null
}
parseTime("2015年1月13日 -  ")
parseTime("2015年1月1日")
parseTime("2015年11月13日")
parseTime("2015年11月3日")
parseTime("2015年11月332日")
parseTime("15年11月332日")
