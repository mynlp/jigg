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

import java.util.concurrent.LinkedBlockingQueue
import java.util.Properties
import scala.annotation.tailrec
import scala.xml._
import scala.sys.process.Process
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

abstract class MecabAnnotator(override val name: String, override val props: Properties)
    extends ExternalProcessSentencesAnnotator { self=>

  def dic: SystemDic

  @Prop(gloss = "Use this command to launch mecab. System dictionary is selected according to the current configuration accessible with '-P' option.") var command = MecabAnnotator.defaultCommand
  readProps()

  localAnnotators // instantiate lazy val here

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

  trait LocalMecabAnnotator extends LocalAnnotator with IOCreator {
    def command = self.command

    override def launchTesters = Seq(
      LaunchTester("EOS", _ == "EOS", _ == "EOS"))
    override def defaultArgs = Seq("-O", "")
    def softwareUrl = "https://taku910.github.io/mecab/"

    val mecab = mkIO()
    override def close() = mecab.close()

    override def newSentenceAnnotation(sentence: Node): Node = {

      def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex

      val sindex = (sentence \ "@id").toString
      val text = sentence.textElem

      def nextNonspaceIdx(offset: Int) = {
        def isSpace(c: Char) = c == ' ' || c == '\t'
        @tailrec
        def proceed(i: Int): Int =
          if (i < text.size && isSpace(text(i))) proceed(i + 1)
          else i
        proceed(offset)
      }

      var tokenIndex = 0
      var offset = 0

      val tokenNodes = runMecab(text).map { t =>
        val analysis = t.split('\t')
        val form = analysis(0)
        val feats = analysis(1).split(',')

        val span = (offset + "", offset + form.size + "")

        val node = tokenToNode(form, span, feats, tid(sindex, tokenIndex))
        tokenIndex += 1

        offset = nextNonspaceIdx(offset + form.size)

        node
      }

      val tokensAnnotation = <tokens annotators={ name }>{ tokenNodes }</tokens>
      sentence addChild tokensAnnotation
    }

    protected def tokenToNode(
      form: String, span: (String, String), feats: Array[String], id: String): Node

    private def runMecab(text: String): Seq[String] = {
      mecab.safeWriteWithFlush(text)
      mecab.readUntil(_ == "EOS").dropRight(1)
    }
  }

  override def requires = Set(Requirement.Ssplit)
}

class IPAMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.ipadic

  def mkLocalAnnotator = new IPALocalMecabAnnotator

  class IPALocalMecabAnnotator extends LocalMecabAnnotator {
    //output form of mecab ipadic
    //表層形\t品詞,品詞細分類1,品詞細分類2,品詞細分類3,活用型,活用形,原形,読み,発音
    //form\tpos,pos1,pos2,pos3,cType,cForm,lemma,yomi,pron
    def tokenToNode(
      form: String, span: (String, String), feats: Array[String], id: String) =
      <token
        id={ id }
        form={ form }
        offsetBegin={ span._1 }
        offsetEnd={ span._2 }
        pos={ feats(0) }
        pos1={ feats(1) }
        pos2={ feats(2) }
        pos3={ feats(3) }
        cType={ feats(4) }
        cForm={ feats(5) }
        lemma={ feats(6) }
        yomi={ if (feats.size > 7) feats(7) else "*" }
        pron={ if (feats.size > 8) Text(feats(8)) else Text("*") }/>
  }

  override def requirementsSatisfied = Set(JaRequirement.TokenizeWithIPA)
}

class JumanDicMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.jumandic

  def mkLocalAnnotator = new JumanLocalMecabAnnotator

  class JumanLocalMecabAnnotator extends LocalMecabAnnotator {
    def tokenToNode(
      form: String, span: (String, String), feats: Array[String], id: String) =
      <token
        id={ id }
        form={ form }
        offsetBegin={ span._1 }
        offsetEnd={ span._2 }
        pos={ feats(0) }
        pos1={ feats(1) }
        cType={ feats(2) }
        cForm={ feats(3) }
        lemma={ feats(4) }
        yomi={ feats(5) }
    misc={ feats(6) }/>
  }

  override def requirementsSatisfied = Set(JaRequirement.TokenizeWithJumandic)
}

class UnidicMecabAnnotator(name: String, props: Properties) extends MecabAnnotator(name, props) {
  def dic = SystemDic.unidic

  def mkLocalAnnotator = new UnidicLocalMecabAnnotator

  class UnidicLocalMecabAnnotator extends LocalMecabAnnotator {
    def tokenToNode(
      form: String, span: (String, String), feats: Array[String], id: String) = {

      val feat:Int=>String =
        if (feats.size <= 18) idx => if (idx < feats.size) feats(idx) else "*" // unk token
        else feats(_)

      <token
        id={ id }
        form={ form }
        offsetBegin={ span._1 }
        offsetEnd={ span._2 }
        pos={ feat(0) }
        pos1={ feat(1) }
        pos2={ feat(2) }
        pos3={ feat(3) }
        cType={ feat(4) }
        cForm={ feat(5) }
        lForm={ feat(6) }
        lemma={ feat(7) }
        orth={ feat(8) }
        pron={ feat(9) }
        orthBase={ feat(10) }
        pronBase={ feat(11) }
        goshu={ feat(12) }
        iType={ feat(13) }
        iForm={ feat(14) }
        fType={ feat(15) }
        fForm={ feat(16) }/>
    }
  }

  override def requirementsSatisfied = Set(JaRequirement.TokenizeWithUnidic)
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
      System.err.println(s"Failed to search dictionary file from the current mecab path: ${cmd}. Assuming ipadic is used...")
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

  def getConfig(cmd: String) = Process(cmd + " --dump-config").lineStream_!
  def getHelp(cmd: String) = Process(cmd + " --help").lineStream_!
}
