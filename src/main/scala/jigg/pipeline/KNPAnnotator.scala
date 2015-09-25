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

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import scala.xml._
import jigg.util.XMLUtil

trait KNPAnnotator extends Annotator{
  val knpProcess : java.lang.Process
  lazy val knpIn = new BufferedReader(new InputStreamReader(knpProcess.getInputStream, "UTF-8"))
  lazy val knpOut = new BufferedWriter(new OutputStreamWriter(knpProcess.getOutputStream, "UTF-8"))

  def runKNP(jumanTokens:String): Seq[String] = {
    knpOut.write(jumanTokens)
    knpOut.flush()

    Stream.continually(knpIn.readLine()) match {
      case strm @ (begin #:: _) if begin.startsWith("# S-ID") => strm.takeWhile(_ != "EOS").toIndexedSeq :+ "EOS"
      case other #:: _ => argumentError("command", s"Something wrong in $name\n$other\n...")
    }
  }

  def isDocInfo(knpStr:String) : Boolean = knpStr(0) == '#'
  def isChunk(knpStr:String) : Boolean = knpStr(0) == '*'
  def isBasicPhrase(knpStr:String) : Boolean = knpStr(0) == '+'
  def isEOS(knpStr:String) : Boolean = knpStr == "EOS"
  def isToken(knpStr:String) : Boolean = ! isDocInfo(knpStr) && ! isChunk(knpStr) && ! isBasicPhrase(knpStr) && ! isEOS(knpStr)

  private def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex.toString
  private def cid(sindex: String, cindex: Int) = sindex + "_chu" + cindex
  private def bpid(sindex: String, bpindex: Int) = sindex + "_bp" + bpindex.toString
  private def bpdid(sindex: String, bpdindex: Int) = sindex + "_bpdep" + bpdindex.toString
  private def depid(sindex: String, depindex: Int) = sindex + "_dep" + depindex.toString
  private def crid(sindex: String, crindex:Int) = sindex + "_cr" + crindex.toString
  private def neid(sindex: String, neindex:Int) = sindex + "_ne" + neindex.toString

  def getTokens(knpResult:Seq[String], sid:String) : Node = {
    var tokenIndex = 0

    val nodes = knpResult.filter(s => isToken(s)).map{
      s =>
      val tok = s.split(' ')

      val surf              = tok(0)
      val reading           = tok(1)
      val base              = tok(2)
      val pos               = tok(3)
      val posId            = tok(4)
      val pos1              = tok(5)
      val pos1Id           = tok(6)
      val inflectionType    = tok(7)
      val inflectionTypeId = tok(8)
      val inflectionForm    = tok(9)
      val inflectionFormId = tok(10)
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
      posId={ posId }
      pos1Id={ pos1Id }
      inflectionTypeId={ inflectionTypeId }
      inflectionFormId={ inflectionFormId }
      features={ features }/>
      tokenIndex += 1
      node
    }

    <tokens>{ nodes }</tokens>
  }

  def getBasicPhrases(knpResult:Seq[String], sid:String) = {
    var tokIdx = -1

    val basicPhraseBoundaries = knpResult.zipWithIndex.filter(x=>isBasicPhrase(x._1)).map(_._2) :+ knpResult.size
    val basicPhrases = basicPhraseBoundaries.sliding(2).toSeq.zipWithIndex map { case (Seq(b,  e), bpIdx) =>
      val knpStr = knpResult(b)
      val tokenIDs = (b + 1 until e).filter(i=>isToken(knpResult(i))) map { _ =>
        tokIdx += 1
        tid(sid, tokIdx)
      }
      <basicPhrase id={ bpid(sid, bpIdx) } tokens={ tokenIDs.mkString(" ") } features={ knpStr.split(" ")(2) } />
    }
    <basicPhrases>{ basicPhrases }</basicPhrases>
  }

  def getChunks(knpResult:Seq[String], sid:String) = {
    var tokIdx = -1

    val chunkBoundaries = knpResult.zipWithIndex.filter(x=>isChunk(x._1)).map(_._2) :+ knpResult.size
    val chunks = chunkBoundaries.sliding(2).toSeq.zipWithIndex map { case (Seq(b, e), chunkIdx) =>
      val knpStr = knpResult(b)
      val tokenIDs = (b + 1 until e).filter(i=>isToken(knpResult(i))) map { _ =>
        tokIdx += 1
        tid(sid, tokIdx)
      }
      <chunk id={ cid(sid, chunkIdx) } tokens={ tokenIDs.mkString(" ") } features={ knpStr.split(" ")(2) }/>
    }
    <chunks>{ chunks }</chunks>
  }

  def getBasicPhraseDependencies(knpResult:Seq[String], sid:String) = {
    val bpdepStrs = knpResult.filter(knpStr => isBasicPhrase(knpStr))
    val bpdepNum = bpdepStrs.length
    var bpdInd = 0

    // init: remove the last dependency (+ -1D ...)
    val dpdXml = bpdepStrs.init.map{
      bpdepStr =>
      val hd = bpid(sid, bpdepStr.split(" ")(1).init.toInt)
      val dep = bpid(sid, bpdInd)
      val lab = bpdepStr.split(" ")(1).last.toString

      val ans = <basicPhraseDependency id={bpdid(sid, bpdInd)} head={hd} dependent={dep} label={lab} />
      bpdInd += 1

      ans
    }

    <basicPhraseDependencies root={bpid(sid, bpdepNum-1)} >{ dpdXml }</basicPhraseDependencies>
  }


  def getDependencies(knpResult:Seq[String], sid:String) = {
    val depStrs = knpResult.filter(knpStr => isChunk(knpStr))
    val depNum = depStrs.length
    var depInd = 0


    // init: remove the last dependency (* -1D ...)
    val depXml = depStrs.init.map{
      depStr =>
      val hd = cid(sid, depStr.split(" ")(1).init.toInt)
      val dep = cid(sid, depInd)
      val lab = depStr.split(" ")(1).last.toString

      val ans = <dependency id={depid(sid, depInd)} head={hd} dependent={dep} label={lab} />
      depInd += 1

      ans
    }

    <dependencies root={cid(sid, depNum-1)} >{ depXml }</dependencies>
  }

  // "格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-
  def getCaseRelations(knpResult:Seq[String], tokensXml:NodeSeq, bpsXml:NodeSeq, sid:String) = {
    var crInd = 0

    val ans = knpResult.filter(str => isBasicPhrase(str)).zipWithIndex.filter(tpl => tpl._1.contains("<格解析結果:")).map{
      tpl =>
      val str = tpl._1
      val bpInd = tpl._2

      val pattern1 = "<格解析結果:[^>]+>".r
      val sp = pattern1.findFirstIn(str).getOrElse("<>").init.tail.split(":")
      val caseResults = sp(3)  //  ガ/C/太郎/0/0/1;ヲ/ ... or ガ/C/太郎/0/0/d0-s0;ヲ/ ...
      val hd = bpid(sid, bpInd)

      caseResults.split(";").map{
        str =>
        val caseResult = str.split("/")
        val lab = caseResult(0)
        val fl = caseResult(1)

        // assumes that sentence_id is as "s0"
        val dependBpid = if (caseResult(3) == "-") None else Some(bpid("s" + (sid.tail.toInt - caseResult(4).toInt), caseResult(3).toInt))
        val dependTok : Option[String]= dependBpid.map{
          bpid =>
          //find a token whose surf equals to case_result(2)

          val dependBp : Option[NodeSeq] = (bpsXml \\ "basicPhrase").find(bp => (bp \ "@id").toString == bpid)
          val tokenIds : List[String] = dependBp.map(bp => (bp \ "@tokens").toString.split(' ').toList).getOrElse(List() : List[String])
          tokenIds.find(tokId => ((tokensXml \ "token").find(tok => (tok \ "@id").toString == tokId).getOrElse(<error/>) \ "@surf").toString == caseResult(2))
        }.flatten

        val ansXml = <caseRelation id={crid(sid, crInd)} head={hd} depend={ dependTok.getOrElse("unk") } label={lab} flag={fl} />
        crInd += 1
        ansXml
      }
    }.flatten

    <caseRelations>{ ans }</caseRelations>
  }

  def getNamedEntities(knpResult:Seq[String], sid:String) = {
    var neInd = 0
    var lastTag = "N" //for convenience, use "N" as non-tag of "B/I/E/S"
    val tempTokens = new ArrayBuffer[String]
    var tempLabel = ""

    val pattern = new Regex("""\<NE:([A-Z]+):([BIES])\>""", "reLabel", "reTag")
    val namedEntities = new ArrayBuffer[Node]

    for (tpl <- knpResult.filter(knpStr => isToken(knpStr)).zipWithIndex){
      val knpStr = tpl._1
      val tokInd = tpl._2
      val (reLabel, reTag) = pattern.findFirstMatchIn(knpStr).map(m => (m.group("reLabel"), m.group("reTag"))).getOrElse(("", "N"))

      if ((lastTag == "N" && reTag == "B") || (lastTag == "N" && reTag == "S")){
        lastTag = reTag
        tempTokens += tid(sid, tokInd)
        tempLabel = reLabel
      }
      else if((lastTag == "S" && reTag == "N") || (lastTag == "B" && reTag == "N") || (lastTag == "E" && reTag == "N")){
        namedEntities += <namedEntity id={neid(sid, neInd)} tokens={tempTokens.mkString(" ")} label={tempLabel} />

        lastTag = reTag
        neInd += 1
        tempTokens.clear
        tempLabel = ""
      }
      else if((lastTag == "B" && reTag == "I") || (lastTag == "B" && reTag == "E") || (lastTag == "I" && reTag == "E")){
        lastTag = reTag
        tempTokens += tid(sid, tokInd)
      }
    }

    if(lastTag == "S" || (lastTag == "E")){
      namedEntities += <namedEntity id={neid(sid, neInd)} tokens={tempTokens.mkString(" ")} label={tempLabel} />
    }

    <namedEntities>{ namedEntities }</namedEntities>
  }

  def annotateSentenceNode(sentence:Node, knpResult:Seq[String], sid:String): Node = {
    val knpTokens = getTokens(knpResult, sid)
    val sentenceWithTokens = XMLUtil.replaceAll(sentence, "tokens")(node => knpTokens)
    val basicPhrases = getBasicPhrases(knpResult, sid)
    XMLUtil.addChild(sentenceWithTokens, Seq[Node](
      basicPhrases,
      getChunks(knpResult, sid),
      getBasicPhraseDependencies(knpResult, sid),
      getDependencies(knpResult, sid),
      getCaseRelations(knpResult, knpTokens, basicPhrases, sid),
      getNamedEntities(knpResult, sid)
    ))
  }


  private[this] def recoverTokenStr(tokenNode: Node, alt: Boolean) : String = (if (alt) "@ " else "") +
  Seq("@surf", "@reading", "@base", "@pos", "@posId", "@pos1", "@pos1Id", "@inflectionType", "@inflectionTypeId", "@inflectionForm", "@inflectionFormId").map(tokenNode \ _).mkString(" ") +
  " " + (tokenNode \ "@features").text + "\n"

  def recovJumanOutput(jumanTokens:Node) : Seq[String] = {
    val ans = ArrayBuffer.empty[String]

    (jumanTokens \\ "token").map{
      tok =>
      ans += recoverTokenStr(tok, false)

      val tokenAltSeq = (tok \ "tokenAlt")

      if (tokenAltSeq.nonEmpty){
        tokenAltSeq.map{
          tokAlt =>
          ans += recoverTokenStr(tokAlt, true)
        }
      }
    }

    ans += "EOS\n"
    ans.toSeq
  }
}
