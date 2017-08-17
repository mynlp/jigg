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

// trait IDGeneratorBase {
//   def next(): String
// }

// case class IDGenerator(prefix: String) extends IDGeneratorBase {
//   private[this] val stream = Stream.from(0).iterator
//   def next() = prefix + stream.next
// }

case class IDGenerator(toId: Int=>String) {
  private[this] var stream = Stream.from(0).iterator
  def next() = toId(stream.next)
  def reset() = stream = Stream.from(0).iterator
}

object IDGenerator {
  def apply(prefix: String): IDGenerator = IDGenerator(prefix + _)
}

/** Not thread-safe but little overhead
  */
case class LocalIDGenerator(toId: Int=>String) {
  var i = 0
  def next() = {
    val n = toId(i)
    i += 1
    n
  }
}
