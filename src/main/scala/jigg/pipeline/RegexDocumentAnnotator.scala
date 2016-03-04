package jigg.pipeline

/*
 Copyright 2013-2016 Takafumi Sakakibara and Hiroshi Noji

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

class RegexDocumentAnnotator(override val name: String, override val props: Properties) extends Annotator {

  @Prop(gloss = "Regular expression to segment documents") var pattern = """\n{2,}"""
  readProps()

  private[this] val documentIDGen = jigg.util.IDGenerator("d")
  override def annotate(annotation: Node): Node = {
    val raw = annotation.text

    val documents = raw.split(pattern).map { str =>
      <document id={ documentIDGen.next }>{ str }</document>
    }

    <root>{ documents }</root>
  }

  override def requires = Set()
  override def requirementsSatisfied = Set(Requirement.Dsplit)
}
