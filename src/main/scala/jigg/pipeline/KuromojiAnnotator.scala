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
import scala.reflect.ClassTag
import collection.JavaConversions._
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

import com.atilika.kuromoji.{TokenBase, TokenizerBase}
import com.atilika.kuromoji.ipadic.{Token=>IToken, Tokenizer=>ITokenizer}
import com.atilika.kuromoji.jumandic.{Token=>JToken, Tokenizer=>JTokenizer}
import com.atilika.kuromoji.unidic.{Token=>UToken, Tokenizer=>UTokenizer}

abstract class KuromojiAnnotator[T<:TokenBase](
  override val name: String,
  override val props: Properties)
    extends KuromojiTokenAnnotator[T]
    with KuromojiPOSAnnotator[T] {

  // @Prop(gloss = "Which dictionary do you use? Currently supported: ipa|juman|unidic") var dic = KuromojiAnnotator.defaultDic
  // readProps()

  override def newSentenceAnnotation(sentence: Node): Node = {
    val tokenizedSentence = tokenize(sentence.text)
    val sindex = (sentence \ "@id").toString
    val tokensNode = mkTokensNode(tokenizedSentence, sindex)
    val taggedNode = addTags(tokensNode, tokenizedSentence)
    sentence addChild taggedNode
  }
}


sealed trait KuromojiBaseAnnotator[T<:TokenBase] extends SentencesAnnotator {

  @Prop(gloss = "Which dictionary do you use? Currently supported: ipa|juman|unidic") var dic = KuromojiAnnotator.defaultDic
  readProps()

  protected def tokenize(text: String): Seq[T]

  override def requires = Set(Requirement.Ssplit)
  // Tokenize requirment would be satified by all sub-annotators, though POS annotators
  // later additionally satisfy TokenizeWithXXX requirement.
  override def requirementsSatisfied = Set(Requirement.Tokenize)
}

trait KuromojiIPABaseAnnotator extends KuromojiBaseAnnotator[IToken] {
  val tokenizer = new ITokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)
}

trait KuromojiJumanBaseAnnotator extends KuromojiBaseAnnotator[JToken] {
  val tokenizer = new JTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)
}

trait KuromojiUnidicBaseAnnotator extends KuromojiBaseAnnotator[UToken] {
  val tokenizer = new UTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)
}


trait KuromojiTokenAnnotator[T<:TokenBase] extends KuromojiBaseAnnotator[T] {

  def mkid(sindex: String, tindex: Int) = sindex + "_" + tindex

  override def newSentenceAnnotation(sentence: Node): Node = {
    val sindex = (sentence \ "@id").toString
    val tokenizedSentence = tokenize(sentence.text)
    sentence addChild mkTokensNode(tokenizedSentence, sindex)
  }

  def mkTokensNode(tokens: Seq[T], sindex: String): Node = {
    var tokenIndex = -1
    val tokenNodes = tokens map { case token =>
      val begin = token.getPosition
      val end = begin + token.getSurface.size
      tokenIndex += 1
      <token
      id={ mkid(sindex, tokenIndex) }
      form={ token.getSurface }
      characterOffsetBegin={ begin+"" }
      characterOffsetEnd={ end+"" }/>
    }
    <tokens annotators={ name }>{ tokenNodes }</tokens>
  }
}

trait KuromojiPOSAnnotator[T<:TokenBase] extends KuromojiBaseAnnotator[T] {

  override def newSentenceAnnotation(sentence: Node): Node = {
    val tokenizedSentence = tokenize(sentence.text)
    val tokensNode = (sentence \ "tokens").head
    val taggedTokensNode = addTags(tokensNode, tokenizedSentence)
    sentence addOrOverwriteChild (taggedTokensNode addAnnotatorName name)
  }

  def addTags(tokensNode: Node, tokens: Seq[T]): Node = {
    val tokenNodes = tokensNode \ "token"
    assert(tokenNodes.size == tokens.size)
    val newTokenNodes = tokenNodes zip tokens map { case (tokenNode, token) =>
      decorate(tokenNode, token)
    }
    tokensNode replaceChild newTokenNodes
  }

  protected def decorate(tokenNode: Node, token: T): Node
}

// TODO: support naist-jdic annotator, possibly by making a trait implementing tokenToNodes,
// which is used both for ipa and naist.
trait KuromojiIPAPOSAnnotator
    extends KuromojiPOSAnnotator[IToken] with KuromojiIPABaseAnnotator {
  def decorate(tokenNode: Node, token: IToken): Node =
    tokenNode.addAttributes(Map(
      "pos" -> token.getPartOfSpeechLevel1,
      "pos1" -> token.getPartOfSpeechLevel2,
      "pos2" -> token.getPartOfSpeechLevel3,
      "pos3" -> token.getPartOfSpeechLevel4,
      "cType" -> token.getConjugationType,
      "cForm" -> token.getConjugationForm,
      "lemma" -> token.getBaseForm,
      "yomi" -> token.getReading,
      "pron" -> token.getPronunciation))

  override def requirementsSatisfied =
    super.requirementsSatisfied | Set(JaRequirement.TokenizeWithIPA)
}

trait KuromojiJumanPOSAnnotator
    extends KuromojiPOSAnnotator[JToken] with KuromojiJumanBaseAnnotator {
  def decorate(tokenNode: Node, token: JToken): Node =
    tokenNode.addAttributes(Map(
      "pos" -> token.getPartOfSpeechLevel1,
      "pos1" -> token.getPartOfSpeechLevel2,
      "cType" -> token.getPartOfSpeechLevel3,
      "cForm" -> token.getPartOfSpeechLevel4,
      "lemma" -> token.getBaseForm,
      "yomi" -> token.getReading,
      "misc" -> token.getSemanticInformation))

  override def requirementsSatisfied =
    super.requirementsSatisfied | Set(JaRequirement.TokenizeWithJumandic)
}

trait KuromojiUnidicPOSAnnotator
    extends KuromojiPOSAnnotator[UToken] with KuromojiUnidicBaseAnnotator {
  def decorate(tokenNode: Node, token: UToken): Node =
    tokenNode.addAttributes(Map(
      "pos" -> token.getPartOfSpeechLevel1,
      "pos1" -> token.getPartOfSpeechLevel2,
      "pos2" -> token.getPartOfSpeechLevel3,
      "pos3" -> token.getPartOfSpeechLevel4,
      "cType" -> token.getConjugationType,
      "cForm" -> token.getConjugationForm,
      "lForm" -> token.getLemmaReadingForm,
      "lemma" -> token.getLemma,
      "orth" -> token.getWrittenForm,
      "pron" -> token.getPronunciation,
      "orthBase" -> token.getWrittenBaseForm,
      "pronBase" -> token.getPronunciationBaseForm,
      "goshu" -> token.getLanguageType,
      "iType" -> token.getInitialSoundAlterationType,
      "iForm" -> token.getInitialSoundAlterationForm,
      "fType" -> token.getFinalSoundAlterationType,
      "fForm" -> token.getFinalSoundAlterationForm))

  override def requirementsSatisfied =
    super.requirementsSatisfied | Set(JaRequirement.TokenizeWithUnidic)
}

abstract class KuromojiCompanionBase[A<:Annotator](implicit m: ClassTag[A])
    extends AnnotatorCompanion[A] {

  def defaultDic = "ipa"

  def mkIPA(name: String, props: Properties): A
  def mkJuman(name: String, props: Properties): A
  def mkUnidic(name: String, props: Properties): A

  override def fromProps(name: String, props: Properties) = {
    val key = name + ".dic"
    val dic = PropertiesUtil.findProperty(key, props) getOrElse defaultDic
    dic match {
      case "ipa" => mkIPA(name, props)
      case "juman" => mkJuman(name, props)
      case "unidic" => mkUnidic(name, props)
      case _ =>
        System.err.println(s"WARNING: Dictionary ${dic} is unsupported in kuromoji. Use ipadic...")
        mkIPA(name, props)
    }
  }
}

object KuromojiAnnotator extends KuromojiCompanionBase[KuromojiAnnotator[_]] {

  def mkIPA(name: String, props: Properties) =
    new KuromojiAnnotator[IToken](name, props) with KuromojiIPAPOSAnnotator

  def mkJuman(name: String, props: Properties) =
    new KuromojiAnnotator[JToken](name, props) with KuromojiJumanPOSAnnotator

  def mkUnidic(name: String, props: Properties) =
    new KuromojiAnnotator[UToken](name, props) with KuromojiUnidicPOSAnnotator
}

object KuromojiTokenAnnotator extends KuromojiCompanionBase[KuromojiTokenAnnotator[_]] {

  abstract class TokenAnnotator[T<:TokenBase](
    override val name: String, override val props: Properties)
      extends KuromojiTokenAnnotator[T]

  def mkIPA(name: String, props: Properties) =
    new TokenAnnotator[IToken](name, props) with KuromojiIPABaseAnnotator
  def mkJuman(name: String, props: Properties) =
    new TokenAnnotator[JToken](name, props) with KuromojiJumanBaseAnnotator
  def mkUnidic(name: String, props: Properties) =
    new TokenAnnotator[UToken](name, props) with KuromojiUnidicBaseAnnotator
}

object KuromojiPOSAnnotator extends KuromojiCompanionBase[KuromojiPOSAnnotator[_]] {

  abstract class POSAnnotator[T<:TokenBase](
    override val name: String, override val props: Properties)
      extends KuromojiPOSAnnotator[T]

  def mkIPA(name: String, props: Properties) =
    new POSAnnotator[IToken](name, props) with KuromojiIPAPOSAnnotator
  def mkJuman(name: String, props: Properties) =
    new POSAnnotator[JToken](name, props) with KuromojiJumanPOSAnnotator
  def mkUnidic(name: String, props: Properties) =
    new POSAnnotator[UToken](name, props) with KuromojiUnidicPOSAnnotator
}
