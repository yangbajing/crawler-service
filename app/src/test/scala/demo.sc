import java.nio.charset.Charset
import java.nio.file.{Paths, Files}
import scala.collection.JavaConverters._

import scala.io.Source

val s =
  """crawler-news001    121.199.23.3
    |crawler-news002    121.199.4.6
    |crawler-news003    121.199.2.152
    |crawler-news004    121.199.12.190
    |crawler-news005    121.41.53.230
    |crawler-news006    121.199.5.96
    |crawler-news007    121.199.20.87
    |crawler-news008    121.40.93.44
    |crawler-news009    121.199.22.228
    |crawler-news010    120.26.94.198
    |crawler-news011    120.26.94.202
    |crawler-news012    120.26.94.146
    |crawler-news013    120.26.94.163
    |crawler-news014    120.26.94.211
    |crawler-news015    120.26.94.117
    |crawler-news016    120.26.94.195
    |crawler-news017    120.26.94.207
    |crawler-news018    120.26.94.185
    |crawler-news019    120.26.93.249
    |crawler-news020    120.26.94.17
    |crawler-news021    120.26.94.5
    |crawler-news022    120.26.94.7
    |crawler-news023    120.26.93.202
    |crawler-news024    120.26.94.188
    |crawler-news025    120.26.94.35
    |crawler-news026    120.26.94.58
    |crawler-news027    120.26.94.120
    |crawler-news028    120.26.94.203
    |crawler-news029    120.26.94.38
    |crawler-news030    120.26.94.150
    |crawler-news031    120.26.94.151
    |crawler-news032    120.26.94.147
    |crawler-news033    120.26.94.28
    |crawler-news034    120.26.94.191
    |crawler-news035    120.26.94.18
    |crawler-news036    120.26.93.254
    |crawler-news037    120.26.94.49
    |crawler-news038    120.26.94.139
    |crawler-news039    120.26.94.2
    |crawler-news040    120.26.94.4
    |crawler-news041    120.26.94.23
    |crawler-news042    120.26.94.29
    |crawler-news043    120.26.94.174
    |crawler-news044    120.26.94.8
    |crawler-news045    120.26.93.240
    |crawler-news046    120.26.93.215
    |crawler-news047    120.26.94.122
    |crawler-news048    120.26.94.12
    |crawler-news049    120.26.92.125
    |crawler-news050    120.26.92.180
    |crawler-news051    120.26.93.219
    |crawler-news052    120.26.94.76
    |crawler-news053    120.26.93.229
    |crawler-news054    120.26.94.22
    |crawler-news055    120.26.94.14
    |crawler-news056    120.26.94.84
    |crawler-news057    120.26.94.27
    |crawler-news058    120.26.93.221
    |crawler-news059    121.43.60.236""".stripMargin
val ss = Source.fromString(s).getLines().map { v =>
  val ip = v.drop(19)
  val hostname = v.take(15)
  Seq(hostname, ip, "1核1G", "/usr/app/python <br> /home/sc/open-falcon/agent")
    .mkString("| ", " | ", "  |")
}.toStream

val lines =
  Stream(
    Seq("hostname        ", "IP", "hardware", "path"),
    Seq("----------------", "--", "--------", "----")
  ).map(_.mkString("| ", "  | ", "  |")) #:::
    ss

Files.write(Paths.get("/tmp/crawler-news-hosts.txt"), lines.asJava)