package jigg.pipeline

import java.util.Properties
import scala.io.Source
import scala.xml._
import collection.JavaConversions._

import org.atilika.kuromoji.Token
import org.atilika.kuromoji.Tokenizer

class KuromojiAnnotator(override val name: String, val props: Properties) extends SentencesAnnotator {

  val tokenizer = Tokenizer.builder.build

  override def newSentenceAnnotation(sentence: Node): Node = {

    def id(sindex: String, tindex: Int) = sindex + "_" + tindex

    val sindex = (sentence \ "@id").toString
    val tokenizedSentence = tokenizer.tokenize(sentence.text)
    var tokenIndex = 0

    val tokenNodes = tokenizedSentence.map { case token =>
      val surf = token.getSurfaceForm

      // array of 品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用形,活用型,原形,読み,発音
      val features = token.getAllFeaturesArray

      // When attribute value is Option[xml.Text], None attribute is automatically skipped
      val reading = if (features.size > 8) Some(Text(features(8))) else None
      val pronounce = if (features.size > 9) Some(Text(features(9))) else None

      val nodes = <token
        id={ id(sindex, tokenIndex) }
        surf={ surf }
        pos={ features(0) }
        pos1={ features(1) }
        pos2={ features(2) }
        pos3={ features(3) }
        inflectionType={ features(4) }
        inflectionForm={ features(5) }
        base={ features(6) }
        reading={ reading }
        pronounce={ pronounce }/>

      tokenIndex += 1
      nodes
    }

    val tokensAnnotation = <tokens>{ tokenNodes }</tokens>

    jigg.util.XMLUtil.addChild(sentence, tokensAnnotation)
  }

  override def requires = Set(Requirement.Sentence)
  override def requirementsSatisfied = Set(Requirement.TokenizeWithIPA)
}
