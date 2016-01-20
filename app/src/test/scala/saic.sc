import scala.io.Source

val s = """|120.55.182.150<br>(10.117.12.74)   | 1核1G    | /usr/app/python |saic  |
          ||120.26.225.105<br>(10.117.55.14)   | 1核1G    | /usr/app/python |saic  |
          ||121.41.2.74<br>(10.168.96.82)      | 1核1G    | /usr/app/python |saic  |
          ||120.55.113.230<br>(10.168.152.118) | 1核1G    | /usr/app/python |saic  |
          ||120.55.114.18<br>(10.168.154.133)  | 1核1G    | /usr/app/python |saic  |
          ||120.55.88.109<br>(10.117.196.51)   | 1核1G    | /usr/app/python |saic  |
          ||121.41.2.196<br>(10.168.91.79)     | 1核1G    | /usr/app/python |saic  |
          ||121.41.2.186<br>(10.168.94.151)    | 1核1G    | /usr/app/python |saic  |
          ||120.55.64.125<br>(10.117.211.194)  | 1核1G    | /usr/app/python |saic  |
          ||121.41.2.162<br>(10.168.93.81)     | 1核1G    | /usr/app/python |saic  |
          ||121.41.1.166<br>(10.168.54.249)    | 1核1G    | /usr/app/python |saic  |
          ||120.26.217.236<br>(10.117.52.105)  | 1核1G    | /usr/app/python |saic  |
          ||120.26.92.73<br>(10.51.8.148)      | 1核1G    | /usr/app/python |saic  |
          ||120.55.180.251<br>(10.117.8.21)    | 1核1G    | /usr/app/python |saic  |
          ||120.26.91.2<br>(10.117.209.143)    | 1核1G    | /usr/app/python |saic  |
          ||120.26.223.152<br>(10.117.51.186)  | 1核1G    | /usr/app/python |saic  |
          ||120.26.223.135<br>(10.117.52.107)  | 1核1G    | /usr/app/python |saic  |
          ||120.26.91.8<br>(10.117.209.141)    | 1核1G    | /usr/app/python |saic  |
          ||120.55.112.92<br>(10.168.152.171)  | 1核1G    | /usr/app/python |saic  |
          ||120.55.181.10<br>(10.117.8.192)    | 1核1G    | /usr/app/python |saic  |""".stripMargin
val lines = Source.fromString(s).getLines().toStream
println(lines.size)
