package jigg.pipeline

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


/** Currently, this trait is useful to assign unique id
  * for each annotation.
  */
abstract class Annotation(val idPrefix: String) {
  val idGen = jigg.util.IDGenerator(idPrefix)
  def nextId: String = idGen.next
}

object Annotation {

  object Document extends Annotation("d")

  object Sentence extends Annotation("s")

  object Token extends Annotation("t")

  object Dependency extends Annotation("dep")

  object CCG extends Annotation("ccg")

  object NE extends Annotation("ne")

  object Mention extends Annotation("me")

  object Coreference extends Annotation("cr")

  object PredArg extends Annotation("pa")

  object ParseSpan extends Annotation("sp")
  object CCGSpan extends Annotation("ccgsp")

  object Chunk extends Annotation("ch")
}
