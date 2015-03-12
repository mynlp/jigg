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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import scala.xml._
import scala.sys.process.Process
import jigg.util.PropertiesUtil

abstract class MecabAnnotator(override val name: String, override val props: Properties) extends SentencesAnnotator {

  def dic: SystemDic

  @Prop(gloss = "Use this command to launch mecab. System dictionary is selected according to the current configuration accessible with '-P' option.") var command = MecabAnnotator.defaultCommand
  readProps()

  override def description = {

    def keyName = makeFullName("command")
    def helpMessage = MecabAnnotator.getHelp(command).map("    " + _).mkString("\n")

    s"""${super.description}

  Tokenize sentence by MeCab.
  Current dictionary is ${dic}.
  You can customize these settings by, e.g, -${keyName} "mecab -d /path/to/dic"

  Original help message:
${helpMessage}
"""
  }

  lazy private[this] val mecab_process = new java.lang.ProcessBuilder(buildCommand(command)).start
  lazy private[this] val mecab_in = new BufferedReader(new InputStreamReader(mecab_process.getInputStream, "UTF-8"))
  lazy private[this] val mecab_out = new BufferedWriter(new OutputStreamWriter(mecab_process.getOutputStream, "UTF-8"))

  /**
   * Close the external process and the interface
   */
  override def close() {
    mecab_out.close()
    mecab_in.close()
    mecab_process.destroy()
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    /**
     * Input a text into the mecab process and obtain output
     * @param text text to tokenize
     * @return output of Mecab
     */
    def runMecab(text: String): Seq[String] = {
      mecab_out.write(text)
      mecab_out.newLine()
      mecab_out.flush()

      val strm = Stream.continually(mecab_in.readLine())

      strm.takeWhile {
        case null => // it returns null if the process terminates with some (bad) reason
          argumentError("command", s"Something wrong in $name?\n" + strm.takeWhile(_ != null).mkString("\n"))
        case "EOS" => false
        case _ => true
      }.toSeq
    }

    def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex

    val sindex = (sentence \ "@id").toString
    val text = sentence.text
    val tokens = runMecab(text).map{str => str.replace("\t", ",")}

    var tokenIndex = 0

    //output form of Mecab
    //表層形\t品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用型,活用形,原形,読み,発音
    //surf\tpos,pos1,pos2,pos3,inflectionType,inflectionForm,base,reading,pronounce
    val tokenNodes =
      tokens.filter(s => s != "EOS").map{
        tokenized =>
        val features         = tokenized.split(",")
        val surf           = features(0)
        val pos            = features(1)
        val pos1           = features(2)
        val pos2           = features(3)
        val pos3           = features(4)
        val inflectionType = features(5)
        val inflectionForm = features(6)
        val base           = features(7)

        val reading   = if (features.size > 8) Some(Text(features(8))) else None
        val pronounce = if (features.size > 9) Some(Text(features(9))) else None

        //TODO ordering attribute
        val nodes = <token
        id={ tid(sindex, tokenIndex) }
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

    jigg.util.XMLUtil.addChild(sentence, tokensAnnotation)
  }

  override def requires = Set(Requirement.Sentence)
}

class IPAMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.ipadic
  override def requirementsSatisfied = Set(Requirement.TokenizeWithIPA)
}
class JumanDicMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.jumandic
  override def requirementsSatisfied = Set(Requirement.TokenizeWithJuman)
}
class UnidicMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.unidic
  override def requirementsSatisfied = Set(Requirement.TokenizeWithUnidic)
}

object MecabAnnotator extends AnnotatorCompanion[MecabAnnotator] {

  import SystemDic._
  override def fromProps(name: String, props: Properties) = {
    val cmd = currentCommand(name, props)

    currentDictionary(cmd) map {
      case SystemDic.ipadic => new IPAMecabAnnotator(name, props)
      case SystemDic.jumandic => new JumanDicMecabAnnotator(name, props)
      case SystemDic.unidic => new UnidicMecabAnnotator(name, props)
    } getOrElse {
      System.out.println("Failed to search dictionary with \"${cmd}\". Using IPAMecabAnnotator...")
      new IPAMecabAnnotator(name, props)
    }
  }

  def defaultCommand = "mecab"

  def currentCommand(name: String, props: Properties): String = {
    val key = name + ".command"
    PropertiesUtil.findProperty(key, props) getOrElse (defaultCommand)
  }

  def currentDictionary(cmd: String): Option[SystemDic] = {
    try {
      val config = getConfig(cmd)
      val dicdirLine = config.find(_.startsWith("dicdir:"))

      dicdirLine map { l => l.drop(l.lastIndexOf('/') + 1) } map {
        case dicdir if dicdir.containsSlice("ipadic") => SystemDic.ipadic
        case dicdir if dicdir.containsSlice("jumandic") => SystemDic.jumandic
        case dicdir if dicdir.containsSlice("unidic") => SystemDic.unidic
      }
    } catch { case e: Throwable => None }
  }

  def getConfig(cmd: String) = Process(cmd + " --dump-config").lines_!
  def getHelp(cmd: String) = Process(cmd + " --help").lines_!
}
