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
import jigg.util.XMLUtil.RichNode

import com.atilika.kuromoji.{TokenBase, TokenizerBase}
import com.atilika.kuromoji.ipadic.{Token=>IToken, Tokenizer=>ITokenizer}
import com.atilika.kuromoji.jumandic.{Token=>JToken, Tokenizer=>JTokenizer}
import com.atilika.kuromoji.unidic.{Token=>UToken, Tokenizer=>UTokenizer}

abstract class KuromojiAnnotator(override val name: String, override val props: Properties)
    extends SentencesAnnotator {

  type T <: TokenBase

  @Prop(gloss = "Which dictionary do you use? Currently supported: ipa|juman|unidic") var dic = KuromojiAnnotator.defaultDic

  readProps()

  override def newSentenceAnnotation(sentence: Node): Node = {

    def id(sindex: String, tindex: Int) = sindex + "_" + tindex

    val sindex = (sentence \ "@id").toString

    val tokenizedSentence = tokenize(sentence.text)
    var tokenIndex = 0

    val tokenNodes = tokenizedSentence.map { case token =>
      val begin = token.getPosition
      val end = begin + token.getSurface.size
      val node = tokenToNode(token, begin + "", end + "", id(sindex, tokenIndex))
      tokenIndex += 1
      node
    }

    val tokensAnnotation = <tokens annotators={ name }>{ tokenNodes }</tokens>

    sentence addChild tokensAnnotation
  }

  protected def tokenize(text: String): Seq[T]
  protected def tokenToNode(token: T, begin: String, end: String, id: String): Node

  override def requires = Set(Requirement.Ssplit)
  override def requirementsSatisfied = Set(JaRequirement.TokenizeWithIPA)
}

// TODO: support naist-jdic annotator, possibly by making a trait implementing tokenToNodes,
// which is used both for ipa and naist.
class IPAKuromojiAnnotator(name: String, props: Properties)
    extends KuromojiAnnotator(name, props) {

  type T = IToken

  val tokenizer = new ITokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)

  def tokenToNode(token: IToken, begin: String, end: String, id: String): Node =
    <token
      id={ id }
      form={ token.getSurface }
      characterOffsetBegin={ begin }
      characterOffsetEnd={ end }
      pos={ token.getPartOfSpeechLevel1 }
      pos1={ token.getPartOfSpeechLevel2 }
      pos2={ token.getPartOfSpeechLevel3 }
      pos3={ token.getPartOfSpeechLevel4 }
      cType={ token.getConjugationType }
      cForm={ token.getConjugationForm }
      lemma={ token.getBaseForm }
      yomi={ token.getReading }
      pron={ token.getPronunciation }/>

  override def requirementsSatisfied = Set(JaRequirement.TokenizeWithIPA)
}

class JumanKuromojiAnnotator(name: String, props: Properties)
    extends KuromojiAnnotator(name, props) {

  type T = JToken

  val tokenizer = new JTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)

  def tokenToNode(token: JToken, begin: String, end: String, id: String): Node =
    <token
      id={ id }
      form={ token.getSurface }
      characterOffsetBegin={ begin }
      characterOffsetEnd={ end }
      pos={ token.getPartOfSpeechLevel1 }
      pos1={ token.getPartOfSpeechLevel2 }
      cType={ token.getPartOfSpeechLevel3 }
      cForm={ token.getPartOfSpeechLevel4 }
      lemma={ token.getBaseForm }
      yomi={ token.getReading }
      misc={ token.getSemanticInformation }/>

  override def requirementsSatisfied = Set(JaRequirement.TokenizeWithJumandic)
}

class UnidicKuromojiAnnotator(name: String, props: Properties)
    extends KuromojiAnnotator(name, props) {

  type T = UToken

  val tokenizer = new UTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)

  def tokenToNode(token: UToken, begin: String, end: String, id: String): Node =
    <token
      id={ id }
      form={ token.getSurface }
      characterOffsetBegin={ begin }
      characterOffsetEnd={ end }
      pos={ token.getPartOfSpeechLevel1 }
      pos1={ token.getPartOfSpeechLevel2 }
      pos2={ token.getPartOfSpeechLevel3 }
      pos3={ token.getPartOfSpeechLevel4 }
      cType={ token.getConjugationType }
      cForm={ token.getConjugationForm }
      lForm={ token.getLemmaReadingForm }
      lemma={ token.getLemma }
      orth={ token.getWrittenForm }
      pron={ token.getPronunciation }
      orthBase={ token.getWrittenBaseForm }
      pronBase={ token.getPronunciationBaseForm }
      goshu={ token.getLanguageType }
      iType={ token.getInitialSoundAlterationType }
      iForm={ token.getInitialSoundAlterationForm }
      fType={ token.getFinalSoundAlterationType }
      fForm={ token.getFinalSoundAlterationForm }/>

  override def requirementsSatisfied = Set(JaRequirement.TokenizeWithJumandic)
}

object KuromojiAnnotator extends AnnotatorCompanion[KuromojiAnnotator] {

  def defaultDic = "ipa"

  override def fromProps(name: String, props: Properties) = {
    val key = name + ".dic"
    val dic = PropertiesUtil.findProperty(key, props) getOrElse defaultDic
    dic match {
      case "ipa" => new IPAKuromojiAnnotator(name, props)
      case "juman" => new JumanKuromojiAnnotator(name, props)
      case "unidic" => new UnidicKuromojiAnnotator(name, props)
      case _ =>
        System.err.println(s"WARNING: Dictionary ${dic} is unsupported in kuromoji. Use ipadic...")
        new IPAKuromojiAnnotator(name, props)
    }
  }
}
