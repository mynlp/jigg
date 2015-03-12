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

import java.util.Properties
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import scala.collection.mutable.ArrayBuffer
import jigg.util.XMLUtil

class JumanAnnotator(override val name: String, override val props: Properties) extends SentencesAnnotator {

  @Prop(gloss = "Use this command to launch juman") var command = "juman"
  readProps()

  lazy private[this] val jumanProcess = new java.lang.ProcessBuilder((command)).start
  lazy private[this] val jumanIn = new BufferedReader(new InputStreamReader(jumanProcess.getInputStream, "UTF-8"))
  lazy private[this] val jumanOut = new BufferedWriter(new OutputStreamWriter(jumanProcess.getOutputStream, "UTF-8"))


  /**
    * Close the external process and the interface
    */
  override def close() {
    jumanOut.close()
    jumanIn.close()
    jumanProcess.destroy()
  }

  def makeTokenAltChild(nodes: NodeSeq) : NodeSeq = {
    val tokenBoundaries = nodes.zipWithIndex.filter(_._1.label=="token").map(_._2) :+ nodes.size

    tokenBoundaries.sliding(2).toSeq map {
      case Seq(b, e) => XMLUtil.addChild(nodes(b), (b + 1 until e).map(i=>nodes(i)))
    }
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    def runJuman(text: String): Seq[String] = {
      jumanOut.write(text)
      jumanOut.newLine()
      jumanOut.flush()
      Iterator.continually(jumanIn.readLine()).takeWhile{line => line != null && line != "EOS"}.toSeq
    }

    val sindex = (sentence \ "@id").toString
    def tid(tindex: Int) = sindex + "_tok" + tindex
    def tidAlt(tindex: Int, aindex: Int) = tid(tindex) + "_alt" + aindex

    val text = sentence.text

    //Before tokenIndex is substituted, it will be added 1. So, the first tokenIndex is 0.
    var tokenIndex = -1
    var tokenAltIndex = -1

    //output form of Juman
    //surf reading base pos n pos1 n inflectionType n inflectionForm meaningInformation
    //表層形 読み 原形 品詞 n 品詞細分類1 n 活用型 n 活用形 n 意味情報

    val tokenNodes =
      runJuman(text).filter(s => s != "EOS").map{
        tokenized =>
        val isAmbiguityToken = (tokenized.head == '@')
        val tokenizedFeatures = if (isAmbiguityToken) tokenized.drop(2).split(" ") else tokenized.split(" ") //drop "@ "

        val surf             = tokenizedFeatures(0)
        val reading          = tokenizedFeatures(1)
        val base             = tokenizedFeatures(2)
        val pos              = tokenizedFeatures(3)
        val posId            = tokenizedFeatures(4)
        val pos1             = tokenizedFeatures(5)
        val pos1Id           = tokenizedFeatures(6)
        val inflectionType   = tokenizedFeatures(7)
        val inflectionTypeId = tokenizedFeatures(8)
        val inflectionForm   = tokenizedFeatures(9)
        val inflectionFormId = tokenizedFeatures(10)
        val features         = tokenizedFeatures.drop(11).mkString(" ") // avoid splitting features with " "

        if (isAmbiguityToken){
          tokenAltIndex += 1
        }
        else{
          tokenIndex += 1

          //Before tokenAltIndex is substituted, it will be added 1. So, the first tokenIndex is 0.
          tokenAltIndex = -1
        }

        val id = if (isAmbiguityToken) tidAlt(tokenIndex, tokenAltIndex) else tid(tokenIndex)
        val token = <token
        id={ id }
        surf={ surf }
        pos={ pos }
        pos1={ pos1 }
        inflectionType={ inflectionType }
        inflectionForm={ inflectionForm }
        base={ base }
        reading={ reading }
        posId={ posId }
        pos1Id={ pos1Id }
        inflectionTypeId={ inflectionTypeId }
        inflectionFormId={ inflectionFormId }
        features={ features }/> // For easy recoverment of the result of Juman, don't remove quotation marks

        if (isAmbiguityToken) token.copy(label="tokenAlt") else token
      }

    val tokensAnnotation = <tokens>{ makeTokenAltChild(tokenNodes) }</tokens>

    XMLUtil.addChild(sentence, tokensAnnotation)
  }

  override def requires = Set(Requirement.Sentence)
  override def requirementsSatisfied = Set(Requirement.TokenizeWithJuman)
}
