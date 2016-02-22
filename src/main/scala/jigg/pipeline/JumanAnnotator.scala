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
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import scala.collection.mutable.ArrayBuffer
import jigg.util.XMLUtil

class JumanAnnotator(override val name: String, override val props: Properties)
    extends SentencesAnnotator with ParallelIO with IOCreator {

  @Prop(gloss = "Use this command to launch juman") var command = "juman"
  readProps()

  val ioQueue = new IOQueue(nThreads)

  def softwareUrl = "http://nlp.ist.i.kyoto-u.ac.jp/index.php?JUMAN"

  override def close() = ioQueue.close()

  def makeTokenAltChild(nodes: NodeSeq): NodeSeq = {
    val tokenBoundaries =
      (0 until nodes.size).filter(nodes(_).label == "token") :+ nodes.size

    (0 until tokenBoundaries.size - 1).map { i =>
      val b = tokenBoundaries(i)
      val e = tokenBoundaries(i + 1)
      XMLUtil.addChild(nodes(b), (b + 1 until e).map(nodes))
    }
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    val sindex = (sentence \ "@id").toString
    def tid(tindex: Int) = sindex + "_tok" + tindex
    def tidAlt(tindex: Int, aindex: Int) = tid(tindex) + "_alt" + aindex

    val text = sentence.text

    //Before tokenIndex is substituted, it will be added 1. So, the first tokenIndex is 0.
    var tokenIndex = -1
    var tokenAltIndex = -1

    // output form of Juman
    // surf reading base pos n pos1 n inflectionType n inflectionForm semantic
    // 表層形 読み 原形 品詞 n 品詞細分類1 n 活用型 n 活用形 n 意味情報
    def tokenToNode(tokenized: String): Node = {
      val isAmbig = (tokenized.head == '@')
      val id = if (isAmbig) {
        tokenAltIndex += 1
        tidAlt(tokenIndex, tokenAltIndex)
      } else {
        tokenIndex += 1
        tokenAltIndex = -1
        tid(tokenIndex)
      }

      val spaceIdx = -1 +: (0 until tokenized.size - 1).filter {
        // The reason why we check the next token is to process half space tokens correctly
        // See https://github.com/mynlp/jigg/issues/28 for detail.
        i => tokenized(i) == ' ' && tokenized(i + 1) != ' '
      }

      val feat:Int => String =
        if (isAmbig) i => tokenized.substring(spaceIdx(i + 1) + 1, spaceIdx(i + 2)) // skip @
        else i => tokenized.substring(spaceIdx(i) + 1, spaceIdx(i + 1))

      val semantic =
        if (isAmbig) tokenized.substring(spaceIdx(12)+1)
        else tokenized.substring(spaceIdx(11)+1)

      val token = <token
        id={ id }
        surf={ feat(0) }
        reading={ feat(1) }
        base={ feat(2) }
        pos={ feat(3) }
        posId={ feat(4) }
        pos1={ feat(5) }
        pos1Id={ feat(6) }
        inflectionType={ feat(7) }
        inflectionTypeId={ feat(8) }
        inflectionForm={ feat(9) }
        inflectionFormId={ feat(10) }
        semantic={ semantic }/>

      if (isAmbig) token.copy(label="tokenAlt") else token
    }

    val tokenNodes = runJuman(text).map(tokenToNode)
    val tokensAnnotation = <tokens>{ makeTokenAltChild(tokenNodes) }</tokens>
    XMLUtil.addChild(sentence, tokensAnnotation)
  }

  private def runJuman(text: String): Seq[String] = ioQueue.using { io =>
    io.safeWriteWithFlush(text)
    io.readUntil(_ == "EOS").dropRight(1)
  }

  override def requires = Set(Requirement.Sentence)
  override def requirementsSatisfied = Set(Requirement.TokenizeWithJuman)
}
