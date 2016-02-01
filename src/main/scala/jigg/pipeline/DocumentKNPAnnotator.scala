package jigg.pipeline

/*
 Copyright 2013-2015 Takafumi Sakakibara and Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

import java.util.Properties
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import scala.xml._
import jigg.util.XMLUtil

class DocumentKNPAnnotator(override val name: String, override val props: Properties) extends DocumentAnnotator with KNPAnnotator{
  @Prop(gloss = "Use this command to launch KNP (-tab and -anaphora are mandatory and automatically added). Version >= 4.12 is assumed.") var command = "knp"
  readProps()

  //for KNP 4.12 (-ne option is unneed)
  lazy val knpProcess = try {
    new java.lang.ProcessBuilder(command, "-tab", "-anaphora").start
  }
  catch {
    case e: Exception =>
      val command_name = makeFullName("command")
      val error_mes = s"""Failed to start KNP. Check environment variable PATH
  You can get KNP at http://nlp.ist.i.kyoto-u.ac.jp/index.php?KNP
  If you have KNP out of your PATH, set ${command_name} option as follows
    -${command_name} /PATH/TO/KNP/knp
"""

      argumentError("command", error_mes)
  }

  /**
    * Close the external process and the interface
    */
  override def close() {
    knpOut.close()
    knpIn.close()
    knpProcess.destroy()
  }

  private def corefid(did: String, corefindex:Int) = did + "_coref" + corefindex.toString
  private def parid(sid: String, parindex:Int) = sid + "_par" + parindex.toString

  def recovJumanOutputWithDocId(jumanTokens:Node, did:String, sid:String) : Seq[String] = {
    val docIdInfo = "# S-ID:" + did + "-" + sid + " JUMAN:7.01" + "\n" //FIXME

    docIdInfo +: recovJumanOutput(jumanTokens)
  }


  override def newDocumentAnnotation(document: Node): Node = {
    val did = (document \ "@id").text
    val sentenceNodes = (document \ "sentences" \ "sentence")

    val knpResults = sentenceNodes.map{
      sentenceNode =>
      val sindex = (sentenceNode \ "@id").text
      val jumanTokens = (sentenceNode \ "tokens").head

      val jumanStr = recovJumanOutputWithDocId(jumanTokens, did, sindex).mkString

      runKNP(jumanStr)
    }

    //sentence-level annotation
    val annotatedNodes = sentenceNodes.zip(knpResults).map{
      pair =>
      val sentenceNode = pair._1
      val knpResult = pair._2
      val sid = (sentenceNode \ "@id").text

      annotateSentenceNode(sentenceNode, knpResult, sid)
    }

    val annotatedDocNode = annotatedNodes.foldLeft(document){
      (node, annotatedNode) =>
      XMLUtil.replaceAll(node, "sentence")(sentenceNode =>
        if ((sentenceNode \ "@id").text == (annotatedNode \ "@id").text)
          annotatedNode else sentenceNode)
    }

    val docNodeWithCoref = XMLUtil.addChild(annotatedDocNode, getCoreferences(annotatedDocNode))
    XMLUtil.replaceAll(docNodeWithCoref, "sentence"){
      sentenceNode =>
      XMLUtil.addChild(sentenceNode, getPredicateArgumentRelations(sentenceNode, did))
    }
  }

  def getCoreferences(docNode:NodeSeq) = {
    val eidToBpids = (docNode \\ "basicPhrase").map{ bp =>
      val bpid = (bp \ "@id").text
      val feature : String = (bp \ "@features").text
      val pattern = new Regex("""\<EID:(\d+)\>""", "eid")
      val eid = pattern.findFirstMatchIn(feature).map(m => m.group("eid").toInt).getOrElse(-1)
      (eid, bpid)
    }.groupBy(_._1) // Map[Int, Seq[Int]]
      .toSeq               // Seq[(Int, Seq[Int])]
      .sortBy(_._1)
      .map { case (eid, lst) => (eid, lst.map(_._2).mkString(" ")) } // Seq[Int, String]

    val did = (docNode \ "@id").text
    val ans = eidToBpids.map{
      case (eid, bps) =>
        <coreference id={corefid(did, eid)} basicPhrases={bps} />
    }

    <coreferences>{ ans }</coreferences>
  }

  def getPredicateArgumentRelations(sentenceNode:NodeSeq, did:String) = {
    var parInd = 0

    //<述語項構造:飲む/のむ:動1:ガ/N/麻生太郎/1;ヲ/C/コーヒー/2>
    //<述語項構造:紅茶/こうちゃ:名1>

    val pattern = new Regex("""\<述語項構造:[^:]+:[^:]+:(.+)\>""", "args")
    val sid = (sentenceNode \ "@id").text

    val predArgNodes = (sentenceNode \\ "basicPhrase").filter(node => (node \ "@features").text.contains("<述語項構造:")).map{
      bpNode =>
      val bpid = (bpNode \ "@id").text
      val featureStr = (bpNode \ "@features").text
      val args = pattern.findFirstMatchIn(featureStr).map(m => m.group("args")) //.getOrElse("")

      val ans = args match {
        case Some(args_str) =>
          args_str.split(";").map{
            arg =>
            val sp = arg.split("/")
            val label = sp(0)
            val flag = sp(1)
            //val name = sp(2)
            val eid = sp(3).toInt

            val par_ans = <predicateArgumentRelation id={parid(sid, parInd)} predicate={bpid} argument={corefid(did, eid)} label={label} flag={flag} />
            parInd += 1
            par_ans
          }
        case None => scala.xml.Null
      }
      ans
    }
    <predicateArgumentRelations>{ predArgNodes }</predicateArgumentRelations>
  }

  override def requires = Set(Requirement.TokenizeWithJuman)
  override def requirementsSatisfied = {
    import Requirement._
    Set(Chunk, Dependency, BasicPhrase, BasicPhraseDependency, Coreference, PredArg, NamedEntity)
  }
}
