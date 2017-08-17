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

import scala.collection.mutable.ArrayBuffer
import scala.xml._
import scala.sys.process.Process

import jigg.nlp.ccg.lexicon.{EnglishCCGBankReader, ParseTree}
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode


class EasyCCGAnnotator(override val name: String, override val props: Properties)
    extends AnnotatingSentencesInParallel { self =>

  @Prop(gloss = "Path to easyccg.jar", required = true) var path = ""
  @Prop(gloss = "Path to the model directory (containing bias, capitals, etc)", required = true) var model = ""

  readProps()

  localAnnotators // instantiate lazy val here

  def mkLocalAnnotator = new LocalEasyCCGAnnotator

  class LocalEasyCCGAnnotator
      extends SentencesAnnotator with LocalAnnotator with IOCreator {

    def command = s"java -jar ${path} -m ${model}"

    override def launchTesters = Seq(
      LaunchTester("", _ == "Parsing...", _ => true))
    override def defaultArgs = Seq("-i", "POSandNERtagged", "-o", "extended")
    def softwareUrl = "http://homepages.inf.ed.ac.uk/s1049478/easyccg.html"

    val parser = mkIO()
    override def close() = parser.close()

    override def newSentenceAnnotation(sentence: Node): Node = {

      val output = run(mkInput(sentence))
      val tree = mkTree(output)

      annotateCCGSpans(sentence, tree)
    }

    private def mkInput(sentence: Node): String = {
      val tokenSeq = sentence \\ "token"
      tokenSeq.map { t => (t \@ "form") + "|x|x" } mkString " "
    }

    protected def run(text: String): String = {
      parser.safeWriteWithFlush(text)
      parser.readUntil(_.startsWith("(<")).last
    }

    private def mkTree(line: String): ParseTree[String] = {
      val reader = new EnglishCCGBankReader
      reader.readParseTree(line, true)
    }

    def annotateCCGSpans(sentence: Node, tree: ParseTree[String]): Node = {
      val treeWithIds = tree.map { (Annotation.CCGSpan.nextId, _) }
      treeWithIds.setSpans()
      val tokens = sentence \ "tokens" \ "token"

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
            tokens(idx) \@ "id"
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
      sentence addChild (
        <ccg annotators={ name } root={ root } id={ Annotation.CCG.nextId }>{ spans }</ccg>
      )
    }
  }

  override def requires = Set(Requirement.Ssplit, Requirement.Tokenize)

  override def requirementsSatisfied = Set(Requirement.CCGDerivation)
}

