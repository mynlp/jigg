package enju.pipeline

import java.util.Properties
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter


class CabochaAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {
  private def tid(sindex: String, tindex: String) = sindex + "_t" + tindex
  private def cid(sindex: String, cindex: String) = sindex + "_c" + cindex
  private def did(sindex: String, dindex: String) = sindex + "_d" + dindex


  def getTokens(xml:Node, sid:String) : NodeSeq = {
    val nodeSeq = (xml \\ "tok").map{
      tok =>
      val t_id = tid(sid, (tok \ "@id").toString)
      val t_feature = (tok \ "@feature").toString
      <tok id={ t_id } feature={ t_feature }>{ tok.text }</tok>
    }

    <tokens>{ nodeSeq }</tokens>
  }


  def getChunks(xml:Node, sid:String) : NodeSeq = {
    val nodeSeq = (xml \\ "chunk").map{
      chunk =>
      val c_id = cid(sid, (chunk \ "@id").toString)
      val c_tokens = (chunk \ "tok").map(tok => tid(sid, (tok \ "@id").toString)).mkString(",")
      val c_head = tid(sid, (chunk \ "@head").toString)
      val c_func = tid(sid, (chunk \ "@func").toString)
      <chunk id={ c_id } tokens={ c_tokens } head={ c_head } func = {c_func} />
    }

    <chunks>{ nodeSeq }</chunks>
  }

  def getDependencies(xml:Node, sid:String) : Option[NodeSeq] = {
    val nodeSeq = (xml \\ "chunk").filter(chunk => (chunk \ "@link").toString != "-1").map{
      chunk =>
      val d_id =did(sid, (chunk \ "@id").toString)
      val d_head = cid(sid, (chunk \ "@link").toString)
      val d_dependent = cid(sid, (chunk \ "@id").toString)
      val d_label = (chunk \ "@rel").toString

      <dependency id={ d_id } head={ d_head } dependent={ d_dependent } label={ d_label } />
    }

    if(! nodeSeq.isEmpty) Some(<dependencies>{ nodeSeq }</dependencies>) else None
  }


  // input: parsed sentence (XML) by cabocha
  // <sentence>から始まるcabochaのXML出力を受けとり、我々が欲しいXMLを返す
  def transXml(xml:Node, sid:String) : Node = {
    if (xml == <sentence/>){
      return xml
    }
    else{
      val tokens = getTokens(xml, sid)
      val chunks = getChunks(xml, sid)
      val dependencies = getDependencies(xml, sid)

      return dependencies match {
        case Some(depend) => <sentence id={ sid }>{ tokens }{ chunks }{ depend }</sentence>
        case None         => <sentence id={ sid }>{ tokens }{ chunks }</sentence>
      }
    }
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
