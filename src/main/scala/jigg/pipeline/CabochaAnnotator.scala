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
import scala.sys.process.Process
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

abstract class CabochaAnnotator(override val name: String, override val props: Properties)
    extends SentencesAnnotator with ParallelIO with IOCreator {

  def dic: SystemDic

  @Prop(gloss = "Use this command to launch cabocha. Do not touch -f and -I options. -f1 -I1 are always automatically added.") var command = CabochaAnnotator.defaultCommand
  readProps()

  val ioQueue = new IOQueue(nThreads)

  override def launchTesters = Seq(
    LaunchTester("EOS", _ == "EOS", _ == "EOS"))

  override def description = {

    def keyName = makeFullName("command")
    def helpMessage = CabochaAnnotator.getHelp(command).map("    " + _).mkString("\n")

    s"""${super.description}

  Annotate chunks (bunsetsu) and dependencies on chunks.

  Note about system dictionary:
    Dictionary settings of tokenizer (e.g., mecab) and cabocha should be consistent (the same),
    but the pipeline does not try to fix it even if there is some inconsistency, e.g., when
    mecab uses ipadic while cabocha uses unidic.

    What the pipeline does is stopping annotation when such inconsistency is detected and
    it is user's responsibility to make the dictionary setting consistent acorss annotators in
    the pipeline.

    The pipeline try to find the current dic (posset) in the following way:
      1) If ${keyName} is customized as, e.g., -${keyName} "cabocha -P JUMAN" (or IPA or UNIDIC),
         it assumes that specified dic is used.
      2) Otherwise, it tries to find the cabocharc file and read the setting.
      3) If 2 is faild, it try to read the default setting (that depends on the system) from the
         help message of the current command.

  Original help message:
${helpMessage}
"""
  }

  override def defaultArgs = Seq("-f1", "-I1")
  def softwareUrl = "http://taku910.github.io/cabocha/"

  override def close() = ioQueue.close()

  override def newSentenceAnnotation(sentence: Node): Node = {
    val tokens: NodeSeq = (sentence \ "tokens").head \ ("token")
    val result = runCabocha(tokens).toArray

    val sid = (sentence \ "@id").toString
    val chunks = resultToChunks(result)

    val tokenIds = tokens map(_ \ "@id" + "")

    sentence addChild Seq(chunksNode(chunks, sid, tokenIds), depsNode(chunks, sid))
  }

  private def runCabocha(tokens:NodeSeq): Seq[String] = ioQueue.using { io =>
    io.safeWriteWithFlush({
      for (token <- tokens) yield tokenToMecabFormat(token)
    } ++ Iterator("EOS"))
    io.readUntil(_ == "EOS").dropRight(1)
  }

  protected def tokenToMecabFormat(token: Node): String =
    (token \ "@form") + "\t" + featAttributes.map(token \ _).mkString(",")

  protected def featAttributes: Array[String] // depends on dictionary

  private def resultToChunks(result: Array[String]): Seq[Chunk] = {
    val chunkIdxs = (0 until result.size).filter { i => result(i).startsWith("* ") }

    (0 until chunkIdxs.size).map { i =>
      val idx = chunkIdxs(i)
      val offset = idx - i

      val endIdx = if (i == chunkIdxs.size - 1) result.size else chunkIdxs(i+1)
      val numTokens = endIdx - idx - 1
      Chunk.fromResult(result(idx), offset, numTokens)
    }
  }

  case class Chunk(id: Int, headChunk: Int, rel: String,
    head: Int, func: Int, offset: Int, numTokens: Int) {

    def range = (offset until offset + numTokens)
    def headIdx = offset + head
    def funcIdx = offset + func
  }

  object Chunk {
    def fromResult(line: String, offset: Int, numTokens: Int): Chunk = {
      val items = line.split(' ')
      val id = items(1).toInt
      val rel = items(2).last.toString
      val headChunk = items(2).dropRight(1).toInt

      val hd = items(3).split('/')
      val head = hd(0).toInt
      val func = hd(1).toInt

      Chunk(id, headChunk, rel, head, func, offset, numTokens)
    }
  }

  def chunksNode(chunks: Seq[Chunk], sid:String, tokenIds: Seq[String]): Node = {
    val nodeSeq = (0 until chunks.size) map { i =>
      val chunk = chunks(i)
      val id = chunkId(sid, chunk.id)
      val tokens = chunk.range.map(tokenIds).mkString(" ")

      val head = tokenIds(chunk.headIdx)
      val func = tokenIds(chunk.funcIdx)
      <chunk id={ id } tokens={ tokens } head={ head } func={ func } />
    }
    <chunks annotators={ name }>{ nodeSeq }</chunks>
  }

  def depsNode(chunks: Seq[Chunk], sid:String): Node = {
    val nodeSeq = chunks.map { chunk =>
      val id = depId(sid, chunk.id)
      val head = chunk.headChunk match {
        case -1 => "root"
        case id => chunkId(sid, id)
      }
      val dep = chunkId(sid, chunk.id)
      <dependency unit="chunk" id={ id } head={ head } dependent={ dep } deprel={ chunk.rel } />
    }
    <dependencies annotators={ name }>{ nodeSeq }</dependencies>
  }

  def chunkId(sid: String, idx: Int) = sid + "_chu" + idx
  def depId(sid: String, idx: Int) = sid + "_dep" + idx

  override def requirementsSatisfied =
    Set(JaRequirement.CabochaChunk, JaRequirement.ChunkDependencies)
}

class IPACabochaAnnotator(name: String, props: Properties) extends CabochaAnnotator(name, props) {
  def dic = SystemDic.ipadic

  val featAttributes = Array(
    "pos", "pos1", "pos2", "pos3", "cType", "cForm",
    "lemma", "yomi", "pron").map("@"+_)

  override def requires = Set(JaRequirement.TokenizeWithIPA)
}

class JumanDicCabochaAnnotator(name: String, props: Properties) extends CabochaAnnotator(name, props) {
  def dic = SystemDic.jumandic

  val featAttributes = Array(
    "pos", "pos1", "cType", "cForm", "lemma", "yomi", "misc").map("@"+_)

  override def requires = Set(JaRequirement.TokenizeWithJumandic)
}

class UnidicCabochaAnnotator(name: String, props: Properties) extends CabochaAnnotator(name, props) {
  def dic = SystemDic.jumandic

  val featAttributes = Array(
    "pos", "pos1", "pos2", "pos3", "cType", "cForm", "lForm", "lemma", "orth", "pron",
    "orthBase", "pronBase", "goshu", "iType", "iForm", "fType", "fForm").map("@"+_)

  override def requires = Set(JaRequirement.TokenizeWithUnidic)
}

object CabochaAnnotator extends AnnotatorCompanion[CabochaAnnotator] {

  def defaultCommand = "cabocha"
  def defaultDic = SystemDic.ipadic

  override def fromProps(name: String, props: Properties) = {
    val selector = new CabochaSelector(name, props)
    selector.select
  }

  def getHelp(cmd: String) = Process(cmd + " --help").lineStream_!

  private class CabochaSelector(name: String, props: Properties) {
    val cmdKey = name + ".command"
    val cmd = PropertiesUtil.findProperty(cmdKey, props) getOrElse (defaultCommand)
    val cmdList = cmd.split("\\s+")

    def select(): CabochaAnnotator = {
      def dicFromCommand: Option[SystemDic] = readFromCommand() flatMap { toSystemDic(_) }
      def dicFromCabocharc: Option[SystemDic] = readFromCabocharc() flatMap { toSystemDic(_) }
      def dicFromHelp: SystemDic = {
        import System.err.{ println => p }
        p(s"WARNING: Failed to find cabocharc from the current command. Please check cabocha-config exists on the same path as cabocha.")
        readDefaultFromHelp() map { foundDefault =>
          p(s"Assume the default posset is used (${foundDefault}).")
          foundDefault
        } getOrElse {
          p(s"Failed to get default posset with --help command. Probably the given cabocha command is broken? Assume ${defaultDic} is used.")
          defaultDic
        }
      }
      val dic = dicFromCommand orElse(dicFromCabocharc) getOrElse(dicFromHelp)
      create(dic)
    }

    def readFromCommand(): Option[String] = cmdList.zipWithIndex.find(_._1 == "-P") map {
      case (p, idx) => cmdList(idx + 1)
    } orElse {
      cmdList.zipWithIndex.find(_._1.startsWith("--posset=")) map {
        case (p, _) => p.drop(p.indexOf("=") + 1)
      }
    }

    def readFromCabocharc(): Option[String] = tryToFindCabocharc() flatMap { path =>
      try jigg.util.IOUtil.openIterator(path).toSeq.filter(_.startsWith("posset")) match {
        case head :+ last => Some(last.drop(last.lastIndexOf(' ') + 1).trim)
        case _ => None
      } catch { case e: Throwable => None }
    }

    def tryToFindCabocharc(): Option[String] = {
      val configCommand = cmdList(0) + "-config --sysconfdir"
      safeProcess(configCommand) match {
        case Some(Seq(directoryPath)) => Some(directoryPath + "/cabocharc")
        case _ => None
      }
    }

    def readDefaultFromHelp(): Option[SystemDic] = try {
      val help = getHelp(cmd)
      val possetLine = help.find(_.startsWith("-P,"))

      possetLine map { l => l.slice(l.lastIndexOf(' ') + 1, l.size - 1) } flatMap { // ... (default IPA) -> IPA
        toSystemDic(_)
      }
    } catch { case e: Throwable => None }

    def toSystemDic(posset: String): Option[SystemDic] = posset match {
      case "IPA" => Some(SystemDic.ipadic)
      case "JUMAN" => Some(SystemDic.jumandic)
      case "UNIDIC" => Some(SystemDic.unidic)
      case _ => None
    }

    def create(dic: SystemDic): CabochaAnnotator = dic match {
      case SystemDic.ipadic => new IPACabochaAnnotator(name, props)
      case SystemDic.jumandic => new JumanDicCabochaAnnotator(name, props)
      case SystemDic.unidic => new UnidicCabochaAnnotator(name, props)
    }

    def safeProcess(cmd:String): Option[Seq[String]] = {
      try { Some(Process(cmd).lineStream_!.toSeq) }
      catch { case e : Throwable => None }
    }

  }
}
