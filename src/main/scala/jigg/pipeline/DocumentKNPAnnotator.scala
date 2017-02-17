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
import jigg.util.XMLUtil.RichNode

class DocumentKNPAnnotator(override val name: String, override val props: Properties)
    extends DocumentAnnotator with KNPAnnotator {

  @Prop(gloss = "Use this command to launch KNP (-tab and -anaphora are mandatory and automatically added). Version >= 4.12 is assumed.") var command = "knp"
  readProps()

  val ioQueue = new IOQueue(nThreads)

  override def close() = ioQueue.close()

  override def defaultArgs = Seq("-tab", "-anaphora")

  private def corefId(did: String, corefIdx:Int) = did + "_knpcr" + corefIdx
  private def predArgId(sid: String, prIdx:Int) = sid + "_knppr" + prIdx

  override def newDocumentAnnotation(document: Node): Node = {
    def rmHyphen(id: String) = id map { case '-' => '_'; case a => a }

    val did = (document \ "@id").text
    val sentenceNodes = (document \ "sentences" \ "sentence")
    val sentenceIds = sentenceNodes map (_ \@ "id")

    def nPrevId(from: Int): Int=>String = { i => sentenceIds(Math.max(from - i, 0)) }

    val annotatedSentences: NodeSeq = ioQueue.using { io =>
      sentenceNodes.zipWithIndex map { case (sentenceNode, idx) =>
        val sentenceId = sentenceIds(idx)

        val docIdInfo =
          "# S-ID:" + rmHyphen(did) + "-" + rmHyphen(sentenceId) + " JUMAN:7.01" //FIXME

        val result = runKNP(sentenceNode, Some(docIdInfo), io)
        val s = annotateSentenceNode(sentenceNode, result, sentenceId, nPrevId(idx))
        s addChild extractPredArgs(s, did)
      }
    }
    val coreferencesNode = extractCoreferences(annotatedSentences, did)

    val newDoc = document.replaceAll("sentences") {
      _ copy (child = annotatedSentences)
    }

    newDoc addChild coreferencesNode
  }

  def extractCoreferences(sentences: NodeSeq, did: String): Node = {
    val mentions = (sentences \\ "basicPhrase") map { phrase =>
      val misc = phrase \@ "misc"
      misc indexOf ("<EID:") match {
        case -1 => None
        case begin =>
          val eidx = misc substring (begin + 5, misc indexOf (">", begin + 5))
          val phraseId = phrase \@ "id"
          Some((eidx.toInt, phraseId))
      }
    }
    val idxToMentions: Seq[(Int, Seq[String])] = mentions.collect {
      case Some((eidx, phraseId)) => (eidx, phraseId)
    }.groupBy(_._1).toSeq.sortBy(_._1) map { case (k, v) => (k, v map (_._2)) }

    val nodes = idxToMentions map { case (eidx, mentions) =>
      <coreference id={corefId(did, eidx)} mentions={ mentions.mkString(" ") } />
    }
    <coreferences annotators={ name }>{ nodes }</coreferences>
  }

  def extractPredArgs(sentence: Node, did: String) = {

    val sid = sentence \@ "id"
    val idGen = jigg.util.LocalIDGenerator { i => predArgId(sid, i) }

    def extractPredArgStr(basicPhrase: Node): Option[String] = {
      val line = basicPhrase \@ "misc"
      line indexOf "<述語項構造:" match {
        case -1 => None
        case begin =>
          val end = line indexOf (">", begin + 7) // 7 is the index of char next to :
          val items = line substring (begin, end) split ":"

          if (items.size > 3) Some(items(3)) else None
      }
    }

    def basicPhraseToPredArgs(basicPhrase: Node): NodeSeq =
      extractPredArgStr(basicPhrase) map { str =>
        extractPredArgs (str, basicPhrase \@ "id")
      } getOrElse Seq()

    def extractPredArgs(predArgsStr: String, predId: String): NodeSeq = {

      val nodes = predArgsStr split ";" map (_ split "/") map { items =>
        val eid = corefId(did, items(3).toInt)
        <predArg id={ idGen.next } pred={ predId } arg={ eid } deprel={ items(0) }
        flag={ items(1) }/>
      }
      nodes.toSeq
    }

    val predArgNodes = for (
      basicPhrase <- sentence \\ "basicPhrase";
      predArg <- basicPhraseToPredArgs(basicPhrase)
    ) yield predArg

    <predArgs annotators={ name }>{ predArgNodes }</predArgs>
  }

  override def requirementsSatisfied =
    super.requirementsSatisfied | Set(Requirement.Coreference, JaRequirement.KNPPredArg)
}
