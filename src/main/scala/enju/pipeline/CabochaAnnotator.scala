package enju.pipeline

import java.util.Properties
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter


class CabochaAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {
  def tid(sindex: String, tindex: String) = sindex + "_t" + tindex


  def getTokens(xml:Node, sid:String) : NodeSeq = {
    val nodeSeq = (xml \\ "tok").map{
      tok =>
      val t_id = tid(sid, (tok \ "@id").toString)
      val t_feature = (tok \ "@feature").toString
      <tok id={ t_id } feature={ t_feature }>{ tok.text }</tok>
    }
    <tokens>{ nodeSeq }</tokens>
  }

  // input: parsed sentence (XML) by cabocha
  // <sentence>から始まるcabochaのXML出力を受けとり、我々が欲しいXMLを返す
  def transXml(xml:Node, sid:String) : Node = {
    val tokens = getTokens(xml, sid)

    return <sentence id="s0">{ tokens }<chunks><chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/></chunks></sentence>
  }

  val cabocha_command: String = props.getProperty("cabocha.command", "cabocha") + " -f3"

  lazy private[this] val cabocha_process = new java.lang.ProcessBuilder((cabocha_command)).start
  lazy private[this] val cabocha_in = new BufferedReader(new InputStreamReader(cabocha_process.getInputStream, "UTF-8"))
  lazy private[this] val cabocha_out = new BufferedWriter(new OutputStreamWriter(cabocha_process.getOutputStream, "UTF-8"))


  /**
   * Close the external process and the interface
   */
  override def close() {
    cabocha_out.close()
    cabocha_in.close()
    cabocha_process.destroy()
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    def runCabocha(text: String): Node = {
      cabocha_out.write(text)
      cabocha_out.newLine()
      cabocha_out.flush()

      xml.XML.loadString(Iterator.continually(cabocha_in.readLine()).toSeq.foldLeft("")(_ + _))
      // Iterator.continually(cabocha_in.readLine()).takeWhile {line => line != null && line != "EOS"}.toSeq
    }

    def cid(sindex: String, cindex: Int) = sindex + "_c" + cindex

    val sindex = (sentence \ "@id").toString
    val text = sentence.text

    val parsedXml = runCabocha(text)

    var tokenIndex = 0

    //ここまで
    //XMLをパーズして取りたいものをとって返すことが必要


    // val tokensAnnotation = <tokens>{ tokenNodes }</tokens>

    // enju.util.XMLUtil.addChild(sentence, tokensAnnotation)
    sentence
  }


  override def requires = Set(Annotator.JaSentence)
  override def requirementsSatisfied = Set(Annotator.JaTokenize)
}
