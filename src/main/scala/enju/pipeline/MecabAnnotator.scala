package enju.pipeline

import java.util.Properties
import scala.xml._
import collection.JavaConversions._
import scala.sys.process.Process
import scala.language.postfixOps // Processを使うために必要。ないとエラーメッセージが出た

class MecabAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {

  override def newSentenceAnnotation(sentence: Node): Node = {

    def id(sindex: String, tindex: Int) = sindex + "_" + tindex

    val sindex = (sentence \ "@id").toString
    val string = sentence.text

    val tokens_str = Process("echo " + string) #| Process("mecab") !!
    val tokens = tokens_str.replaceAll( "\t", "," ).split("\n")

    var tokenIndex = 0

    //Mecabの出力形式
    //表層形\t品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用形,活用型,原形,読み,発音
    val tokenNodes = tokens.filter(s => s != "EOS").map { tokenized =>
      val result : Array[String] = tokenized.split(",")


      val surf           = result(0)
      val pos            = result(1)
      val pos1           = result(2)
      val pos2           = result(3)
      val pos3           = result(4)
      val inflectionType = result(5)
      val inflectionForm = result(6)
      val base           = result(7)
      val reading        = result(8)
      val pronounce      = result(9)

      //TODO attributeの順番を制御する
      val nodes = <token
        id={ id(sindex, tokenIndex) }
        surf={ surf }
        pos={ pos }
        pos1={ pos1 }
        pos2={ pos2 }
        pos3={ pos3 }
        inflectionType={ inflectionType }
        inflectionForm={ inflectionForm }
        base={ base }
        reading={ reading }
        pronounce={ pronounce }/>

      tokenIndex += 1
      nodes
    }

    val tokensAnnotation = <tokens>{ tokenNodes }</tokens>

    enju.util.XMLUtil.addChild(sentence, tokensAnnotation)
  }


  override def requires = Set(Annotator.JaSentence)
  override def requirementsSatisfied = Set(Annotator.JaTokenize)
}
