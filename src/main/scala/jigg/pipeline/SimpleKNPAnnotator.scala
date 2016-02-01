package jigg.pipeline

/*
 Copyright 2013-2015 Takafumi Sakakibara and Hiroshi Noji

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
import scala.xml._

class SimpleKNPAnnotator(override val name: String, override val props: Properties) extends SentencesAnnotator with KNPAnnotator{
  @Prop(gloss = "Use this command to launch KNP (-tab is automatically added. -anaphora is not compatible with this annotator. In that case, use knpDoc instead). Version >= 4.12 is assumed.") var command = "knp"
  readProps()

  lazy val knpProcess = startExternalProcess(
    command,
    Seq("-tab"),
    "http://nlp.ist.i.kyoto-u.ac.jp/index.php?KNP")

  /**
    * Close the external process and the interface
    */
  override def close() {
    knpOut.close()
    knpIn.close()
    knpProcess.destroy()
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    val sindex = (sentence \ "@id").toString
    val jumanTokens = (sentence \ "tokens").head
    val jumanStr = recovJumanOutput(jumanTokens).mkString
    val knpResult = runKNP(jumanStr)

    annotateSentenceNode(sentence, knpResult, sindex)
  }

  override def requires = Set(Requirement.TokenizeWithJuman)
  override def requirementsSatisfied = {
    import Requirement._
    Set(Chunk, Dependency, BasicPhrase, BasicPhraseDependency, NamedEntity)
  }
}
