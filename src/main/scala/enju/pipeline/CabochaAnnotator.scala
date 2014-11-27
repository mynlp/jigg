package enju.pipeline

import java.util.Properties
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter


class CabochaAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {
  val cabocha_command: String = props.getProperty("cabocha.command", "cabocha")
  // option -f3 : output result as XML
  lazy private[this] val cabocha_process = new java.lang.ProcessBuilder(cabocha_command, "-f3").start
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

  // input: parsed sentence by cabocha as XML
  // output: XML tree what as the specification
  def convertXml(sentence:Node, cabocha_xml:Node, sid:String) : Node = {
    if (cabocha_xml == <sentence/>) sentence else{
      val tokens = getTokens(cabocha_xml, sid)
      val chunks = getChunks(cabocha_xml, sid)
      val dependencies = getDependencies(cabocha_xml, sid)

      var ans = enju.util.XMLUtil.addChild(sentence, tokens)
      ans = enju.util.XMLUtil.addChild(ans, chunks)

      ans = dependencies match {
        case Some(depend) => enju.util.XMLUtil.addChild(ans, depend)
        case None         => ans
      }

      ans
    }
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    def runCabocha(text: String, sindex:String): Seq[String] = {
      cabocha_out.write(text)
      cabocha_out.newLine()
      cabocha_out.flush()

      Iterator.continually(cabocha_in.readLine()).takeWhile(_ != "</sentence>").toSeq :+ "</sentence>"
    }

    val text = sentence.text
    val sindex = (sentence \ "@id").toString
    val cabocha_result = XML.loadString(runCabocha(text, sindex).mkString)

    convertXml(sentence, cabocha_result, sindex)
  }


  override def requires = Set(Annotator.JaTokenize)
  override def requirementsSatisfied = Set(Annotator.JaTokenize, Annotator.JaChunk, Annotator.JaDependency)
}
