package jigg.pipeline

/*
 Copyright 2013-2017 Hiroshi Noji

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

import java.io.{ByteArrayInputStream, File}
import java.util.Properties

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.xml._
import scala.sys.process.Process

import jigg.nlp.ccg.lexicon.{EnglishCCGBankReader, ParseTree}
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

import uk.ac.ed.easyccg.syntax.Category
import uk.ac.ed.easyccg.syntax.Combinator.RuleType
import uk.ac.ed.easyccg.syntax.ParsePrinter
import uk.ac.ed.easyccg.syntax.Parser
import uk.ac.ed.easyccg.syntax.ParserAStar
import uk.ac.ed.easyccg.syntax.SyntaxTreeNode
import uk.ac.ed.easyccg.syntax.TaggerEmbeddings
import uk.ac.ed.easyccg.main.EasyCCG

class EasyCCGAnnotator(override val name: String, override val props: Properties)
    extends AnnotatingSentencesInParallel {

  @Prop(gloss = "Path to the model directory (containing bias, capitals, etc)", required = true) var model = ""
  @Prop(gloss = "Outputs k-best derivations if this value > 1") var kBest = 1
  // TODO: Support other options defined in EasyCCG (e.g., max length?)
  val rootCategories = Seq("S[dcl]", "S[wq]", "S[q]", "S[qem]", "NP")
  val maxLength = 70
  val superTaggerBeam = 0.0001
  val maxTagsPerWord = 50
  val nbestbeam = 0.0

  readProps()

  val printer = ParsePrinter.EXTENDED_CCGBANK_PRINTER
  val reader = new EnglishCCGBankReader

  override def init() = {
    localAnnotators
  }

  class LocalEasyCCGAnnotator extends SentencesAnnotator with LocalAnnotator {

    val parser: WrappedParser = buildParser()

    def newSentenceAnnotation(sentence: Node): Node = {

      val tokenseq = sentence \ "tokens" \ "token"
      val line = tokenseq map (t => (t \@ "form") + "|*|*") mkString " "
      val output = parser.parse(line).split("\n")
      assert(output.size % 2 == 0)
      val parseStrs = output.indices collect { case i if i % 2 == 1 => output(i) }
      assert(parseStrs.size <= kBest)

      //val ccgs = parses.map { parse => mkCCGNode(tokenseq, parse) }
      val ccgs = parseStrs.map { line =>
        val tree = reader.readParseTree(line, true)
        mkCCGSpans(tokenseq, tree)
      }

      sentence addChild ccgs
    }

    def mkCCGSpans(tokenseq: Seq[Node], tree: ParseTree[String]): Node = {
      val treeWithIds = tree.map { (Annotation.CCGSpan.nextId, _) }
      treeWithIds.setSpans()
      val root = treeWithIds.label._1

      val spans = new ArrayBuffer[Node]
      treeWithIds.foreachTree { t =>
        val id = t.label._1
        val l = t.label._2

        // l in nonterminal looks like: "T S[dcl] ba 1 2"
        // l in terminal looks like: "L NP I I x x O NP"
        val items = l.split(" ")

        val children = items(0) match {
          case "T" => t.children.map(_.label._1).mkString(" ")
          case "L" =>
            val idx = t.span.get.begin
            tokenseq(idx) \@ "id"
        }

        val rule = items(0) match {
          case "T" => items(2)
          case "L" => null
        }
        val symbol: String = items(1)

        spans += <span
        id={ id }
        begin={ t.span.get.begin + "" }
        end={ t.span.get.end + "" }
        symbol={ symbol }
        rule={ rule }
        children={ children }/>
      }
      <ccg annotators={ name } root={ root } id={ Annotation.CCG.nextId }>{ spans }</ccg>
    }

    def buildParser(): WrappedParser = new WrappedParser {
      val parser = new ParserAStar(
        new TaggerEmbeddings(new File(model), maxLength, superTaggerBeam, maxTagsPerWord),
        maxLength,
        kBest,
        nbestbeam,
        EasyCCG.InputFormat.POSANDNERTAGGED,
        rootCategories.asJava,
        new File(model, "unaryRules"),
        new File(model, "binaryRules"),
        new File(model, "seenRules"))

      def parse(line: String): String = {
        val parses = parser.parse(line)
        printer.print(parses, 0)
      }
    }
  }

  /** Abstracts `parse` method. Useful for unit-testing.
    *
    * Inherited in `buildParser` of local annotator.
    * Output string is the output of the printer for K-best outputs, which look like:
    * ID=1
    * (T S[dcl] ... )
    * ID=1
    * (T S[dcl] ... )
    */
  trait WrappedParser {
    def parse(line: String): String
  }

  def mkLocalAnnotator = new LocalEasyCCGAnnotator()

  override def requires = Set(Requirement.Ssplit, Requirement.Tokenize)
  override def requirementsSatisfied = Set(Requirement.CCGDerivation)
}

