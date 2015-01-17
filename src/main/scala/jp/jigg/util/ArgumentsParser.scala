package jp.jigg.util

import java.util.Properties

object ArgumentsParser {
  def parse(args: List[String]): Properties = parseRecur(new Properties, args)

  private def parseRecur(props: Properties, args: List[String]): Properties = args match {
    case ArgKey(key) :: next => next match {
      case ArgKey(nextKey) :: tail => // -key1 -key2 ... => key1 is boolean value
        putTrue(props, key)
        parseRecur(props, next)
      case value :: tail =>
        props.put(key, value)
        parseRecur(props, tail)
      case Nil =>
        putTrue(props, key)
        parseRecur(props, next)
    }
    case _ => props
  }
  def putTrue(props: Properties, key: String) = props.put(key, "true")

  object ArgKey {
    def unapply(key: String): Option[String] = key match {
      case x if x.size > 1 && x(0) == '-' && x(1) == '-' => Some(x.substring(2))
      case x if x.size > 0 && x(0) == '-' => Some(x.substring(1))
      case _ => None
    }
  }
}
