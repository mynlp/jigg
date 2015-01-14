package enju.pipeline

import scala.util.control.Breaks.{break, breakable}
import scala.xml._
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Properties

class KNPAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {
  val knp_command: String = props.getProperty("knp.command", "knp")

  //for KNP 4.12 (-ne option is unneed)
  lazy private[this] val knp_process = new java.lang.ProcessBuilder(knp_command, "-tab", "-anaphora").start
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

  def isBasicPhrase(knp_str:String) : Boolean = knp_str(0) == '+'
  def isChunk(knp_str:String) : Boolean = knp_str(0) == '*'
  def isDocInfo(knp_str:String) : Boolean = knp_str(0) == '#'
  def isEOS(knp_str:String) : Boolean = knp_str == "EOS"
  def isToken(knp_str:String) : Boolean = ! isBasicPhrase(knp_str) && ! isChunk(knp_str) && ! isDocInfo(knp_str) && ! isEOS(knp_str)


  private def tid(sindex: String, tindex: Int) = sindex + "_" + tindex.toString
  private def cid(sindex: String, cindex: Int) = sindex + "_" + cindex
  private def bpid(sindex: String, bpindex: Int) = sindex + "_" + bpindex.toString
  private def bpdid(sindex: String, bpdindex: Int) = sindex + "_" + bpdindex.toString
  private def depid(sindex: String, depindex: Int) = sindex + "_" + depindex.toString
  private def crid(sindex: String, crindex:Int) = sindex + "_" + crindex.toString

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
      id={ tid(sid, tokenIndex) }
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

  def getBasicPhrases(knpResult:Seq[String], sid:String) : NodeSeq = {
    val basic_phrases_num = knpResult.filter(str => isBasicPhrase(str)).length
    val knp_result_rev = knpResult.reverse

    var bp_id = basic_phrases_num - 1
    var tok_id = knpResult.filter(str => isToken(str)).length - 1
    var tokenIDs : List[String] = List()
    var ans = scala.xml.NodeSeq.fromSeq(Seq())

    breakable {
      for (knp_str <- knp_result_rev) {
        if (isToken(knp_str)){
          tokenIDs = tid(sid, tok_id) +: tokenIDs
          tok_id -= 1
        }
        else if (isBasicPhrase(knp_str)) {
          ans = <basic_phrase id={ bpid(sid, bp_id) } tokens={ tokenIDs.mkString(" ") } features={ knp_str.split(" ")(2) } /> +: ans

          if(tok_id == 0 && bp_id == 0){
            break
          }
          bp_id -= 1
          tokenIDs = List()
        }
      }
    }
    <basic_phrases>{ ans }</basic_phrases>
  }

  def getChunks(knpResult:Seq[String], sid:String) : NodeSeq = {
    var chunk_id = knpResult.filter(str => isChunk(str)).length - 1
    var tok_id = knpResult.filter(str => isToken(str)).length - 1
    var tokenIDs : List[String] = List()
    var ans = scala.xml.NodeSeq.fromSeq(Seq())

    breakable {
      for (knp_str <- knpResult.reverse) {
        if (isToken(knp_str)){
          tokenIDs = tid(sid, tok_id) +: tokenIDs
          tok_id -= 1
        }
        else if (isChunk(knp_str)) {
          ans = <chunk id={ cid(sid, chunk_id) } tokens={ tokenIDs.mkString(" ") } features={ knp_str.split(" ")(2) } /> +: ans

          if(tok_id == 0 && chunk_id == 0){
            break
          }
          chunk_id -= 1
          tokenIDs = List()
        }
      }
    }
    <chunks>{ ans }</chunks>
  }


  def getBasicPhraseDependencies(knpResult:Seq[String], sid:String) : NodeSeq = {
    val bpdep_strs = knpResult.filter(knp_str => isBasicPhrase(knp_str))
    val bpdep_num = bpdep_strs.length
    var bpd_id = 0

    // init: remove the last dependency (+ -1D ...)
    val dpd_xml = bpdep_strs.init.map{
      bpdep_str =>
      val hd = bpdid(sid, bpdep_str.split(" ")(1).init.toInt)
      val dp = bpdid(sid, bpd_id)
      val lab = bpdep_str.split(" ")(1).last.toString

      val ans = <basic_phrase_dependency id={bpdid(sid, bpd_id)} head={hd} dependent={dp} label={lab} />
      bpd_id += 1

      ans
    }

    <basic_phrase_dependencies root={bpdid(sid, bpdep_num-1)} >{ dpd_xml }</basic_phrase_dependencies>
  }


  def getDependencies(knpResult:Seq[String], sid:String) : NodeSeq = {
    val dep_strs = knpResult.filter(knp_str => isChunk(knp_str))
    val dep_num = dep_strs.length
    var dep_id = 0


    // init: remove the last dependency (* -1D ...)
    val dep_xml = dep_strs.init.map{
      dep_str =>
      val hd = depid(sid, dep_str.split(" ")(1).init.toInt)
      val dp = depid(sid, dep_id)
      val lab = dep_str.split(" ")(1).last.toString

      val ans = <dependency id={depid(sid, dep_id)} head={hd} dependent={dp} label={lab} />
      dep_id += 1

      ans
    }

    <dependencies root={depid(sid, dep_num-1)} >{ dep_xml }</dependencies>
  }

  // "格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-
  def getCaseRelations(knpResult:Seq[String], tokens_xml:NodeSeq, bps_xml:NodeSeq, sid:String) : NodeSeq = {
    var cr_id = 0

    val ans = knpResult.filter(str => isBasicPhrase(str)).zipWithIndex.filter(tpl => tpl._1.contains("<格解析結果:")).map{
      tpl =>
      val str = tpl._1
      val bp_ind = tpl._2

      val pattern1 = "<格解析結果:[^>]+>".r
      val sp = pattern1.findFirstIn(str).getOrElse("<>").init.tail.split(":")
      val case_results = sp(3)  //  ガ/C/太郎/0/0/1;ヲ/ ...
      val hd = bpid(sid, bp_ind)

      case_results.split(";").map{
        str =>
        val case_result = str.split("/")
        val lab = case_result(0)
        val fl = case_result(1)
        val dp = if (case_result(3) == "-") "unk" else tid("s" + (sid.tail.toInt - case_result(4).toInt), case_result(3).toInt) // assumes that sentence_id is as "s0"

        val ans_xml = <case_relation id={crid(sid, cr_id)} head={hd} depend={dp} label={lab} flag={fl} />
        cr_id += 1
        ans_xml
      }
    }.flatten

    <case_relations>{ ans }</case_relations>
  }

  def makeXml(sentence:Node, knpResult:Seq[String], sid:String) : Node = {
    val knp_tokens = getTokens(knpResult, sid)
    val sentence_with_tokens = enju.util.XMLUtil.replaceAll(sentence, "tokens")(node => knp_tokens)
    val basic_phrases = getBasicPhrases(knpResult, sid)
    val sentence_with_bps = enju.util.XMLUtil.addChild(sentence_with_tokens, basic_phrases)
    // val sentence_with_chunks = enju.util.XMLUtil.addChild(sentence_with_bps, getChunks(knpResult, sid))
    // val sentence_with_bpdeps = enju.util.XMLUtil.addChild(sentence_with_chunks, getBasicPhraseDependencies(knpResult, sid))
    // val sentence_with_deps = enju.util.XMLUtil.addChild(sentence_with_bpdeps, getDependencies(knpResult, sid))
    // val sentence_with_case_relations = enju.util.XMLUtil.addChild(sentence_with_deps, getCaseRelations(knpResult, knp_tokens, basic_phrases, sid))

    // sentence_with_case_relations
    enju.util.XMLUtil.addChild(sentence_with_bps, <test knpResult={knpResult.mkString("$")} />)
  }

  def recovJumanOutput(juman_tokens:Node) : Seq[String] = {
    (juman_tokens \\ "token").map{
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
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    def runKNP(juman_tokens:Node): Seq[String] = {
      knp_out.write(recovJumanOutput(juman_tokens).mkString)
      knp_out.newLine()
      knp_out.flush()

      Iterator.continually(knp_in.readLine()).takeWhile(_ != "EOS").toSeq :+ "EOS"
    }

    val sindex = (sentence \ "@id").toString
    val juman_tokens = (sentence \ "tokens").head
    val knp_result = runKNP(juman_tokens)

    makeXml(sentence, knp_result, sindex)
  }

  override def requires = Set(Annotator.JaTokenize)
  override def requirementsSatisfied = Set(Annotator.JaChunk, Annotator.JaDependency)
}
