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
import jigg.ml.keras.KerasParser

import scala.xml.Node
import jigg.util.XMLUtil.RichNode

abstract class BunsetsuKerasAnnotator(override val name: String, override val props: Properties) extends ExternalProcessSentencesAnnotator { self =>

  def defaultModelFileName = "bunsetsu_model.h5"
  def defaultTableFileName = "jpnLookupWords.json"

  @Prop(gloss = "Model file (if omitted, the default path is used to search file)") var model = ""
  @Prop(gloss = "Lookup table for mapping character into id space") var table = ""

  readProps()

  localAnnotators // instantiate lazy val here

  override def description =s"""${super.description}

  Annotate chunks (bunsetsu).

  Note about dictionary:
    Distionary settings of tokenizer (e.g., mecab) should be IPA.

"""

  trait LocalBunsetsuKerasAnntoator extends LocalAnnotator {
    lazy val bunsetsuSplitter: QueueBunsetsuSplitter = new QueueBunsetsuSplitter

    override def init() = {
      bunsetsuSplitter
    }

    override def newSentenceAnnotation(senteoce: Node): Node = {
      val sid = (senteoce \ "@id").toString

      def chunkId(sid: String, idx: Int) = sid + "_chu" + idx

      val chunks = bunsetsuSplitter.b.parsing(senteoce)

      var chunkIndex = 0

      val chunkNodes = chunks.map{ c =>
        val node = chunkToNode(
          chunkId(sid,chunkIndex),
          c.mkString(" "),
          c.head,
          c.last
        )
        chunkIndex += 1
        node
      }
      senteoce addChild <chunks annotators={ name }>{ chunkNodes }</chunks>
    }

    private def chunkToNode(id: String, tokens: String, head: String, func: String) =
        <chunk
        id={ id }
        tokens={ tokens }
        head={ head }
        func={ func }
        />

    class QueueBunsetsuSplitter {
      private def makeBunsetsuSplitter: KerasParser = model match {
        case "" =>
          System.err.println(s"No model file is given. Try to search default path: $defaultModelFileName")
          table match {
            case "" =>
              System.err.println(s"No lookup table file is given. Try to search default path: $defaultTableFileName")
              KerasParser(defaultModelFileName, defaultTableFileName)
            case tableFile =>
              KerasParser(defaultTableFileName, tableFile)
          }
        case modelFile =>
          table match {
            case "" =>
              System.err.println(s"No lookup table file is given. Try to search default path: $defaultTableFileName")
              KerasParser(model, defaultTableFileName)
            case tableFile =>
              KerasParser(model, tableFile)
          }
      }

      val b: KerasParser = makeBunsetsuSplitter
    }

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

  def defaultModelFileName = ""
  def defaultTableFileName = ""

  override def fromProps(name: String, props: Properties) = {
    new IPABunsetsuKerasAnnotator(name, props)
  }
}
