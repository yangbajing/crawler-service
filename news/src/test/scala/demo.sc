def func(v: Any) =
  v match {
    case 7 =>
      println("v is 7")

    case 32.34 =>
      println("v is 32.34")

    case s: String =>
      println(s"String: $s")

    case i: Int =>
      println(s"Int: $i")

    case f: Boolean =>
      println(s"Boolean: $f")

    case _ =>
      println("default")
  }


func(234)
func("lksdjf")
func(7)
func(32.34)
func(32.33)