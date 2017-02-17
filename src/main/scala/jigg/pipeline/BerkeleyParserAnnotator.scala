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


import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

import java.util.Properties
import java.util.{List => JList}

import scala.xml._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

import edu.berkeley.nlp.PCFGLA.{
  BerkeleyParser, CoarseToFineMaxRuleParser, ParserData, TreeAnnotations}

import edu.berkeley.nlp.syntax.Tree
import edu.berkeley.nlp.util.{MyMethod, Numberer}

trait BerkeleyParserAnnotator extends SentencesAnnotator with ParallelAnnotator {

  def threshold = 1.0 // this value is used without explanation in original BerkeleyParser.java

  def keepFunctionLabels = true // but probably the model does not output function labels.

  def defaultGrFileName = "eng_sm6.gr"

  @Prop(gloss = "Grammar file") var grFileName = ""
  @Prop(gloss = "Use annotated POS (by another annotator)") var usePOS =
    BerkeleyParserAnnotator.defaultUsePOS
  @Prop(gloss = "Compute viterbi derivation instead of max-rule tree (Default: max-rule)") var viterbi = false
  @Prop(gloss = "Set thresholds for accuracy. (Default: set thresholds for efficiency)") var accurate = false
  @Prop(gloss = "Use variational rule score approximation instead of max-rule (Default: false)") var variational = false

  readProps()

  lazy val parser: Parser = new QueueParser

  override def description = s"""${super.description}

  A wrapper for Berkeley parser. The feature is that this wrapper is implemented to be
  thread-safe. To do this, the wrapper keeps many parser instances (the number can be
  specified by customizing -nThreads).

  The path to the model file can be changed by setting -${name}.grFileName.

  If -${name}.usePOS is true, the annotator assumes the POS annotation is already
  performed, and the parser builds a tree based on the assigned POS tags.
  Otherwise, the parser performs joint inference of POS tagging and parsing, which
  is the default behavior.
"""

  // lazy val parser = mkParser()

  override def init() = {
    parser // init here, to output help message without loading
  }

  def treeToNode(tree: Tree[String], tokenSeq: Seq[Node], sentenceId: String): Node = {
    val trees = tree.getChildren.asScala // ignore root node

    var id = -1
    var tokIdx = -1

    def nextId = { id += 1; sentenceId + "_berksp" + id }
    def nextTokId = { tokIdx += 1; tokenSeq(tokIdx) \@ "id" }

    val addId = new MyMethod[Tree[String], (String, String)] {
      def call(t: Tree[String]): (String, String) = t match {
        case t if t.isPreTerminal => (t.getLabel, nextTokId) // preterminal points to token id; this is ok since `transformNodes` always proceeds left-to-right manner
        case t if t.isLeaf => (t.getLabel, "")
        case t => (t.getLabel, nextId)
      }
    }

    val treesWithId: Seq[Tree[(String, String)]] =
      trees map { _ transformNodesUsingNode addId }
    val root = treesWithId map (_.getLabel._2) mkString " "

    val spans = new ArrayBuffer[Node]

    def traverseTree[A, B](t: Tree[A])(f: Tree[A]=>B): Unit = {
      f(t)
      t.getChildren.asScala foreach (traverseTree(_)(f))
    }

    treesWithId foreach { treeWithId =>
      traverseTree(treeWithId) { t =>
        val label = t.getLabel
        val children = t.getChildren.asScala map (_.getLabel._2) mkString " "

        if (!t.isLeaf && !t.isPreTerminal)
          spans += <span id={ label._2 } symbol={ label._1 } children={ children } />
      }
    }
    <parse annotators={ name } root={ root }>{ spans }</parse>
  }

  def safeParse(sentence: JList[String], pos: JList[String]): Tree[String] = {
    val tree = parser.parse(sentence, pos)
    if (tree.size == 1 && !sentence.isEmpty) throw new AnnotationError("Failed to parse.")
    else tree
  }

  trait Parser {
    def parse(sentence: JList[String], pos: JList[String]): Tree[String]
  }

  class QueueParser extends Parser {
    val parserQueue = new ResourceQueue(nThreads, mkParser _) {
      def postProcess(parser: CoarseToFineMaxRuleParser, e: ProcessError) = {
        queue.put(parser)
        throw e
      }
    }

    def mkParser(): CoarseToFineMaxRuleParser = {
      val gr = grFileName match {
        case "" =>
          System.err.println(s"No grammar file is given. Try to search from default path: ${defaultGrFileName}.")
          defaultGrFileName
        case _ => grFileName
      }

      val parserData = ParserData Load gr
      if (parserData == null) {
        argumentError("grFileName", s"""Failed to load grammar from $gr.
You can download the English model file from:
  https://github.com/slavpetrov/berkeleyparser/raw/master/eng_sm6.gr
""")
      }

      val grammar = parserData.getGrammar
      val lexicon = parserData.getLexicon
      Numberer.setNumberers(parserData.getNumbs())

      new CoarseToFineMaxRuleParser(
        grammar,
        lexicon,
        threshold,
        -1,
        viterbi,
        false, // substates are not supported
        false, // scores are not supported
        accurate,
        variational,
        true, // copied from BerkeleyParser.java
        true) // copied from BerkeleyParser.java
    }

    def parse(sentence: JList[String], pos: JList[String]): Tree[String] = {
      parserQueue using { parser =>
        val deriv = parser.getBestConstrainedParse(sentence, pos, null)

        TreeAnnotations.unAnnotateTree(deriv, keepFunctionLabels)
      }
    }
  }
}

class BerkeleyParserAnnotatorFromToken(
  override val name: String,
  override val props: Properties) extends BerkeleyParserAnnotator {

  override def newSentenceAnnotation(sentence: Node) = {

    def addPOS(tokenSeq: NodeSeq, tree: Tree[String]): NodeSeq = {
      val preterminals = tree.getPreTerminals.asScala
      (0 until tokenSeq.size) map { i =>
        tokenSeq(i) addAttribute ("pos", preterminals(i).getLabel)
      }
    }

    val tokens = (sentence \ "tokens").head
    val tokenSeq = tokens \ "token"

    val tree = safeParse(tokenSeq.map(_ \@ "form").asJava, null)

    val taggedSeq = addPOS(tokenSeq, tree)

    val newTokens = {
      val nameAdded = tokens addAnnotatorName name
      nameAdded replaceChild taggedSeq
    }
    val parseNode = treeToNode(tree, taggedSeq, sentence \@ "id")

    // TODO: this may be customized with props?
    sentence addOrOverwriteChild Seq(newTokens, parseNode)
  }

  override def requires = Set(Requirement.Tokenize)
  override def requirementsSatisfied = Set(Requirement.POS, Requirement.Parse)
}

class BerkeleyParserAnnotatorFromPOS(
  override val name: String,
  override val props: Properties) extends BerkeleyParserAnnotator {

  override def newSentenceAnnotation(sentence: Node) = {
    val tokens = sentence \ "tokens"
    val tokenSeq = tokens \ "token"
    val posSeq = tokenSeq.map(_ \@ "pos").asJava

    val tree = safeParse(tokenSeq.map(_+"").asJava, posSeq)

    val parseNode = treeToNode(tree, tokenSeq, sentence \@ "id")

    // TODO: is it ok to override in default?
    sentence addOrOverwriteChild Seq(parseNode)
  }

  override def requires = Set(Requirement.POS)
  override def requirementsSatisfied = Set(Requirement.Parse)
}

object BerkeleyParserAnnotator extends AnnotatorCompanion[BerkeleyParserAnnotator] {

  def defaultUsePOS = false

  override def fromProps(name: String, props: Properties) = {
    val usepos = name + ".usePOS"
    PropertiesUtil.findProperty(usepos, props) getOrElse (defaultUsePOS+"") match {
      case "true" => new BerkeleyParserAnnotatorFromPOS(name, props)
      case "false" => new BerkeleyParserAnnotatorFromToken(name, props)
    }
  }
}
