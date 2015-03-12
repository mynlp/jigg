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

import java.util.Properties
import scala.xml.Node
import org.scalatest._

import jigg.util.Prop

class NothingAnnotator(override val name: String, override val props: Properties) extends Annotator {

  @Prop(gloss = "gloss of variable1", required=true) var variable1 = ""
  readProps()

  def annotate(node: Node) = node
}

class AnnotatorSpec extends FlatSpec with Matchers {

  "Opt variable" should "be customizable with property file" in {
    val props = new Properties
    props.setProperty("nothing.variable1", "hoge")

    val annotator = new NothingAnnotator("nothing", props)

    annotator.variable1 should be("hoge")
  }

  "Annotator" should "throws an exception during initProps if required variable is missed" in {
    val props = new Properties
    try {
      val annotator = new NothingAnnotator("nothing", props)
      fail()
    } catch {
      case e: ArgumentError =>
      case _: Throwable => fail()
    }
  }
}
