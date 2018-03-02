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

// Base class for kuromoji doing both tokenization and pos-tagging
abstract class KuromojiFullAnnotator[T<:TokenBase](
  override val name: String,
  override val props: Properties)
    extends KuromojiTokenBaseAnnotator[T]
    with KuromojiPOSBaseAnnotator[T] {

  override def newSentenceAnnotation(sentence: Node): Node = {
    val tokenizedSentence = tokenize(sentence.text)
    val sindex = (sentence \ "@id").toString
    val tokensNode = mkTokensNode(tokenizedSentence, sindex)
    val taggedNode = addTags(tokensNode, tokenizedSentence)
    sentence addChild taggedNode
  }
}

// Base class for kuromoji doing only tokenization
abstract class KuromojiTokenAnnotator[T<:TokenBase](
  override val name: String, override val props: Properties)
    extends KuromojiTokenBaseAnnotator[T]

// Base class for kuromoji doing only pos-tagging
abstract class KuromojiPOSAnnotator[T<:TokenBase](
  override val name: String, override val props: Properties)
    extends KuromojiPOSBaseAnnotator[T]

sealed trait KuromojiAnnotator[T<:TokenBase] extends SentencesAnnotator {

  @Prop(gloss = "Which dictionary do you use? Currently supported: ipa|juman|unidic") var dic = KuromojiAnnotator.defaultDic
  readProps()

  protected def tokenize(text: String): Seq[T]

  override def requires = Set(Requirement.Ssplit)
  override def requirementsSatisfied = Set(Requirement.Tokenize)
}

trait KuromojiIPABaseAnnotator extends KuromojiAnnotator[IToken] {
  val tokenizer = new ITokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)
}

trait KuromojiJumanBaseAnnotator extends KuromojiAnnotator[JToken] {
  val tokenizer = new JTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)
}

trait KuromojiUnidicBaseAnnotator extends KuromojiAnnotator[UToken] {
  val tokenizer = new UTokenizer
  def tokenize(text: String) = tokenizer.tokenize(text)
}

trait KuromojiTokenBaseAnnotator[T<:TokenBase] extends KuromojiAnnotator[T] {

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

trait KuromojiPOSBaseAnnotator[T<:TokenBase] extends KuromojiAnnotator[T] {

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
    extends KuromojiPOSBaseAnnotator[IToken] with KuromojiIPABaseAnnotator {
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
    extends KuromojiPOSBaseAnnotator[JToken] with KuromojiJumanBaseAnnotator {
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
    extends KuromojiPOSBaseAnnotator[UToken] with KuromojiUnidicBaseAnnotator {
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

object KuromojiAnnotator extends AnnotatorCompanion[KuromojiAnnotator[_]] {

  def defaultDic = "ipa"

  trait Launcher {
    def name: String
    def props: Properties

    val key = name + ".dic"
    val dic = PropertiesUtil.findProperty(key, props) getOrElse defaultDic

    def launch(): KuromojiAnnotator[_] = dic match {
      case "ipa" => mkIPA()
      case "juman" => mkJuman()
      case "unidic" => mkUnidic()
    }

    def mkIPA(): KuromojiAnnotator[_]
    def mkJuman(): KuromojiAnnotator[_]
    def mkUnidic(): KuromojiAnnotator[_]
  }

  class FullLauncher(val name: String, val props: Properties) extends Launcher {
    def mkIPA() =
      new KuromojiFullAnnotator[IToken](name, props) with KuromojiIPAPOSAnnotator
    def mkJuman() =
      new KuromojiFullAnnotator[JToken](name, props) with KuromojiJumanPOSAnnotator
    def mkUnidic() =
      new KuromojiFullAnnotator[UToken](name, props) with KuromojiUnidicPOSAnnotator
  }

  class TokenLauncher(val name: String, val props: Properties) extends Launcher {
    def mkIPA() =
      new KuromojiTokenAnnotator[IToken](name, props) with KuromojiIPABaseAnnotator
    def mkJuman() =
      new KuromojiTokenAnnotator[JToken](name, props) with KuromojiJumanBaseAnnotator
    def mkUnidic() =
      new KuromojiTokenAnnotator[UToken](name, props) with KuromojiUnidicBaseAnnotator
  }

  class POSLauncher(val name: String, val props: Properties) extends Launcher {
    def mkIPA() =
      new KuromojiPOSAnnotator[IToken](name, props) with KuromojiIPAPOSAnnotator
    def mkJuman() =
      new KuromojiPOSAnnotator[JToken](name, props) with KuromojiJumanPOSAnnotator
    def mkUnidic() =
      new KuromojiPOSAnnotator[UToken](name, props) with KuromojiUnidicPOSAnnotator
  }

  override def fromProps(_name: String, props: Properties) = {
    val (name, options) = _name.indexOf('[') match {
      case -1 => (_name, "")
      case b => (_name.substring(0, b), _name.substring(b+1, _name.size-1))
    }
    val launcher = options match {
      case "tokenize" => new TokenLauncher(name, props)
      case "pos" => new POSLauncher(name, props)
      case "tokenize,pos"|"" => new FullLauncher(name, props)
      case _ => throw new ArgumentError(
        s"Invalid options for kuromoji (${_name}), which should be like kuromoji, kuromoji[tokenize], or kuromoji[pos]")
    }
    launcher.launch()
  }
}
