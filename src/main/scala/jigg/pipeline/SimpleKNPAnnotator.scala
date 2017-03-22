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

class SimpleKNPAnnotator(override val name: String, override val props: Properties)
    extends KNPAnnotator with ExternalProcessSentencesAnnotator { self=>

  @Prop(gloss = "Use this command to launch KNP (-tab is automatically added. -anaphora is not compatible with this annotator. In that case, use knpDoc instead). Version >= 4.12 is assumed.") var command = "knp"
  readProps()

  localAnnotators // instantiate lazy val here

  def mkLocalAnnotator = new SimpleKNPLocalAnnotator

  class SimpleKNPLocalAnnotator extends LocalAnnotator with BaseKNPLocalAnnotator {
    override def defaultArgs = Seq("-tab")

    val knp = mkIO()

    override def newSentenceAnnotation(sentence: Node): Node = {
      val sentenceId = (sentence \ "@id").toString

      val knpResult = runKNP(sentence, None)
      annotateSentenceNode(sentence, knpResult, sentenceId,  _ => sentenceId)
    }
  }
}
