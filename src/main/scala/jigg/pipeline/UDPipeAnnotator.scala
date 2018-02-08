package jigg.pipeline

/*
 Copyright 2013-2018 Hiroshi Noji

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

import java.io.{ByteArrayInputStream, File}
import java.util.Properties

import scala.xml._
import scala.collection.mutable.ArrayBuffer

import jigg.util.PropertiesUtil
import jigg.util.ResourceUtil
import jigg.util.XMLUtil.RichNode

/** UDPipe annotator
  *
  * We do not support parallel processing, for simplicity, and assuming UDPipe
  * is already sufficiently fast.
  */
trait UDPipeAnnotator extends Annotator with IOCreator {

  type CoNLLSentence = Seq[Seq[String]]

  @Prop(gloss = "Path 'activate' script of the virtual environment that you wish to run on depccg") var venv = ""
  @Prop(gloss = "Path to the model file (e.g., english-ud-2.0-170801.udpipe)", required = true) var model = ""
  readProps()

  override def nThreads = 1

  override def description = s"""${super.description}

  A wrapper for UDPipe (https://github.com/ufal/udpipe), a state-of-the-art pipeline
  for parsing to universal dependencies from raw inputs. It can perform sentence-
  splitting, tokenization, POS-tagging, and dependency parsing from raw text.

  As in CoreNLP, Jigg assumes special form of an annotation name, like udpipe[tokenize]
  and udpipe[tokenize,pos,parse]. Three candidates are valid for this option:

    tokenize: performs tokenization and sentence splitting.
    pos:      performs POS tagging (including assigning UD-specific "feats" attributes)
    parse:    performs dependency parsing

  Note that these options are mandatory. That is, "-annotators ${name}" is an invalid
  option. If you want UDPipe to only perform dependency parsing, please specify it
  explicitly by "-annotators ${name}[parse]".

  Note also that UDPipe tokenizer ("tokenize") performs both sentence splitting and
  (usual sense of) tokenization. This means you cannot, for example, combine the UDPipe
  tokenizer and CoreNLP's sentence splitter. The following pipeline is fine, which
  performs tokenization by CoreNLP and gives the result to UDPipe:

    "-annotators corenlp[tokenize,ssplit],udpipe[pos,parse]"

  "${name}[parse]" requires that "@feats" and "@upos" attributes for each token are
  annotated beforehand, and currently they are only given by "udpipe[pos]". So please
  specify "${name}[pos]" as a POS tagger, if you want to do dependency parsing on UDPipe.
"""

  override def softwareUrl = ""
  override def launchErrorMessage = s"""
  To use UDPipe, Jigg assumes that its Python binding is installed on the system.
  Make sure that udpipe is correctly installed in your Python environment. The easist
  way is to use pip: "pip install ufal.udpipe".

  You can also specify the path to a virtualenv, on which udpipe is installed. See
  "-help ${name}" for details.
"""

  def mode: String // one of ['all', 'tok|pos', 'pos|par', 'tok', 'pos', 'par']

  def doTokenize: Boolean
  def doPOS: Boolean
  def doDeps: Boolean

  lazy val script: File = ResourceUtil.readPython("udpipe.py")
  def command = {
    val venvcommand = if (venv == "") "" else s"source ${venv} && "
    venvcommand + s"python ${script.getPath} $model '$mode'"
  }

  override def launchTesters = {
    // One CoNLL line can also be regarded as a raw input.
    val l = "1\tThis\tthis\tPRON\tDT\tNumber=Sing|PronType=Dem\t_\t_\t_\t_"
    Seq(LaunchTester(l+"\n####EOD####", _ == "END", _ == "END"))
  }

  lazy val udpipe = mkIO()
  override def close() = udpipe.close()

  override def init() = {
    udpipe
  }

  /** Common procedure:
    *
    * 1. extract input from root node
    * 2. give it to pipeline
    * 3. add information to root
    */
  def annotate(annotation: Node) = {
    annotation.replaceAll("document") { e =>
      val input = mkInput(e)
      val result = runUDPipe(input)

      val conllSentences = readCoNLLUSentences(result)

      addAnnotation(e, conllSentences)
    }
  }

  private def runUDPipe(input: String): Seq[String] = {
    udpipe.safeWriteWithFlush(input)
    udpipe.readUntil(_ == "END").dropRight(1)
  }

  def mkInput(document: Node): String =
    (if (doTokenize) node2raw(document) else node2conllu(document)) + "\n####EOD####"

  def addAnnotation(document: Node, conllSentences: Seq[CoNLLSentence]): Node = {
    val sentencesNode = doTokenize match {
      case true =>
        val text = document.textElem
        mkSentencesNode(text, conllSentences)
      case _ =>
        (document \ "sentence").head
    }
    val mayAddTags = if (doPOS) Some(addTags _) else None
    val mayAddDeps = if (doDeps) Some(addDependencies _) else None
    val adder: Seq[(Node, CoNLLSentence)=>Node] = (mayAddTags ++ mayAddDeps).toSeq

    val newSentencesNode = adder match {
      case Seq() => sentencesNode
      case adder => addToEverySentence(sentencesNode, conllSentences, adder)
    }
    document addOrOverwriteChild newSentencesNode
  }

  private def node2raw(document: Node): String = {
    if (!(document \\ "tokens").isEmpty)
      throw new ArgumentError(s"${name}[tokenize] cannot be run after any ssplit or other tokenize annotators.")
    document.textElem
  }

  /** Given a node that may be annotated with tokens, possibly with pos, upos, and
    * feats, convert it to the corresponding CoNLLU format.
    */
  private def node2conllu(document: Node): String = {
    val sentences = document \ "sentences" \ "sentence"
    sentences.flatMap { sentence =>
      val tokens = sentence \ "tokens" \ "token"
      val lines = tokens.zipWithIndex.map { case (token, i) =>
        def underlineOr(r: String) = if (r == "") "_" else r

        val form = token \@ "form"
        val lemma = underlineOr(token \@ "lemma")
        val pos = underlineOr(token \@ "pos")
        val upos = underlineOr(token \@ "upos")
        val feats = underlineOr(token \@ "feats")

        s"${i+1}\t${form}\t${lemma}\t${upos}\t${pos}\t${feats}\t_\t_\t_\t_"
      }
      lines :+ "\n"
    }.mkString("\n")
  }

  def readCoNLLUSentences(lines: Seq[String]) = {
    val sentences = new ArrayBuffer[CoNLLSentence]
    val sentence = new ArrayBuffer[Seq[String]]

    for (line <- lines; if !line.startsWith("#")) {
      val l = line.trim
      if (l.isEmpty) {
        if (!sentence.isEmpty) {
          sentences += sentence.clone
          sentence.clear
        }
      } else {
        val token = l.split("\t")
        // Here we strip-off multi-word tokens, though I don't know whether UDPipe
        // utilizes it internally.
        if (token(0).matches("\\d+")) sentence += token
      }
    }
    if (!sentence.isEmpty) sentences += sentence
    sentences
  }

  def mkSentencesNode(text: String, conllSentences: Seq[CoNLLSentence]): Node = {
    var sentenceOffset = 0
    var tokenOffset = 0
    val sentenceseq = conllSentences.map { conllSentence =>
      def form(token: Seq[String]) = token(1)

      val firstToken = conllSentence(0)
      val sentenceBegin = text.indexOfSlice(form(firstToken), sentenceOffset)

      // update sentenceOffset
      val tokenseq: NodeSeq = conllSentence.map { token =>
        val f = form(token)
        val begin = text.indexOfSlice(f, tokenOffset)
        val end = begin + f.size
        tokenOffset = end // now temporary this value is useless

        <token
        id={ Annotation.Token.nextId }
        form={ f }
        offsetBegin={ begin+"" }
        offsetEnd={ end+"" }/>
      }
      val tokens = <tokens annotators={ name }>{ tokenseq }</tokens>
      val sentenceEnd = tokenOffset
      sentenceOffset = tokenOffset

      val sentenceText = text.substring(sentenceBegin, sentenceEnd)

      <sentence
      id={ Annotation.Sentence.nextId }
      characterOffsetBegin={ sentenceBegin+"" }
      characterOffsetEnd={ sentenceEnd+"" }>{ sentenceText }{ tokens }</sentence>
    }
    <sentences>{ sentenceseq }</sentences>
  }

  def addToEverySentence(
    sentencesNode: Node,
    conllSentences: Seq[CoNLLSentence],
    adders: Seq[(Node, CoNLLSentence)=>Node]
  ): Node = {
    val sentenceseq = sentencesNode \ "sentence"
    assert(sentenceseq.size == conllSentences.size)

    val newSentenceseq = sentenceseq.zip(conllSentences) map {
      case (sentenceNode, conllSentence) =>
        adders.foldLeft(sentenceNode) { (currentNode, adder) =>
          adder(currentNode, conllSentence)
        }
    }
    sentencesNode replaceChild newSentenceseq
  }

  def addTags(sentence: Node, conllSentence: CoNLLSentence): Node = {
    val tokens = (sentence \ "tokens").head
    val tokenseq = tokens \ "token"

    // CoNLLSentence is just Seq[Seq[String]]
    val newTokenSeq = tokenseq.zip(conllSentence).map { case (tokenNode, conllToken) =>
      val pos = conllToken(3)
      val upos = conllToken(2)
      val lemma = conllToken(4)
      val feats = conllToken(5)

      tokenNode.addAttributes(
        Map("pos"->pos, "upos"->upos, "lemma"->lemma, "feats"->feats))
    }
    val newTokens = tokens.replaceChild(newTokenSeq).addAnnotatorName(name)
    sentence addOrOverwriteChild newTokens
  }

  def addDependencies(sentence: Node, conllSentence: CoNLLSentence): Node = {
    val tokens = sentence \ "tokens"
    val tokenseq = tokens \ "token"

    val deps = conllSentence.zipWithIndex.map { case (conllToken, i) =>
      val deprel = conllToken(7)
      val depId = tokenseq(i) \@ "id"
      val headId = conllToken(6).toInt match {
        case 0 => "root"
        case j => tokenseq(j - 1) \@ "id"
      }
      <dependency id={ Annotation.Dependency.nextId } head={ headId } dependent={ depId } deprel={ deprel } />
    }
    sentence addOrOverwriteChild (
      <dependencies annotators={ name }>{ deps }</dependencies>)
  }
}

object UDPipeAnnotator extends AnnotatorCompanion[UDPipeAnnotator] {

  override def fromProps(name: String, props: Properties) = {
    def error() = throw new ArgumentError(
      s"Invalid options for udpipe (${name}), which should be like udpipe[parse] or udpipe[tokenize,pos,parse]")
    name.indexOf('[') match {
      case -1 =>
        // Launch if annotator's help is invoked.
        val dummy = PropertiesUtil.findProperty("help", props).filter { p =>
          p.split(",").map(_.trim).contains(name)
        }.map(_ => new All(name, props))
        dummy getOrElse error()
      case b =>
        val base = name.substring(0, b)
        name.substring(b+1, name.size-1).split("""[,\s]+""").toSeq match {
          case Seq("tokenize", "pos", "parse") => new All(base, props)
          case Seq("tokenize") => new Tokenize(base, props)
          case Seq("pos") => new POS(base, props)
          case Seq("parse") => new Parse(base, props)
          case Seq("tokenize", "pos") => new TokenizePOS(base, props)
          case Seq("pos", "parse") => new POSParse(base, props)
          case _ => error()
        }
    }
  }

  val tokenizeRequirements: Set[Requirement] =
    Set(Requirement.Ssplit, Requirement.Tokenize)
  val posRequirements: Set[Requirement] =
    Set(Requirement.POS, Requirement.UPOS, Requirement.UDFeatures)
  val parseRequirements: Set[Requirement] = Set(Requirement.Dependencies)

  class All(override val name: String, override val props: Properties)
      extends UDPipeAnnotator {

    def mode = "all"
    def doTokenize = true
    def doPOS = true
    def doDeps = true

    override val requirementsSatisfied =
      tokenizeRequirements | posRequirements | parseRequirements
  }

  class Tokenize(override val name: String, override val props: Properties)
      extends UDPipeAnnotator {

    def mode = "tok"
    def doTokenize = true
    def doPOS = false
    def doDeps = false

    override val requirementsSatisfied = tokenizeRequirements
  }

  class POS(override val name: String, override val props: Properties)
      extends UDPipeAnnotator {

    def mode = "pos"
    def doTokenize = false
    def doPOS = true
    def doDeps = false

    override val requires = tokenizeRequirements
    override val requirementsSatisfied = posRequirements
  }

  class Parse(override val name: String, override val props: Properties)
      extends UDPipeAnnotator {

    def mode = "par"
    def doTokenize = false
    def doPOS = false
    def doDeps = true

    override val requires = tokenizeRequirements | posRequirements
    override val requirementsSatisfied = parseRequirements
  }

  class TokenizePOS(override val name: String, override val props: Properties)
      extends UDPipeAnnotator {

    def mode = "tok|pos"
    def doTokenize = true
    def doPOS = true
    def doDeps = false

    override val requirementsSatisfied = tokenizeRequirements | posRequirements
  }

  class POSParse(override val name: String, override val props: Properties)
      extends UDPipeAnnotator {

    def mode = "pos|par"
    def doTokenize = false
    def doPOS = true
    def doDeps = true

    override val requires = tokenizeRequirements
    override val requirementsSatisfied = posRequirements | parseRequirements
  }
}
