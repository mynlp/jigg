package jigg.pipeline

import java.util.Properties

import scala.io.Source
import scala.xml.{Node, Elem, Text, Atom}
import jigg.util.XMLUtil

class RegexSentenceAnnotator(override val name: String, val props: Properties) extends Annotator {

  // TODO: Reconsider how to manage this id; this is temporarily moved here to share orders across multiple calls in shell mode.
  private[this] var sentenceID: Int = 0

  override def annotate(annotation: Node): Node = {

    def newSentenceID(): String = {
      val new_id = "s" + sentenceID
      sentenceID += 1
      new_id
    }

    val splitRegex = props.getProperty("ssplit.pattern") match {
      case null | "" =>
        props.getProperty("ssplit.method") match {
          case "newLine" => RegexSentenceAnnotator.newLine
          case "point" => RegexSentenceAnnotator.point
          case "pointAndNewLine" => RegexSentenceAnnotator.pointAndNewLine
          case _ => RegexSentenceAnnotator.defaultMethod
        }
      case pattern: String =>
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
  override def requirementsSatisfied = Set(Annotator.JaSentence)

}

object RegexSentenceAnnotator {
  val newLine = """\n+""".r
  val point = """。+""".r
  val pointAndNewLine = """\n+|。\n*""".r

  val defaultMethod = pointAndNewLine
}
