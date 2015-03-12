package jigg.pipeline

import java.util.Properties
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import scala.xml._
import scala.sys.process.Process

abstract class CabochaAnnotator(override val name: String, override val props: Properties) extends SentencesAnnotator {

  def dic: SystemDic

  @Prop(gloss = "Use this command to launch cabocha. Do not set -f and -I options. -f3 -I1 are always automatically added.") var command = CabochaAnnotator.defaultCommand
  readProps()

  override def description = {

    def keyName = makeFullName("command")
    def helpMessage = CabochaAnnotator.getHelp(command).map("    " + _).mkString("\n")

    s"""
${super.description}

  Annotate chunks (bunsetsu) and dependencies on chunks.

  Note about system dictionary:
    Dictionary settings of mecab and cabocha should be consistent (the same), but the pipeline
    does not try to fix it even if there is some inconsistency, e.g., when mecab uses ipadic
    while cabocha uses unidic.

    What the pipeline does is stopping annotation when such inconsistency is detected and
    it is user's responsibility to make the dictionary setting consistent acorss annotators in
    the pipeline.

    Please customize ${keyName} as -${keyName} "cabocha -P JUMAN" (or IPA or UNIDIC)
    when you want to change the dictionary used in cabocha. The pipeline works if this
    setting is consistent with the dictionary of mecab. Note that the pipeline does *NOT*
    check the "mecabrc" file, which also allows to modify the dictionary setting of cabocha.
    In other words, even if you have modified the mecabrc to change the used posset, the
    pipeline requires specifying that setting in -${keyName} as "-P" option. Without this,
    the pipeline recognizes the current posset is the default setting (commonly IPA).

  Original help message:
${helpMessage}
"""
  }

  // option -I1 : input tokenized file
  // option -f3 : output result as XML
  lazy private[this] val cabocha_process = new java.lang.ProcessBuilder(command, "-f3", "-I1").start
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

  private def tid(sindex: String, tindex: String) = sindex + "_tok" + tindex
  private def cid(sindex: String, cindex: String) = sindex + "_chu" + cindex
  private def did(sindex: String, dindex: String) = sindex + "_dep" + dindex

  //ununsed
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
      val c_tokens = (chunk \ "tok").map(tok => tid(sid, (tok \ "@id").toString)).mkString(" ")
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
      //tokens have been annotated by another annotator
      // val tokens = getTokens(cabocha_xml, sid)
      val chunks = getChunks(cabocha_xml, sid)
      val dependencies = getDependencies(cabocha_xml, sid)


      val sentence_with_chunks = jigg.util.XMLUtil.addChild(sentence, chunks)

      dependencies.map(jigg.util.XMLUtil.addChild(sentence_with_chunks, _)).getOrElse(sentence_with_chunks)
    }
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    def runCabocha(tokens:Node, sindex:String): Seq[String] = {
      //surf\tpos,pos1,pos2,pos3,inflectionType,inflectionForm,base,reading,pronounce
      val toks = (tokens \\ "token").map{
        tok =>
        (tok \ "@surf") + "\t" + (tok \ "@pos") + "," + (tok \ "@pos1") + "," +
        (tok \ "@pos2") + "," + (tok \ "@pos3") + "," +
        (tok \ "@inflectionType") + "," + (tok \ "@inflectionForm") + "," +
        (tok \ "@base") +
        tok.attribute("reading").map(","+_).getOrElse("") +
        tok.attribute("pronounce").map(","+_).getOrElse("") + "\n"
      } :+ "EOS\n"

      cabocha_out.write(toks.mkString)
      cabocha_out.flush()

      Iterator.continually(cabocha_in.readLine()).takeWhile(_ != "</sentence>").toSeq :+ "</sentence>"
    }

    val text = sentence.text
    val sindex = (sentence \ "@id").toString
    val tokens = (sentence \\ "tokens").head
    val cabocha_result = XML.loadString(runCabocha(tokens, sindex).mkString)

    convertXml(sentence, cabocha_result, sindex)
  }

  override def requirementsSatisfied = Set(Requirement.Chunk, Requirement.Dependency)
}

class IPACabochaAnnotator(name: String, props: Properties) extends CabochaAnnotator(name, props) {
  def dic = SystemDic.ipadic
  override def requires = Set(Requirement.TokenizeWithIPA)
}

class JumanDicCabochaAnnotator(name: String, props: Properties) extends CabochaAnnotator(name, props) {
  def dic = SystemDic.jumandic
  override def requires = Set(Requirement.TokenizeWithJuman)
}

class UnidicCabochaAnnotator(name: String, props: Properties) extends CabochaAnnotator(name, props) {
  def dic = SystemDic.jumandic
  override def requires = Set(Requirement.TokenizeWithUnidic)
}

object CabochaAnnotator extends AnnotatorCompanion[CabochaAnnotator] {

  def defaultCommand = "cabocha"

  override def fromProps(name: String, props: Properties) = {
    new IPACabochaAnnotator(name, props)
  }

  def getHelp(cmd: String) = Process(cmd + " --help").lines_!
}
