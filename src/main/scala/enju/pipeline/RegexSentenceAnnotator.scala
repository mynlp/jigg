package enju.pipeline

import java.util.Properties

import scala.io.Source
import scala.xml.Node

class RegexSentenceAnnotator(val name: String, val props: Properties) extends StringAnnotator {

  override def annotate(input: Stream[String]): Node = {

    var sentenceID = 0
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

    val sentences =
      for (line <- input) yield {
        val sentenceBoundaries = splitRegex.findAllMatchIn(line).map(_.end).toList
        (0 +: sentenceBoundaries :+ line.length).sliding(2) flatMap {
          case Seq(begin, end) =>
            val sentence = line.substring(begin, end).trim()
            if (sentence.isEmpty)
              None
            else
              Option(<sentence id={ newSentenceID() }>{ sentence }</sentence>)
        }
      }
    //val sentences = Array(<sentence id="s0">今日は晴れです。</sentence>)

    <sentences>{ sentences.flatten }</sentences>
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

