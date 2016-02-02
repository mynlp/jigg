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
  You can customize these settings by, e.g, -${keyName} "mecab -d /path/to/dic".

  Original help message:
${helpMessage}
"""
  }

  private [this] val mecabProcess = startExternalProcess(
    command,
    Seq(),
    "https://taku910.github.io/mecab/")

  lazy private[this] val mecabIn = new BufferedReader(new InputStreamReader(mecabProcess.getInputStream, "UTF-8"))
  lazy private[this] val mecabOut = new BufferedWriter(new OutputStreamWriter(mecabProcess.getOutputStream, "UTF-8"))

  /**
    * Close the external process and the interface
    */
  override def close() {
    mecabOut.close()
    mecabIn.close()
    mecabProcess.destroy()
  }

  protected def tokenToNodes(token: Seq[String], id: String): Node

  override def newSentenceAnnotation(sentence: Node): Node = {
    /**
      * Input a text into the mecab process and obtain output
      * @param text text to tokenize
      * @return output of Mecab
      */
    def runMecab(text: String): Iterator[String] = {
      mecabOut.write(text)
      mecabOut.newLine()
      mecabOut.flush()

      val iter = Iterator.continually(mecabIn.readLine())
      iter.takeWhile {
        case null =>
          argumentError("command", s"Error: Something wrong in $name?\n" +
            iter.takeWhile(_ != null).mkString("\n"))
        case l => l != "EOS"
      }
    }

    def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex

    val sindex = (sentence \ "@id").toString
    val text = sentence.text

    var tokenIndex = 0

    val tokenNodes = runMecab(text).withFilter(_!="EOS").map { t =>
      val token = t.split(Array(',', '\t'))
      val nodes = tokenToNodes(token, tid(sindex, tokenIndex))
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

  //output form of mecab ipadic
  //表層形\t品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用型,活用形,原形,読み,発音
  //surf\tpos,pos1,pos2,pos3,inflectionType,inflectionForm,base,reading,pronounce
  def tokenToNodes(token: Seq[String], id: String) = <token
    id={ id }
    surf={ token(0) }
    pos={ token(1) }
    pos1={ token(2) }
    pos2={ token(3) }
    pos3={ token(4) }
    inflectionType={ token(5) }
    inflectionForm={ token(6) }
    base={ token(7) }
    reading={ if (token.size > 8) token(8) else "" }
    pronounce={ if (token.size > 9) Text(token(9)) else Text("") }/>

  override def requirementsSatisfied = Set(Requirement.TokenizeWithIPA)
}

class JumanDicMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.jumandic

  def tokenToNodes(token: Seq[String], id: String) = <token
    id={ id }
    surf={ token(0) }
    pos={ token(1) }
    pos1={ token(2) }
    pos2={ token(3) }
    pos3={ token(4) }
    base={ token(5) }
    reading={ token(6) }
    semantic={ token(7) }/>

  override def requirementsSatisfied = Set(Requirement.TokenizeWithJuman)
}
class UnidicMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.unidic

  def tokenToNodes(token: Seq[String], id: String) = {

    val pos = token(4).split("-")
    val inflectionType = if (token.size > 5) token(5) else "*"
    val inflectionForm = if (token.size > 6) token(6) else "*"

    <token
      id={ id }
      surf={ token(0) }
      pos={ pos(0) }
      pos1={ if (pos.size > 1) pos(1) else "*" }
      pos2={ if (pos.size > 2) pos(2) else "*" }
      pos3={ if (pos.size > 3) pos(3) else "*" }
      inflectionType={ inflectionType }
      inflectionForm={ inflectionForm }
      lemmaReading={ token(2) }
      lemma={ token(3) }
      pronounce={ token(1) }/>
  }

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
      System.out.println(s"Failed to search dictionary file from the current mecab path: ${cmd}. Assume ipadic is used...")
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
        // NOTE: we use slice to pick up a variant of ipadic, e.g., mecab-ipadic-neologd.
        // We also assume naist-jdic is a variant of ipadic.
        case dicdir if dicdir.containsSlice("ipadic") || dicdir.containsSlice("naist-jdic") =>
          SystemDic.ipadic
        case dicdir if dicdir.containsSlice("jumandic") => SystemDic.jumandic
        case dicdir if dicdir.containsSlice("unidic") => SystemDic.unidic
      }
    } catch { case e: Throwable => None }
  }

  def getConfig(cmd: String) = Process(cmd + " --dump-config").lines_!
  def getHelp(cmd: String) = Process(cmd + " --help").lines_!
}
