package enju.pipeline

import java.util.Properties
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter


class MecabAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {
  val mecab_command: String = props.getProperty("mecab.command", "mecab")

  //TODO option
  // val mecab_options: Seq[String] = props.getProperty("mecab.options", "").split("[\t ]+").filter(_.nonEmpty)

  lazy private[this] val mecab_process = new java.lang.ProcessBuilder((mecab_command)).start
  lazy private[this] val mecab_in = new BufferedReader(new InputStreamReader(mecab_process.getInputStream, "UTF-8"))
  lazy private[this] val mecab_out = new BufferedWriter(new OutputStreamWriter(mecab_process.getOutputStream, "UTF-8"))


  /**
   * Close the external process and the interface
   */
  override def close() {
    mecab_out.close()
    mecab_in.close()
    mecab_process.destroy()
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    /**
     * Input a text into the mecab process and obtain output
     * @param text text to tokenize
     * @return output of Mecab
     */
    def runMecab(text: String): Seq[String] = {
      mecab_out.write(text)
      mecab_out.newLine()
      mecab_out.flush()

      Iterator.continually(mecab_in.readLine()).takeWhile {line => line != null && line != "EOS"}.toSeq
    }


    def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex

    val sindex = (sentence \ "@id").toString
    val text = sentence.text
    val tokens = runMecab(text).map{str => str.replace("\t", ",")}

    var tokenIndex = 0

    //output form of Mecab
    //表層形\t品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用型,活用形,原形,読み,発音
    //surf\tpos,pos1,pos2,pos3,inflectionType,inflectionForm,base,reading,pronounce
    val tokenNodes =
      tokens.filter(s => s != "EOS").map{
        tokenized =>
        val features         = tokenized.split(",")
        val surf           = features(0)
        val pos            = features(1)
        val pos1           = features(2)
        val pos2           = features(3)
        val pos3           = features(4)
        val inflectionType = features(5)
        val inflectionForm = features(6)
        val base           = features(7)

        val reading   = if (features.size > 8) Some(Text(features(8))) else None
        val pronounce = if (features.size > 9) Some(Text(features(9))) else None

        //TODO ordering attribute
        val nodes = <token
        id={ tid(sindex, tokenIndex) }
        surf={ surf }
        pos={ pos }
        pos1={ pos1 }
        pos2={ pos2 }
        pos3={ pos3 }
        inflectionType={ inflectionType }
        inflectionForm={ inflectionForm }
        base={ base }
        reading={ reading }
        pronounce={ pronounce }/>

        tokenIndex += 1
        nodes
      }

    val tokensAnnotation = <tokens>{ tokenNodes }</tokens>

    enju.util.XMLUtil.addChild(sentence, tokensAnnotation)
  }


  override def requires = Set(Annotator.JaSentence)
  override def requirementsSatisfied = Set(Annotator.JaTokenize)
}
