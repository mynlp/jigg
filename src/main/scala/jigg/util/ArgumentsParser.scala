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

object ArgumentsParser {
  def parse(args: List[String]): Properties = parseRecur(new Properties, args)

  private def parseRecur(props: Properties, args: List[String]): Properties = args match {
    case ArgKey(key) :: next => next match {
      case ArgKey(nextKey) :: tail => // -key1 -key2 ... => key1 is boolean value
        putTrue(props, key)
        parseRecur(props, next)
      case value :: tail =>
        key match {
          case "props" => props.load(jigg.util.IOUtil.openIn(value))
          case _ => props.put(key, value)
        }
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
      case x if x.size > 1 && x(0) == '-' && x.drop(1).forall(x=>x.isDigit || x=='.') => None // -10.0, -1, etc are not key
      case x if x.size > 1 && x(0) == '-' && x(1) == '-' => Some(x.substring(2))
      case x if x.size > 1 && x(0) == '-' => Some(x.substring(1)) // we don't catch if x.size == 1, ('-' is recognized as some value)
      case _ => None
    }
  }
}
