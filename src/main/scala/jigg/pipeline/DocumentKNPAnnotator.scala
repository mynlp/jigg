package jigg.pipeline

/*
 Copyright 2013-2015 Hiroshi Noji

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

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Properties
import scala.xml._

import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer
import scala.xml._
import jigg.util.XMLUtil

class DocumentKNPAnnotator(override val name: String, override val props: Properties) extends DocumentAnnotator with KNPAnnotator{
  @Prop(gloss = "Use this command to launch KNP (-tab and -anaphora are mandatory and automatically added). Version >= 4.12 is assumed.") var command = "knp"
  readProps()

  //for KNP 4.12 (-ne option is unneed)
  lazy private[this] val knpProcess = new java.lang.ProcessBuilder(command, "-tab", "-anaphora").start
  lazy private[this] val knpIn = new BufferedReader(new InputStreamReader(knpProcess.getInputStream, "UTF-8"))
  lazy private[this] val knpOut = new BufferedWriter(new OutputStreamWriter(knpProcess.getOutputStream, "UTF-8"))

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
    def runKNP(jumanTokens:String): Seq[String] = {
      knpOut.write(jumanTokens)
      knpOut.flush()

      //FIXME なぜか元の制約だと動かない
      Stream.continually(knpIn.readLine()) match {
        case strm  => strm.takeWhile(_ != "EOS").toSeq :+ "EOS"
          // case strm @ (begin #:: _) if begin.startsWith("# S-ID") => strm.takeWhile(_ != "EOS").toSeq :+ "EOS"
          // case other #:: _ => argumentError("command", s"Something wrong in $name\n$other\n...")
      }
    }

    //recovJumanOutput : <tokens>ノードを受けとり、Seq[String]を返す

    //Documentノードを受けとる
    //Documentの中のsentenceノードの文字列をjumanの出力に直す(その際、# S-ID: did-sidをつける)
    //出力を1つの文字列として結合
    //jumanの出力をKNPに食わせる → knpResult
    //knpResultを「文ごとの解析結果」に分割する
    //knpResult
    //「文単位のアノテートをしてNodeを返すメソッド」により、各sentenceノードをアノテート

    // ------------- ここまでOK -----------------

    //predArgをアノテート
    //corefをアノテート

    val did = (document \ "@id").text
    val sentenceNodes = (document \ "sentences" \ "sentence")

    val knpResults = sentenceNodes.map{
      sentenceNode =>
      val sindex = (sentenceNode \ "@id").text
      val jumanTokens = (sentenceNode \ "tokens").head

      val jumanStr = recovJumanOutputWithDocId(jumanTokens, did, sindex).mkString

      runKNP(jumanStr)
    }

    val annotatedNodes = sentenceNodes.zip(knpResults).map{
      pair =>
      val sentenceNode = pair._1
      val knpResult = pair._2
      val sid = (sentenceNode \ "@id").text

      annotateSentenceNode(sentenceNode, knpResult, sid)
    }

    val temp = annotatedNodes.foldLeft(document){
      (temp, annotatedNode) =>
      XMLUtil.replaceAll(temp, "sentence")(sentenceNode =>
        if ((sentenceNode \ "@id").text == (annotatedNode \ "@id").text)
          annotatedNode else sentenceNode)
    }

    val ans = XMLUtil.addChild(temp, getCoreferences(temp))
    ans
  }

  def getCoreferences(docNode:NodeSeq) = {
    val eidHash = scala.collection.mutable.LinkedHashMap[Int, String]()

    (docNode \\ "basicPhrase").map{
      bp =>
      val bpid = (bp \ "@id").text
      val feature : String = (bp \ "@features").text
      val pattern = new Regex("""\<EID:(\d+)\>""", "eid")
      val eid = pattern.findFirstMatchIn(feature).map(m => m.group("eid").toInt).getOrElse(-1)

      if (eidHash.contains(eid)){
        eidHash(eid) = eidHash(eid) + " " + bpid
      }
      else{
        eidHash(eid) = bpid
      }
    }

    val did = (docNode \ "@id").text
    val ans = eidHash.map{
      case (eid, bps) =>
        <coreference id={corefid(did, eid)} basicPhrases={bps} />
    }

    <coreferences>{ ans }</coreferences>
  }



  // def getPredicateArgumentRelations(knpResult:Seq[String], sid:String) = {
  //   var parInd = 0

  //   //<述語項構造:飲む/のむ:動1:ガ/N/麻生太郎/1;ヲ/C/コーヒー/2>
  //   val pattern = new Regex("""\<述語項構造:[^:]+:[^:]+:(.+)\>""", "args")

  //   val ans = knpResult.filter(knpStr => isBasicPhrase(knpStr)).zipWithIndex.filter(tpl => tpl._1.contains("<述語項構造:")).map{
  //     tpl =>
  //     val knpStr = tpl._1
  //     val bpInd = tpl._2

  //     val argsOpt = pattern.findFirstMatchIn(knpStr).map(m => m.group("args"))
  //     argsOpt.map{
  //       args =>
  //       args.split(";").map{
  //         arg =>
  //         val sp = arg.split("/")
  //         val label = sp(0)
  //         val flag = sp(1)
  //         //val name = sp(2)
  //         val eid = sp(3).toInt

  //         val ans = <predicateArgumentRelation id={parid(sid, parInd)} predicate={bpid(sid, bpInd)} argument={corefid(sid, eid)} label={label} flag={flag} />
  //         parInd += 1
  //         ans
  //       }
  //     }.getOrElse(NodeSeq.Empty)
  //   }

  //   <predicateArgumentRelations>{ ans }</predicateArgumentRelations>
  // }

  // def makeXml(sentence:Node, knpResult:Seq[String], sid:String): Node = {
  //   val knpTokens = getTokens(knpResult, sid)
  //   val sentenceWithTokens = XMLUtil.replaceAll(sentence, "tokens")(node => knpTokens)
  //   val basicPhrases = getBasicPhrases(knpResult, sid)
  //   XMLUtil.addChild(sentenceWithTokens, Seq[Node](
  //     getCoreferences(basicPhrases, sid),
  //     getPredicateArgumentRelations(knpResult, sid)
  //   ))
  // }

  override def requires = Set(Requirement.TokenizeWithJuman)
  override def requirementsSatisfied = {
    import Requirement._
    Set(Chunk, Dependency, BasicPhrase, BasicPhraseDependency, Coreference, PredArg, NamedEntity)
  }
}
