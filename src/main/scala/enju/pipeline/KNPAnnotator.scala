package enju.pipeline

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Properties
import scala.util.control.Breaks.{break, breakable}
import scala.util.matching.Regex
import scala.xml._

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


  private def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex.toString
  private def cid(sindex: String, cindex: Int) = sindex + "_chu" + cindex
  private def bpid(sindex: String, bpindex: Int) = sindex + "_bp" + bpindex.toString
  private def bpdid(sindex: String, bpdindex: Int) = sindex + "_bpdep" + bpdindex.toString
  private def depid(sindex: String, depindex: Int) = sindex + "_dep" + depindex.toString
  private def crid(sindex: String, crindex:Int) = sindex + "_cr" + crindex.toString
  private def corefid(sindex: String, corefindex:Int) = sindex + "_coref" + corefindex.toString
  private def parid(sindex: String, parindex:Int) = sindex + "_par" + parindex.toString
  private def neid(sindex: String, neindex:Int) = sindex + "_ne" + neindex.toString

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
      val features          = tok.drop(11).mkString(" ")
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


    var bp_ind = basic_phrases_num - 1
    var tok_ind = knpResult.filter(str => isToken(str)).length - 1
    var tokenIDs : List[String] = List()
    var ans = scala.xml.NodeSeq.fromSeq(Seq())

    breakable {
      for (knp_str <- knp_result_rev) {
        if (isToken(knp_str)){
          tokenIDs = tid(sid, tok_ind) +: tokenIDs
          tok_ind -= 1
        }
        else if (isBasicPhrase(knp_str)) {
          ans = <basic_phrase id={ bpid(sid, bp_ind) } tokens={ tokenIDs.mkString(" ") } features={ knp_str.split(" ")(2) } /> +: ans

          if(tok_ind == 0 && bp_ind == 0){
            break
          }
          bp_ind -= 1
          tokenIDs = List()
        }
      }
    }
    <basic_phrases>{ ans }</basic_phrases>
  }

  def getChunks(knpResult:Seq[String], sid:String) : NodeSeq = {
    var chunk_ind = knpResult.filter(str => isChunk(str)).length - 1
    var tok_ind = knpResult.filter(str => isToken(str)).length - 1
    var tokenIDs : List[String] = List()
    var ans = scala.xml.NodeSeq.fromSeq(Seq())

    breakable {
      for (knp_str <- knpResult.reverse) {
        if (isToken(knp_str)){
          tokenIDs = tid(sid, tok_ind) +: tokenIDs
          tok_ind -= 1
        }
        else if (isChunk(knp_str)) {
          ans = <chunk id={ cid(sid, chunk_ind) } tokens={ tokenIDs.mkString(" ") } features={ knp_str.split(" ")(2) } /> +: ans

          if(tok_ind == 0 && chunk_ind == 0){
            break
          }
          chunk_ind -= 1
          tokenIDs = List()
        }
      }
    }
    <chunks>{ ans }</chunks>
  }


  def getBasicPhraseDependencies(knpResult:Seq[String], sid:String) : NodeSeq = {
    val bpdep_strs = knpResult.filter(knp_str => isBasicPhrase(knp_str))
    val bpdep_num = bpdep_strs.length
    var bpd_ind = 0


    // init: remove the last dependency (+ -1D ...)
    val dpd_xml = bpdep_strs.init.map{
      bpdep_str =>
      val hd = bpid(sid, bpdep_str.split(" ")(1).init.toInt)
      val dep = bpid(sid, bpd_ind)
      val lab = bpdep_str.split(" ")(1).last.toString

      val ans = <basic_phrase_dependency id={bpdid(sid, bpd_ind)} head={hd} dependent={dep} label={lab} />
      bpd_ind += 1

      ans
    }

    <basic_phrase_dependencies root={bpid(sid, bpdep_num-1)} >{ dpd_xml }</basic_phrase_dependencies>
  }


  def getDependencies(knpResult:Seq[String], sid:String) : NodeSeq = {
    val dep_strs = knpResult.filter(knp_str => isChunk(knp_str))
    val dep_num = dep_strs.length
    var dep_ind = 0


    // init: remove the last dependency (* -1D ...)
    val dep_xml = dep_strs.init.map{
      dep_str =>
      val hd = cid(sid, dep_str.split(" ")(1).init.toInt)
      val dep = cid(sid, dep_ind)
      val lab = dep_str.split(" ")(1).last.toString

      val ans = <dependency id={depid(sid, dep_ind)} head={hd} dependent={dep} label={lab} />
      dep_ind += 1

      ans
    }

    <dependencies root={cid(sid, dep_num-1)} >{ dep_xml }</dependencies>
  }

  // "格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-
  def getCaseRelations(knpResult:Seq[String], tokens_xml:NodeSeq, bps_xml:NodeSeq, sid:String) : NodeSeq = {
    var cr_ind = 0

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

        // assumes that sentence_id is as "s0"
        val depend_bpid = if (case_result(3) == "-") None else Some(bpid("s" + (sid.tail.toInt - case_result(4).toInt), case_result(3).toInt))
        val depend_tok : Option[String]= depend_bpid.map{
          bpid =>
          //find a token whose surf equals to case_result(2)

          val depend_bp : Option[NodeSeq] = (bps_xml \\ "basic_phrase").find(bp => (bp \ "@id").toString == bpid)
          val token_ids : List[String] = depend_bp.map(bp => (bp \ "@tokens").toString.split(' ').toList).getOrElse(List() : List[String])
          token_ids.find(tok_id => ((tokens_xml \ "token").find(tok => (tok \ "@id").toString == tok_id).getOrElse(<error/>) \ "@surf").toString == case_result(2))
        }.flatten

        val ans_xml = <case_relation id={crid(sid, cr_ind)} head={hd} depend={ depend_tok.getOrElse("unk") } label={lab} flag={fl} />
        cr_ind += 1
        ans_xml
      }
    }.flatten

    <case_relations>{ ans }</case_relations>
  }

  def getCoreferences(bp_xml:NodeSeq, sid:String) : Node = {
    val eid_hash = scala.collection.mutable.LinkedHashMap[Int, String]()

    (bp_xml \ "basic_phrase").map{
      bp =>
      val bpid = (bp \ "@id").toString
      val feature : String = (bp \ "@features").text

      val pattern = new Regex("""\<EID:(\d+)\>""", "eid")
      val eid = pattern.findFirstMatchIn(feature).map(m => m.group("eid").toInt).getOrElse(-1)

      if (eid_hash.contains(eid)){
        eid_hash(eid) = eid_hash(eid) + " " + bpid
      }
      else{
        eid_hash(eid) = bpid
      }
    }

    val ans = eid_hash.map{
      case (eid, bps) =>
        <coreference id={corefid(sid, eid)} basic_phrases={bps} />
    }

    <coreferences>{ ans }</coreferences>
  }

  def getPredicateArgumentRelations(knpResult:Seq[String], sid:String) : Node = {
    var par_ind = 0

    //<述語項構造:飲む/のむ:動1:ガ/N/麻生太郎/1;ヲ/C/コーヒー/2>
    val pattern = new Regex("""\<述語項構造:[^:]+:[^:]+:(.+)\>""", "args")

    val ans = knpResult.filter(knp_str => isBasicPhrase(knp_str)).zipWithIndex.filter(tpl => tpl._1.contains("<述語項構造:")).map{
      tpl =>
      val knp_str = tpl._1
      val bp_ind = tpl._2

      val args_opt = pattern.findFirstMatchIn(knp_str).map(m => m.group("args"))
      args_opt.map{
        args =>
        args.split(";").map{
          arg =>
          val sp = arg.split("/")
          val label = sp(0)
          val flag = sp(1)
          //val name = sp(2)
          val eid = sp(3).toInt

          val ans = <predicate_argument_relation id={parid(sid, par_ind)} predicate={bpid(sid, bp_ind)} argument={corefid(sid, eid)} label={label} flag={flag} />
          par_ind += 1
          ans
        }
      }.getOrElse(NodeSeq.fromSeq(Seq()))
    }

    <predicate_argument_relations>{ ans }</predicate_argument_relations>
  }

  def getNamedEntities(knpResult:Seq[String], sid:String) : Node = {
    var ne_ind = 0
    var last_BIES = "N" //for convenience, use "N" as non-tag of "B/I/E/S"
    var temp_tokens : Seq[String] = Seq()
    var temp_label = ""

    val pattern = new Regex("""\<NE:([A-Z]+):([BIES])\>""", "re_label", "re_BIES")
    var ans = NodeSeq.fromSeq(Seq())

    for (tpl <- knpResult.filter(knp_str => isToken(knp_str)).zipWithIndex){
      val knp_str = tpl._1
      val tok_ind = tpl._2
      val (re_label, re_BIES) = pattern.findFirstMatchIn(knp_str).map(m => (m.group("re_label"), m.group("re_BIES"))).getOrElse(("", "N"))

      if ((last_BIES == "N" && re_BIES == "B") || (last_BIES == "N" && re_BIES == "S")){
        last_BIES = re_BIES
        temp_tokens = temp_tokens :+ tid(sid, tok_ind)
        temp_label = re_label
      }
      else if((last_BIES == "S" && re_BIES == "N") || (last_BIES == "B" && re_BIES == "N") || (last_BIES == "E" && re_BIES == "N")){
        ans = ans :+ <named_entity id={neid(sid, ne_ind)} tokens={temp_tokens.mkString(" ")} label={temp_label} />

        last_BIES = re_BIES
        ne_ind += 1
        temp_tokens = Seq()
        temp_label = ""
      }
      else if((last_BIES == "B" && re_BIES == "I") || (last_BIES == "B" && re_BIES == "E") || (last_BIES == "I" && re_BIES == "E")){
        last_BIES = re_BIES
        temp_tokens = temp_tokens :+ tid(sid, tok_ind)
      }
    }

    if(last_BIES == "S" || (last_BIES == "E")){
      ans = ans :+ <named_entity id={neid(sid, ne_ind)} tokens={temp_tokens.mkString(" ")} label={temp_label} />
    }

    <named_entities>{ ans }</named_entities>
  }

  def makeXml(sentence:Node, knpResult:Seq[String], sid:String) : Node = {
    val knp_tokens = getTokens(knpResult, sid)
    val sentence_with_tokens = enju.util.XMLUtil.replaceAll(sentence, "tokens")(node => knp_tokens)
    val basic_phrases = getBasicPhrases(knpResult, sid)
    val sentence_with_bps = enju.util.XMLUtil.addChild(sentence_with_tokens, basic_phrases)
    val sentence_with_chunks = enju.util.XMLUtil.addChild(sentence_with_bps, getChunks(knpResult, sid))
    val sentence_with_bpdeps = enju.util.XMLUtil.addChild(sentence_with_chunks, getBasicPhraseDependencies(knpResult, sid))
    val sentence_with_deps = enju.util.XMLUtil.addChild(sentence_with_bpdeps, getDependencies(knpResult, sid))
    val sentence_with_case_relations = enju.util.XMLUtil.addChild(sentence_with_deps, getCaseRelations(knpResult, knp_tokens, basic_phrases, sid))
    val sentence_with_coreferences = enju.util.XMLUtil.addChild(sentence_with_case_relations, getCoreferences(basic_phrases, sid))
    val sentence_with_predicate_argument = enju.util.XMLUtil.addChild(sentence_with_coreferences, getPredicateArgumentRelations(knpResult, sid))
    val sentence_with_named_entity = enju.util.XMLUtil.addChild(sentence_with_predicate_argument, getNamedEntities(knpResult, sid))

    sentence_with_named_entity
  }

  def recovJumanOutput(juman_tokens:Node) : Seq[String] = {
    (juman_tokens \\ "token").map{
      tok =>
      val tok_str = (tok \ "@surf") + " " + (tok \ "@reading") + " " + (tok \ "@base") + " " +
      (tok \ "@pos") + " " + (tok \ "@pos_id") + " " +
      (tok \ "@pos1") + " " + (tok \ "@pos1_id") + " " +
      (tok \ "@inflectionType") + " " + (tok \ "@inflectionType_id") + " " +
      (tok \ "@inflectionForm") + " " + (tok \ "@inflectionForm_id") + " " +
        (tok \ "@features").text + "\n"

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
            (tok \ "@features").text + "\n"
        }
      }
    }.foldLeft(List() : List[String])(_ ::: _.toList).toSeq :+ "EOS\n"
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    def runKNP(juman_tokens:Node): Seq[String] = {
      knp_out.write(recovJumanOutput(juman_tokens).mkString)
      knp_out.flush()

      Iterator.continually(knp_in.readLine()).takeWhile(_ != "EOS").toSeq :+ "EOS"
    }

    val sindex = (sentence \ "@id").toString
    val juman_tokens = (sentence \ "tokens").head
    val knp_result = runKNP(juman_tokens)

    makeXml(sentence, knp_result, sindex)
  }

  override def requires = Set(Annotator.JaTokenize)
  override def requirementsSatisfied = Set(Annotator.JaChunk, Annotator.JaDependency, Annotator.NamedEntity)
}
