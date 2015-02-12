package enju.pipeline

import java.util.Properties
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import scala.collection.mutable.ArrayBuffer

class JumanAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {
  val juman_command: String = props.getProperty("juman.command", "juman")

  lazy private[this] val juman_process = new java.lang.ProcessBuilder((juman_command)).start
  lazy private[this] val juman_in = new BufferedReader(new InputStreamReader(juman_process.getInputStream, "UTF-8"))
  lazy private[this] val juman_out = new BufferedWriter(new OutputStreamWriter(juman_process.getOutputStream, "UTF-8"))


  /**
    * Close the external process and the interface
    */
  override def close() {
    juman_out.close()
    juman_in.close()
    juman_process.destroy()
  }

  def makeTokenAltChild(nodes: NodeSeq) : NodeSeq = {
    var ans = NodeSeq.Empty

    val tokenBoundaries = nodes.zipWithIndex.filter(_._1.label=="token").map(_._2) :+ nodes.size
    for (tpl <- tokenBoundaries.sliding(2)){
      val begin = tpl(0)
      val end = tpl(1)

      ans = ans :+ (begin+1 to end-1).foldLeft(nodes(begin))((ans, i) => enju.util.XMLUtil.addChild(ans, nodes(i)))
    }
    ans
  }


  override def newSentenceAnnotation(sentence: Node): Node = {
    def runJuman(text: String): Seq[String] = {
      juman_out.write(text)
      juman_out.newLine()
      juman_out.flush()
      Iterator.continually(juman_in.readLine()).takeWhile{line => line != null && line != "EOS"}.toSeq
    }

    val sindex = (sentence \ "@id").toString
    def tid(tindex: Int) = sindex + "_tok" + tindex
    def tid_alt(tindex: Int, aindex: Int) = tid(tindex) + "_alt" + aindex

    val text = sentence.text

    //Before tokenIndex is substituted, it will be added 1. So, the first tokenIndex is 0.
    var tokenIndex = -1
    var tokenaltIndex = -1

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
        val posID            = tokenizedFeatures(4)
        val pos1             = tokenizedFeatures(5)
        val pos1ID           = tokenizedFeatures(6)
        val inflectionType   = tokenizedFeatures(7)
        val inflectionTypeID = tokenizedFeatures(8)
        val inflectionForm   = tokenizedFeatures(9)
        val inflectionFormID = tokenizedFeatures(10)
        val features         = tokenizedFeatures.drop(11).mkString(" ") // avoid splitting features with " "

        if (isAmbiguityToken){
          tokenaltIndex += 1
        }
        else{
          tokenIndex += 1

          //Before tokenaltIndex is substituted, it will be added 1. So, the first tokenIndex is 0.
          tokenaltIndex = -1
        }

        val id = if (isAmbiguityToken) tid_alt(tokenIndex, tokenaltIndex) else tid(tokenIndex)
        val token = <token
        id={ id }
        surf={ surf }
        pos={ pos }
        pos1={ pos1 }
        inflectionType={ inflectionType }
        inflectionForm={ inflectionForm }
        base={ base }
        reading={ reading }
        pos_id={ posID }
        pos1_id={ pos1ID }
        inflectionType_id={ inflectionTypeID }
        inflectionForm_id={ inflectionFormID }
        features={ features }/> // For easy recoverment of the result of Juman, don't remove quotation marks

        if (isAmbiguityToken) token.copy(label="token_alt") else token
      }

    val tokensAnnotation = <tokens>{ makeTokenAltChild(tokenNodes) }</tokens>

    enju.util.XMLUtil.addChild(sentence, tokensAnnotation)
  }

  override def requires = Set(Annotator.JaSentence)
  override def requirementsSatisfied = Set(Annotator.JaTokenize)
}
