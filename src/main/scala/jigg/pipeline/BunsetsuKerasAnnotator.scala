package jigg.pipeline

/*
 Copyright 2013-2015 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable low or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS.
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the Licensefor the specific language governing permissions and
 limitations under the License.
*/

import java.util.Properties
import scala.xml.Node

import jigg.ml.keras.{KerasModel, KerasParser}
import jigg.util.{HDF5Object, IOUtil, LookupTable}
import jigg.util.XMLUtil.RichNode

abstract class BunsetsuKerasAnnotator(override val name: String, override val props: Properties)
    extends KerasAnnotator with AnnotatingSentencesInParallel { self =>

  def defaultModelPath = "jigg-models/keras/bunsetsu_model.h5"
  def defaultTablePath = "jigg-models/keras/jpnLookupWords.json"

  @Prop(gloss = "Model file (use the default if ommited)") var model = ""
  @Prop(gloss = "Lookup table for mapping character into id space") var table = ""

  readProps()

  override def init() = {
    localAnnotators // init here, to output help message without loading
  }

  override def description =s"""${super.description}

  Annotate chunks (bunsetsu).

  Note about dictionary:
    Distionary settings of tokenizer (e.g., mecab) should be IPA.

"""

  trait LocalBunsetsuKerasAnntoator extends LocalAnnotator {
    val bunsetsuSplitter = self.loadParser()

    override def newSentenceAnnotation(sentence: Node): Node = {
      val sid = (sentence \ "@id").toString

      def chunkId(sid: String, idx: Int) = sid + "_chu" + idx

      val chunks = bunsetsuSplitter.parsing(sentence)

      var chunkIndex = 0

      val chunkNodes = chunks.map{ c =>
        val node = chunkToNode(
          chunkId(sid, chunkIndex),
          c.mkString(" "),
          c.head,
          c.last
        )
        chunkIndex += 1
        node
      }
      sentence addChild <chunks annotators={ name }>{ chunkNodes }</chunks>
    }

    private def chunkToNode(id: String, tokens: String, head: String, func: String) =
        <chunk
        id={ id }
        tokens={ tokens }
        head={ head }
        func={ func }
        />
  }

  override def requirementsSatisfied(): Set[Requirement] = Set(JaRequirement.BunsetsuChunk)
}

class IPABunsetsuKerasAnnotator(name: String, props: Properties) extends BunsetsuKerasAnnotator(name, props){

  def mkLocalAnnotator = new IPALocalBunsetsuKerasAnnotator

  class IPALocalBunsetsuKerasAnnotator extends LocalBunsetsuKerasAnntoator {
    val featAttributes = Array("lemma").map("@"+_)
  }

  override def requires() = Set(JaRequirement.TokenizeWithIPA)
}

object BunsetsuKerasAnnotator extends AnnotatorCompanion[BunsetsuKerasAnnotator] {
  override def fromProps(name: String, props: Properties) = new IPABunsetsuKerasAnnotator(name, props)
}
