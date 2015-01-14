package enju.pipeline

import scala.xml._
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Properties

class KNPAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {
  val knp_command: String = props.getProperty("knp.command", "knp")
  lazy private[this] val knp_process = new java.lang.ProcessBuilder(knp_command, "-tab", "-ne", "-anaphora").start
  lazy private[this] val knp_in = new BufferedReader(new InputStreamReader(knp_process.getInputStream, "UTF-8"))
  lazy private[this] val knp_out = new BufferedWriter(new OutputStreamWriter(knp_process.getOutputStream, "UTF-8"))

  /**
    * Close the external process and the interface
    */
  override def close() {
    knp_out.close()
    knp_in.close()
    knp_process.destroy()
  }

  private def tid(sindex: String, tindex: String) = sindex + "_" + tindex

  def getTokens(knpResult:Seq[String], sid:String) : Node = {
    var tokenIndex = 0

    val nodes = knpResult.filter(s =>  s(0) != '#' && s(0) != '*' && s(0) != '+' && s != "EOS").map{
      s =>
      val tok = s.split(' ')

      val surf              = tok(0)
      val reading           = tok(1)
      val base              = tok(2)
      val pos               = tok(3)
      val pos_id            = tok(4)
      val pos1              = tok(5)
      val pos1_id           = tok(6)
      val inflectionType    = tok(7)
      val inflectionType_id = tok(8)
      val inflectionForm    = tok(9)
      val inflectionForm_id = tok(10)
      val features          = tok.drop(11).mkString(" ").filter(ch => ch != '"')
      val pos2           = None
      val pos3           = None
      val pronounce      = None

      val node = <token
      id={ tid(sid, tokenIndex.toString) }
      surf={ surf }
      pos={ pos }
      pos1={ pos1 }
      pos2={ pos2 }
      pos3={ pos3 }
      inflectionType={ inflectionType }
      inflectionForm={ inflectionForm }
      base={ base }
      reading={ reading }
      pronounce={ pronounce }
      pos_id={ pos_id }
      pos1_id={ pos1_id }
      inflectionType_id={ inflectionType_id }
      inflectionForm_id={ inflectionForm_id }
      features={ features }/>
      tokenIndex += 1
      node
    }

    <tokens>{ nodes }</tokens>
  }


  override def newSentenceAnnotation(sentence: Node): Node = {
    def runKNP(tokens:Node): Seq[String] = {
      // def runKNP(tokens:Node, sindex:String): Seq[String] = {
      val toks = (tokens \\ "token").map{
        tok =>
        val tok_str = (tok \ "@surf") + " " + (tok \ "@reading") + " " + (tok \ "@base") + " " +
        (tok \ "@pos") + " " + (tok \ "@pos_id") + " " +
        (tok \ "@pos1") + " " + (tok \ "@pos1_id") + " " +
        (tok \ "@inflectionType") + " " + (tok \ "@inflectionType_id") + " " +
        (tok \ "@inflectionForm") + " " + (tok \ "@inflectionForm_id") + " " +
        "\"" + (tok \ "@features") + "\"\n"

        val token_alt_seq = (tok \ "token_alt")

        if (token_alt_seq.isEmpty){
          Seq(tok_str)
        }
        else{
          tok_str +: token_alt_seq.map{
            tok_alt =>
            "@ " + (tok_alt \ "@surf") + " " + (tok_alt \ "@reading") + " " + (tok_alt \ "@base") + " " +
            (tok_alt \ "@pos") + " " + (tok_alt \ "@pos_id") + " " +
            (tok_alt \ "@pos1") + " " + (tok_alt \ "@pos1_id") + " " +
            (tok_alt \ "@inflectionType") + " " + (tok_alt \ "@inflectionType_id") + " " +
            (tok_alt \ "@inflectionForm") + " " + (tok_alt \ "@inflectionForm_id") + " " +
            "\"" + (tok_alt \ "@features") + "\"\n"
          }
        }
      }.foldLeft(List() : List[String])(_ ::: _.toList).toSeq :+ "EOS\n"

      knp_out.write(toks.mkString)
      knp_out.newLine()
      knp_out.flush()

      Iterator.continually(knp_in.readLine()).takeWhile(_ != "EOS").toSeq :+ "EOS"
    }

    // val text = sentence.text
    val sindex = (sentence \ "@id").toString
    val juman_tokens = (sentence \\ "tokens").head
    // val knp_result = XML.loadString(runKNP(tokens).mkString)
    val knp_result_seq = runKNP(juman_tokens)
    // val knp_result_rev = knp_result_seq.reverse

    val knp_tokens = getTokens(knp_result_seq, sindex)
    // convertXml(sentence, knp_result, sindex)

    enju.util.XMLUtil.replaceAll(sentence, "tokens")(node => knp_tokens)
  }

  override def requires = Set(Annotator.JaTokenize)
  override def requirementsSatisfied = Set(Annotator.JaChunk, Annotator.JaDependency)
}
