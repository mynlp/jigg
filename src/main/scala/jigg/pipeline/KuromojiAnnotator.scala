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
import scala.io.Source
import scala.xml._
import collection.JavaConversions._
import jigg.util.PropertiesUtil

import com.atilika.kuromoji.{TokenBase, TokenizerBase}
import com.atilika.kuromoji.ipadic.{Token=>IToken, Tokenizer=>ITokenizer}
import com.atilika.kuromoji.jumandic.{Token=>JToken, Tokenizer=>JTokenizer}
import com.atilika.kuromoji.unidic.{Token=>UToken, Tokenizer=>UTokenizer}

abstract class KuromojiAnnotator(override val name: String, override val props: Properties)
    extends SentencesAnnotator {

  type T <: TokenBase

  @Prop(gloss = "Which dictionary do you use? Currently supported: ipa|juman|unidic") var dict = KuromojiAnnotator.defaultDict

  readProps()

  def tokenize(text: String): Seq[T]
  protected def tokenToNodes(token: T, id: String): Node

  override def newSentenceAnnotation(sentence: Node): Node = {

    def id(sindex: String, tindex: Int) = sindex + "_" + tindex

    val sindex = (sentence \ "@id").toString

    val tokenizedSentence = tokenize(sentence.text)
    var tokenIndex = 0

    val tokenNodes = tokenizedSentence.map { case token =>
      val nodes = tokenToNodes(token, id(sindex, tokenIndex))
      tokenIndex += 1
      nodes
    }

    val tokensAnnotation = <tokens>{ tokenNodes }</tokens>

    jigg.util.XMLUtil.addChild(sentence, tokensAnnotation)
  }

  override def requires = Set(Requirement.Sentence)
  override def requirementsSatisfied = Set(Requirement.TokenizeWithIPA)
}

// TODO: support naist-jdic annotator, possibly by making a trait implementing tokenToNodes,
// which is used both for ipa and naist.
class IPAKuromojiAnnotator(name: String, props: Properties)
    extends KuromojiAnnotator(name, props) {

  type T = IToken

  val tokenizer = new ITokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)

  def tokenToNodes(token: IToken, id: String): Node = {
    val surf = token.getSurface

    val nodes = <token
      id={ id }
      surf={ token.getSurface }
      pos={ token.getPartOfSpeechLevel1 }
      pos1={ token.getPartOfSpeechLevel2 }
      pos2={ token.getPartOfSpeechLevel3 }
      pos3={ token.getPartOfSpeechLevel4 }
      inflectionType={ token.getConjugationType }
      inflectionForm={ token.getConjugationForm }
      base={ token.getBaseForm }
      reading={ token.getReading }
      pronounce={ token.getPronunciation }/>

    nodes
  }

  override def requirementsSatisfied = Set(Requirement.TokenizeWithIPA)
}

class JumanKuromojiAnnotator(name: String, props: Properties)
    extends KuromojiAnnotator(name, props) {

  type T = JToken

  val tokenizer = new JTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)

  def tokenToNodes(token: JToken, id: String): Node = {
    val surf = token.getSurface

    val nodes = <token
      id={ id }
      surf={ token.getSurface }
      pos={ token.getPartOfSpeechLevel1 }
      pos1={ token.getPartOfSpeechLevel2 }
      pos2={ token.getPartOfSpeechLevel3 }
      pos3={ token.getPartOfSpeechLevel4 }
      base={ token.getBaseForm }
      reading={ token.getReading }
      semantic={ token.getSemanticInformation }/>

    nodes
  }

  override def requirementsSatisfied = Set(Requirement.TokenizeWithJuman)
}

class UnidicKuromojiAnnotator(name: String, props: Properties)
    extends KuromojiAnnotator(name, props) {

  type T = UToken

  val tokenizer = new UTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)

  def tokenToNodes(token: UToken, id: String): Node = {
    val surf = token.getSurface

    val nodes = <token
      id={ id }
      surf={ token.getSurface }
      pos={ token.getPartOfSpeechLevel1 }
      pos1={ token.getPartOfSpeechLevel2 }
      pos2={ token.getPartOfSpeechLevel3 }
      pos3={ token.getPartOfSpeechLevel4 }
      inflectionType={ token.getConjugationType }
      inflectionForm={ token.getConjugationForm }
      lemmaReading={ token.getLemmaReadingForm }
      lemma={ token.getLemma }
      pronounce={ token.getPronunciation }/>

    nodes
  }

  override def requirementsSatisfied = Set(Requirement.TokenizeWithJuman)
}

object KuromojiAnnotator extends AnnotatorCompanion[KuromojiAnnotator] {

  def defaultDict = "ipa"

  override def fromProps(name: String, props: Properties) = {
    val key = name + ".dict"
    val dict = PropertiesUtil.findProperty(key, props) getOrElse defaultDict
    dict match {
      case "ipa" => new IPAKuromojiAnnotator(name, props)
      case "juman" => new JumanKuromojiAnnotator(name, props)
      case "unidic" => new UnidicKuromojiAnnotator(name, props)
      case _ =>
        System.out.println(s"WARNING: Dictionary ${dict} is unsupported in kuromoji. Use ipadic...")
        new IPAKuromojiAnnotator(name, props)
    }
  }
}
