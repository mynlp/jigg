package enju.pipeline

import java.util.Properties
import scala.io.Source
import scala.xml._
import collection.JavaConversions._

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

class KuromojiAnnotator(val name: String, val props: Properties) extends StringAnnotator {

  val tokenizer = Tokenizer.builder.build

  override def annotate(fileName: String): Node = {

    val sentencesNode = Source.fromFile(fileName).getLines.filter(_!="").zipWithIndex.map {
      case (line, index) =>
        def id(tindex: Int) = "t" + index + "_" + tindex

        val sentence = tokenizer.tokenize(line)
        var tokenIndex = 0

        val tokenNodes = sentence.map { case token =>
          val surf = token.getSurfaceForm

          // array of 品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用形,活用型,原形,読み,発音
          val features = token.getAllFeaturesArray

          val activePoSIndex = (1 until 4) find { features(_) == "*" } getOrElse(4)
          val pos = features.take(activePoSIndex).mkString("-")

          // When attribute value is Option[xml.Text], None attribute is automatically skipped
          val katsuyou = features(5) match {
            case "*" => None
            case a => Some(Text(a))
          }
          val base = features(6)
          val nodes = <token id={ id(tokenIndex) } surf={ surf } pos={ pos } katsuyou={ katsuyou } base= { base } />
          tokenIndex += 1
          nodes
        }
        <sentence id={ "s" + index }><tokens>{ tokenNodes }</tokens></sentence>
    }

    // // All parallelization should be handled here?
    // // Can we handle token index or something in other way?
    // val tokenizedSentences =  {
    //   case line =>
    // }

    // var index = 0

    // val sentencesNode = tokenizedSentences.map { case sentence =>
    //   val tokenNodes = sentence.map { case token =>
    //     index += 1
    //     <token id={ "t" + index } surf={ token.getSurfaceForm } pos={ token.getAllFeatures }/>
    //   }
    //   <sentence><tokens>{ tokenNodes }</tokens></sentence>
    // }
    <sentences>{ sentencesNode }</sentences>
  }

  override def requires = Set()
  override def requirementsSatisfied = Set(Annotator.JaTokenize)
}
