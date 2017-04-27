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
import scala.xml.{Node, Elem, Text, Atom}
import jigg.util.XMLUtil.RichNode

class RegexSentenceAnnotator(override val name: String, override val props: Properties) extends Annotator {

  @Prop(gloss = "Regular expression to segment lines (if omitted, specified method is used)") var pattern = ""
  @Prop(gloss = "Use predefined segment pattern newLine|point|pointAndNewLine") var method = "pointAndNewLine"
  readProps()

  val splitRegex = pattern match {
    case "" =>
      method match {
        case "newLine" => RegexSentenceAnnotator.newLine
        case "point" => RegexSentenceAnnotator.point
        case "pointAndNewLine" => RegexSentenceAnnotator.pointAndNewLine
        case other => argumentError("method")
      }
    case pattern =>
      pattern.r
  }

  private[this] val sentenceIDGen = jigg.util.IDGenerator("s")

  override def annotate(annotation: Node): Node = {

    annotation.replaceAll("document") { e =>
      val line = e.text
      val sentenceBoundaries = 0 +: splitRegex.findAllMatchIn(line).map(_.end).toVector :+ line.length
      val sentences: Vector[Node] =
        sentenceBoundaries.sliding(2).toVector flatMap { case Seq(begin_, end_) =>

          def isSpace(c: Char) = c == ' ' || c == '\t' || c == '\n'

          val snippet = line.substring(begin_, end_)
          val begin = snippet.indexWhere(!isSpace(_)) match {
            case -1 => begin_ // space only
            case offset => begin_ + offset
          }
          val end = snippet.lastIndexWhere(!isSpace(_)) match {
            case -1 => begin_
            case offset => begin_ + offset + 1
          }

          // val sentence: String = line.substring(begin, end).trim()
          val sentence: String = line.substring(begin, end)
          if (sentence.isEmpty)
            None
          else {
            Option(<sentence
              id={ sentenceIDGen.next }
              characterOffsetBegin={ begin+"" }
              characterOffsetEnd={ end+"" }>{ sentence }</sentence>)
          }
        }
      // val textRemoved = XMLUtil.removeText(e)
      // XMLUtil.addChild(textRemoved, <sentences>{ sentences }</sentences>)
      e addChild <sentences>{ sentences }</sentences>
    }
  }

  override def requires = Set()
  override def requirementsSatisfied = Set(Requirement.Ssplit)

}

object RegexSentenceAnnotator extends AnnotatorCompanion[RegexSentenceAnnotator] {
  val newLine = """\n+""".r
  val point = """。+""".r
  val pointAndNewLine = """\n+|。\n*""".r
}
