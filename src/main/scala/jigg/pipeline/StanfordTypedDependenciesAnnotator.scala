package jigg.pipeline

/*
 Copyright 2013-2018 Hiroshi Noji

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
import scala.collection.JavaConverters._
import scala.collection.immutable.SortedMap
import scala.collection.mutable.ArrayBuffer
import scala.xml._
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

import edu.stanford.nlp.parser.lexparser.EnglishTreebankParserParams
import edu.stanford.nlp.pipeline.ParserAnnotatorUtils
import edu.stanford.nlp.trees.{GrammaticalRelation, GrammaticalStructure, Tree, Trees, TypedDependency}
import edu.stanford.nlp.ling.{CoreAnnotations, IndexedWord}
import edu.stanford.nlp.util.{CoreMap, Filters}
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphCoreAnnotations}

import edu.stanford.nlp.process.CoreLabelTokenFactory

import edu.stanford.nlp.{pipeline => core}

class StanfordTypedDependenciesAnnotator(
  override val name: String,
  override val props: Properties) extends SentencesAnnotator {

  @Prop(gloss = "Annotation style for the uncollapsed dependencies (SD|UD). See below in detail.") var style = "SD"
  @Prop(gloss = "Language (currently, only supports 'en')") var lang = "en"

  readProps()

  val generateOriginalDependencies = style == "SD"

  val tlpp = new EnglishTreebankParserParams
  tlpp.setGenerateOriginalDependencies(generateOriginalDependencies)
  val tlp = tlpp.treebankLanguagePack
  val punctFilter = Filters.acceptFilter[String]
  val gsf = tlp.grammaticalStructureFactory(punctFilter, tlpp.typedDependencyHeadFinder)

  val numGen = Stream.from(0).iterator

  val SC = StanfordCoreNLPAnnotator

  def newSentenceAnnotation(sentenceNode: Node) = {

    val coreSent = mkCoreSentence(sentenceNode)
    val tree = mkTree(sentenceNode)

    ParserAnnotatorUtils.fillInParseAnnotations(
      false,
      true,
      gsf,
      coreSent,
      Seq(tree).asJava,
      GrammaticalStructure.Extras.NONE)

    val basicGraph = coreSent get classOf[SemanticGraphCoreAnnotations.BasicDependenciesAnnotation]
    val collapsed = coreSent get classOf[SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation]
    val cc = coreSent get classOf[SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation]
    val enhanced = coreSent get classOf[SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation]
    val plus = coreSent get classOf[SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation]

    def toNode(graph: SemanticGraph, depType: String) =
      SC.semanticGraphToDependenciesNode(sentenceNode, graph, depType, name)

    val basicNode = toNode(basicGraph, SC.basicDepType)
    val collapsedNode = toNode(collapsed, SC.collapsedDepType)
    val ccNode = toNode(cc, SC.ccCollapsedDepType)
    val enhancedNode = toNode(enhanced, SC.enhancedDepType)
    val enhancedPPNode = toNode(plus, SC.enhancedPlusPlusDepType)

    sentenceNode addOrOverwriteChild (
      Seq(basicNode, collapsedNode, ccNode, enhancedNode, enhancedPPNode), Some("type"))
  }

  def mkCoreSentence(sentenceNode: Node): CoreMap = {
    val tf = new CoreLabelTokenFactory

    val tokens = (sentenceNode \ "tokens").head \ "token"

    val coreTokens = tokens map { t =>
      val begin = (t \@ "characterOffsetBegin").toInt
      val end = (t \@ "characterOffsetEnd").toInt
      val token = tf.makeToken(t \@ "form", begin, end - begin)
      token set (classOf[CoreAnnotations.PartOfSpeechAnnotation], t \@ "pos")
      token
    }

    val coreSent: CoreMap = new core.Annotation(sentenceNode.textElem)
    coreSent set (classOf[CoreAnnotations.TokensAnnotation], coreTokens.asJava)
    coreSent set (classOf[CoreAnnotations.SentenceIndexAnnotation], new Integer(numGen.next))

    coreSent
  }

  def mkTree(sentenceNode: Node): Tree = {

    val tokens = (sentenceNode \ "tokens").head \ "token"
    val parse = (sentenceNode \ "parse").head
    val parseStr = StanfordCoreNLPAnnotator.parseStr(tokens, parse)
    Trees.readTree(parseStr)
  }

  override def requires = Set(Requirement.Parse)
  override def requirementsSatisfied = Set(
    Requirement.BasicDependencies,
    Requirement.CollapsedDependencies,
    Requirement.CollapsedCCProcessedDependencies,
    Requirement.EnhancedDependencies,
    Requirement.EnhancedPlusPlusDependencies)
}
