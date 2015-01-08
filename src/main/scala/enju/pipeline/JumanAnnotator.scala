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
    if (nodes.length >= 1 && nodes.head.label == "token"){
      val ans: ArrayBuffer[Node] = new ArrayBuffer()
      var tmp = nodes.head

      for (node <- nodes.tail){
        if (node.label == "token"){
          ans += tmp
          tmp = node
        }
        else if(node.label == "token_alt"){
          tmp = enju.util.XMLUtil.addChild(tmp, node)
        }
        else{
          //Do nothing
          //Is this OK?
        }
      }
      ans += tmp
      return NodeSeq.fromSeq(ans.toSeq)
    }
    else{
      return nodes
    }
  }


  override def newSentenceAnnotation(sentence: Node): Node = {
    def runJuman(text: String): Seq[String] = {
      juman_out.write(text)
      juman_out.newLine()
      juman_out.flush()

      Iterator.continually(juman_in.readLine()).takeWhile {line => line != null && line != "EOS"}.toSeq
    }




    def id(sindex: String, tindex: Int) = sindex + "_" + tindex
    def id_alt(sindex: String, tindex: Int, aindex: Int) = sindex + "_" + tindex + "_" + aindex

    val sindex = (sentence \ "@id").toString
    val text = sentence.text
    val tokens = runJuman(text).map{str => str.replace("\t", ",")}

    //Before tokenIndex is substituted, it will be added 1. So, the first tokenIndex is 0.
    var tokenIndex = -1
    var tokenaltIndex = 0

    //output form of Juman
    //surf reading base pos n pos1 n inflectionType n inflectionForm meaningInformation
    //表層形 読み 原形 品詞 n 品詞細分類1 n 活用型 n 活用形 n 意味情報

    val tokenNodes =
      tokens.filter(s => s != "EOS").map{
        tokenized =>
        val is_ambiguty_token = (tokenized.head == '@')
          val tokenized_features = if (is_ambiguty_token) tokenized.drop(2).split(" ") else tokenized.split(" ") //drop "@ "

        val surf              = tokenized_features(0)
        val reading           = tokenized_features(1)
        val base              = tokenized_features(2)
        val pos               = tokenized_features(3)
        val pos_id            = tokenized_features(4)
        val pos1              = tokenized_features(5)
        val pos1_id           = tokenized_features(6)
        val inflectionType    = tokenized_features(7)
        val inflectionType_id = tokenized_features(8)
        val inflectionForm    = tokenized_features(9)
        val inflectionForm_id = tokenized_features(10)
        val features          = tokenized_features.drop(11).mkString(" ") // avoid splitting features with " "


        val pos2           = None
        val pos3           = None
        val pronounce      = None

        if (is_ambiguty_token){
          tokenaltIndex += 1
        }
        else{
          tokenIndex += 1

          //Before tokenaltIndex is substituted, it will be added 1. So, the first tokenIndex is 0.
          tokenaltIndex = -1
        }


        val nodes = if (is_ambiguty_token){
          <token_alt
          id={ id_alt(sindex, tokenIndex, tokenaltIndex) }
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
          features={ if (features == "NIL") features else features.init.tail }/> // remove quotation marks
        }
        else{
          <token
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
          pronounce={ pronounce }
          pos_id={ pos_id }
          pos1_id={ pos1_id }
          inflectionType_id={ inflectionType_id }
          inflectionForm_id={ inflectionForm_id }
          features={ if (features == "NIL") features else features.init.tail }/> // remove quotation marks
        }

        nodes
      }

    val tokensAnnotation = <tokens>{ makeTokenAltChild(tokenNodes) }</tokens>

    enju.util.XMLUtil.addChild(sentence, tokensAnnotation)
  }

  override def requires = Set(Annotator.JaSentence)
  override def requirementsSatisfied = Set(Annotator.JaTokenize)
}
