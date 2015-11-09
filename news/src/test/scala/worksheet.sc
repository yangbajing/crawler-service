import crawler.news.NewsUtils
import crawler.util.time.DateTimeUtils

Seq("baidu", "sogou", "qq").map(_ => "").mkString("(source = ?", " or source = ?", ")")

"""\d+""".r.findAllMatchIn("找到约16条结果").mkString


DateTimeUtils.toLocalDateTime("2015-11-11 22:22")

"财经网 2015-10-28 17:52".split(' ').toList

println(' '.toChar.toInt)
val div =
  <div class="ft">2009年创办浙江鼎联科科技有限公司（台湾Dlink） 2011年创办杭州易才凯捷科技有限公司 2014年3月创办杭州今元投资管理有限公司、<em><!--red_beg-->杭州今元标矩科技有限公司<!--red_end--></em> 2014年5月创办杭州今元嘉和人力资源有限公司 2014年12月创办今元网络技术有限公司 主办：猎云网 ？...<a id="news_similar" class="samenews" href="?query=%BA%BC%D6%DD%BD%F1%D4%AA%B1%EA%BE%D8%BF%C6%BC%BC%D3%D0%CF%DE%B9%AB%CB%BE&amp;clusterId=http%3A%2F%2Fffq.xue163.com%2F229%2F1%2F2294109.html&amp;ct=1&amp;sort=0&amp;mode=1&amp;w=1447037485558&amp;dr=1&amp;dp=1&amp;p=sogou">
    &gt;&gt;6条相同新闻
  </a>
  </div>

div.text.replace(""">>\d+?条相同新闻""", "")
NewsUtils.uriToBaseUri("http://www.chinacourt.org/article/search.shtml")

DateTimeUtils.toLocalDateTime("2015-04-12 11:46:00")
"2015-04-12 11:46:00".map(v => (v, v.toInt))
' '.toInt
