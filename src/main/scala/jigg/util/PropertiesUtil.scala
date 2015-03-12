package jigg.util

/*
 Copyright 2013-2015 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

import java.util.Properties
import scala.collection.JavaConversions._

object PropertiesUtil {
  def findProperty(key: String, props: Properties): Option[String] = props.getProperty(key) match {
    case null => None
    case value => Some(value)
  }
  def safeFind(key: String, props: Properties): String = findProperty(key, props).getOrElse { sys.error(s"$key property is required!" ) }

  def getBoolean(key: String, props: Properties): Option[Boolean] = findProperty(key, props) map {
    case "true" => true
    case "false" => false
    case _ => sys.error(s"Property $key should be true or false")
  }

  def filter(props: Properties)(f: (String, String)=>Boolean): Seq[(String, String)] =
    props.stringPropertyNames.toSeq
      .map { k => (k, props.getProperty(k)) }
      .filter { case (k, v) => f(k, v) }
}
