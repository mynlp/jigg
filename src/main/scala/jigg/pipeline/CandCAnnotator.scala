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

import scala.xml._
import scala.sys.process.Process

import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

class CandCPOSAnnotator(override val name: String, override val props: Properties)
    extends AnnotatingSentencesInParallel { self =>

  @Prop(gloss = "Path to candc pos tagger binary (/path/to/bin/pos)", required = true) var path = ""
  @Prop(gloss = "Path to candc models directory (containing parser, pos, etc)", required = true) var models = ""

  readProps()

  localAnnotators

  def mkLocalAnnotator = new LocalCandCPOSAnnotator

  class LocalCandCPOSAnnotator
      extends SentencesAnnotator with LocalAnnotator with IOCreator {

    val modelPath = new File(models, "pos").getPath
    def command = s"${self.path} --model ${modelPath}"

    override def launchTesters = Seq(
      LaunchTester("AAAAA", _ startsWith "AAAAA", _ startsWith "AAAAA")
    )
    def softwareUrl = "http://www.cl.cam.ac.uk/~sc609/candc-1.00.html"

    val tagger = mkIO()
    override def close = tagger.close()

    override def newSentenceAnnotation(sentence: Node): Node = {
      val tokens = (sentence \ "tokens").head
      val tokenSeq = tokens.child
      val input = tokenSeq map (_ \@ "form") mkString " "
      val output = runTagger(input)

      val tags = output.split(" ").map { t => t.drop(t.lastIndexOf('|')+1) }

      val taggedTokenSeq =
        tokenSeq zip tags map { case (token, tag) => token.addAttribute("pos", tag) }

      val newTokens = {
        val nameAdded = tokens addAnnotatorName name
        nameAdded replaceChild taggedTokenSeq
      }
      sentence addOrOverwriteChild newTokens
    }

    def runTagger(text: String): String = {
      tagger.safeWriteWithFlush(text)
      val out = tagger.readUntil(_=>true) // read only single line
      out(0)
    }
  }

  override def requires = Set(Requirement.Tokenize, Requirement.Ssplit)
  override def requirementsSatisfied = Set(Requirement.POS)
}

class CandCAnnotator(override val name: String, override val props: Properties)
    extends AnnotatingSentencesInParallel { self =>

  @Prop(gloss = "Path to candc parser binary (/path/to/bin/parser)", required = true) var path = ""
  @Prop(gloss = "Path to the models directory (containing parser, pos, etc)", required = true) var models = ""

  readProps()

  localAnnotators // instantiate lazy val here

  def mkLocalAnnotator = new LocalCandCAnnotator

  class LocalCandCAnnotator
      extends SentencesAnnotator with LocalAnnotator with IOCreator {

    def command = {
      val modelPath = new File(models, "parser").getPath
      val superPath = new File(models, "super").getPath
      s"${self.path} --model ${modelPath} --super ${superPath}"
    }

    override def launchTesters = Seq(
      LaunchTester("a|DT", _ == "</ccg>", _ endsWith "</ccg>"))
    override def defaultArgs = Seq("--printer", "xml")
    def softwareUrl = "http://www.cl.cam.ac.uk/~sc609/candc-1.00.html"

    val candc = mkIO()
    override def close() = candc.close()

    override def newSentenceAnnotation(sentence: Node): Node = {

      val output = runCandC(mkInput(sentence))

      val ccg = XML.loadString(output.mkString("\n"))

      CandCAnnotator.annotateCCGSpans(sentence, ccg, name)
    }

    private def mkInput(sentence: Node): String = {
      val tokenSeq = sentence \\ "token"
      tokenSeq.map { t => (t \@ "form") + "|" + (t \@ "pos") } mkString " "
    }

    private def runCandC(text: String): Seq[String] = {
      candc.safeWriteWithFlush(text)
      candc.readUntil(_ == "</ccg>").map(_.trim()).filter(_.startsWith("<"))
    }
  }

  override def requires = Set(Requirement.Ssplit, Requirement.Tokenize, Requirement.POS)

  override def requirementsSatisfied = Set(Requirement.CCGDerivation)
}

object CandCAnnotator {

  /** A common procedure to transform from candc-style <ccg> output to Jigg's CCG
    * annotation.
    */
  def annotateCCGSpans(sentence: Node, ccg: Node, name: String) = {
    val decoratedCCG: Node = {
      val a = assignId(ccg)
      val b = assignChildren(a, sentence \\ "token")
      assignSpan(b)
    }

    val spans = decoratedCCG.descendant.collect {
      case e: Elem if (e.label == "lf" || e.label == "rule") =>
        val a = e.attributes
        val rule = if (e.label == "lf") None else Some(a("type"))
        <span
        id={ a("id") }
        begin={ a("begin") }
        end={ a("end") }
        symbol={ a("cat") }
        rule={ rule }
        children={ a("children") }/>
    }
    val root = decoratedCCG.nonAtomChild()(0) \@ "id"

    sentence addChild (
      <ccg annotators={ name } root={ root } id={ Annotation.CCG.nextId }>{ spans }</ccg>)
  }

  def assignId(ccg: Node): Node = {
    ccg.replaceIf({ e => e.label == "lf" || e.label == "rule" }, continueSearch=true) {
      _.addAttribute("id", Annotation.CCGSpan.nextId)
    }
  }

  // Assume `assignId` is already performed
  def assignChildren(ccg: Node, tokens: Seq[Node]): Node = {
    // this is top-down to assign small ids to shallower nodes
    ccg.replaceIf(_=>true, continueSearch=true) {
      case e: Elem if e.label == "lf" || e.label == "rule" =>
        val children: String = e.label match {
          case "lf" =>
            val idx = (e \@ "start").toInt
            tokens(idx) \@ "id"
          case "rule" =>
            e.nonAtomChild.map(_ \@ "id") mkString " "
        }
        e.addAttribute("children", children)
      case e => e
    }
  }

  def assignSpan(ccg: Node): Node = {
    // fill begin/end values bottom-up
    ccg.replaceIfBottomup { e => e.label == "lf" || e.label == "rule" } { e =>
      val (begin, end) = e.label match {
        case "lf" =>
          val b = (e \@ "start")
          (b, (b.toInt + 1).toString)
        case "rule" =>
          val children = e.nonAtomChild
          children.size match {
            case 2 =>
              (children(0) \@ "begin", children(1) \@ "end")
            case 1 => // unary
              (children(0) \@ "begin", children(0) \@ "end")
          }
      }
      e.addAttributes(Map("begin" -> begin, "end" -> end))
    }
  }
}
