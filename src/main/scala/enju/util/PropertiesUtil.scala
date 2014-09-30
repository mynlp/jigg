package enju.util

import java.util.Properties

object PropertiesUtil {
  def findProperty(key: String, props: Properties): Option[String] = props.getProperty(key) match {
    case null => None
    case value => Some(value)
  }
}
