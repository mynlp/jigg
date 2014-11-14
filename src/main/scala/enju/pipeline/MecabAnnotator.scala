package enju.pipeline

import java.util.Properties
import scala.xml._
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter


class MecabAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {

  override def newSentenceAnnotation(sentence: Node): Node = {

		val mecab_command: String = props.getProperty("mecab.command", "mecab")

		//TODO option
		// val mecab_options: Seq[String] = props.getProperty("mecab.options", "").split("[\t ]+").filter(_.nonEmpty)

		lazy val mecab_process = new java.lang.ProcessBuilder((mecab_command)).start
		lazy val mecab_in = new BufferedReader(new InputStreamReader(mecab_process.getInputStream, "UTF-8"))
		lazy val mecab_out = new BufferedWriter(new OutputStreamWriter(mecab_process.getOutputStream, "UTF-8"))


		/**
		 * Close the external process and the interface
		 */
		def close() {
			mecab_out.close()
			mecab_in.close()
			mecab_process.destroy()
		}


		/**
		 * Input a text into the mecab process and obtain output
		 * @param text text to tokenize
		 * @return output of Mecab
		 */
		def runMecab(text: String): Seq[String] = {
			mecab_out.write(text)
			mecab_out.newLine()
			mecab_out.flush()
			var lines = Seq[String]()
			def loop() {
				val line = mecab_in.readLine()
				if (line != null && line != "EOS") {
					lines = lines :+ line
					loop()
				}
			}
			loop()
			lines
		}


    def id(sindex: String, tindex: Int) = sindex + "_" + tindex

    val sindex = (sentence \ "@id").toString
    val text = sentence.text
		val tokens = runMecab(text).map{str => str.replace("\t", ",")}

    var tokenIndex = 0

    //output form of Mecab
    //表層形\t品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用形,活用型,原形,読み,発音
		//surf\tpos,pos1,pos2,pos3,inflectionType,inflectionForm,base,reading,pronounce
    val tokenNodes = tokens.filter(s => s != "EOS").map{
				              tokenized =>
											val result         = tokenized.split(",")
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

											//TODO ordering attribute
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
