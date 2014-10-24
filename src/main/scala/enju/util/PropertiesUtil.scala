package enju.util

import java.util.Properties

object PropertiesUtil {
  def findProperty(key: String, props: Properties): Option[String] = props.getProperty(key) match {
    case null => None
    case value => Some(value)
  }
  def getBoolean(key: String, props: Properties): Option[Boolean] = findProperty(key, props) map {
    case "true" => true
    case "false" => false
    case _ => sys.error(s"Property $key should be true or false")
  }
}
