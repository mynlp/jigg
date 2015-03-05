package jigg.pipeline

import java.util.Properties

import scala.io.Source
import scala.xml.{Node, Elem, Text, Atom}
import jigg.util.XMLUtil

class RegexSentenceAnnotator(override val name: String, override val props: Properties) extends Annotator {

  def option = Array(
    "pattern", "Regular expression to segment lines (if omitted, specified method is used)",
    "method", "Use predefined segment pattern [pointAndNewLine] newLine|point|pointAndNewLine"
  )

  // TODO: Reconsider how to manage this id; this is temporarily moved here to share orders across multiple calls in shell mode.
  private[this] var sentenceID: Int = 0

  override def annotate(annotation: Node): Node = {

    def newSentenceID(): String = {
      val new_id = "s" + sentenceID
      sentenceID += 1
      new_id
    }

    val splitRegex = prop("pattern") match {
      case None | Some("") =>
        prop("method") match {
          case Some("newLine") => RegexSentenceAnnotator.newLine
          case Some("point") => RegexSentenceAnnotator.point
          case Some("pointAndNewLine") => RegexSentenceAnnotator.pointAndNewLine
          case _ => RegexSentenceAnnotator.defaultMethod
        }
      case Some(pattern) =>
        pattern.r
    }

    XMLUtil.replaceAll(annotation, "document") { e =>
      val line = e.text
      val sentenceBoundaries = splitRegex.findAllMatchIn(line).map(_.end).toList :+ line.length
      val sentences = (0 :: sentenceBoundaries).sliding(2) flatMap { case Seq(begin, end) =>
        val sentence: String = line.substring(begin, end).trim()
        if (sentence.isEmpty)
          None
        else {
          Option(<sentence id={ newSentenceID() }>{ sentence }</sentence>)
        }
      }
      val textRemoved = XMLUtil.removeText(e)
      XMLUtil.addChild(textRemoved, <sentences>{ sentences }</sentences>)
    }
 }

  override def requires = Set()
  override def requirementsSatisfied = Set(Requirement.Sentence)

}

object RegexSentenceAnnotator extends AnnotatorObject[RegexSentenceAnnotator] {
  val newLine = """\n+""".r
  val point = """。+""".r
  val pointAndNewLine = """\n+|。\n*""".r

  val defaultMethod = pointAndNewLine

  override def options = Array()
}
