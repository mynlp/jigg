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

import scala.collection.mutable.ArrayBuffer
import scala.xml._
import jigg.util.XMLUtil.RichNode

trait KNPAnnotator extends Annotator { self=>

  @Prop(gloss = "If true (default), replace tokens node with the KNP outputs. If false, remain the juman outputs.") var replaceJumanTokens = true

  def command: String

  trait BaseKNPLocalAnnotator extends IOCreator {
    def softwareUrl = "http://nlp.ist.i.kyoto-u.ac.jp/index.php?KNP"
    def command = self.command

    def knp: IO
    override def close() = knp.close()

    override def launchTesters = Seq(
      LaunchTester("EOS", _ == "EOS", _ startsWith "# S-ID"))

    def runKNP(sentence: Node, beginInput: Option[String]): Seq[String] = {
      val jumanTokens = (sentence \ "tokens").head

      val _output = recoverJumanOutput(jumanTokens)
      val jumanOutput = beginInput map (Iterator(_) ++ _output) getOrElse _output

      knp safeWriteWithFlush jumanOutput

      val result = knp readUntil (_ == "EOS")
      if (result(0).startsWith(";;") || result(0).contains("ERROR"))
        throw new ProcessError(result mkString "\n")
      else result
    }

    def annotateSentenceNode(
      sentence: Node,
      knpOutput: Seq[String],
      sentenceId: String,
      nPrevSentenceId: Int=>String): Node = {

      val analyzer = new SentenceAnalyzer(knpOutput, sentenceId)

      val knpTokens = analyzer.extractTokens()
      val basicPhrases = analyzer.extractBasicPhrases()

      val tokenAdded = replaceJumanTokens match {
        case true => sentence addOrOverwriteChild knpTokens
        case false => sentence addChild knpTokens
      }

      val otherChildren = Seq[Node](
        basicPhrases,
        analyzer.extractChunks(),
        analyzer.extractBasicPhraseDependencies(),
        analyzer.extractChunkDependencies(),
        analyzer.extractCaseRelations(knpTokens, basicPhrases, nPrevSentenceId),
        analyzer.extractNEs(knpTokens)
      )
      tokenAdded addChild otherChildren
    }

    private[this] val jumanFeats = Array("form", "yomi", "lemma", "pos", "posId", "pos1",
      "pos1Id", "cType", "cTypeId", "cForm", "cFormId", "misc").map("@" + _)

    private[this] def recoverTokenStr(tokenNode: Node, alt: Boolean): String = {
      def head = if (alt) "@ " else ""
      head + jumanFeats.map { a => (tokenNode \ a).text }.mkString(" ")
    }

    def recoverJumanOutput(jumanTokens: Node): Iterator[String] = {
      val output = ArrayBuffer.empty[String]

      for (tok <- jumanTokens \\ "token") {
        output += recoverTokenStr(tok, false)

        for (alt <- tok \ "tokenAlt") output += recoverTokenStr(alt, true)
      }
      output += "EOS"
      output.toIterator
    }
  }

  class SentenceAnalyzer(output: Seq[String], sentenceId: String) {

    val idgen = new IdGen(sentenceId)
    import idgen._

    private def isDocInfo(line: String) = line.startsWith("# ")
    private def isChunk(line: String) = line.startsWith("* ")
    private def isBasicPhrase(line: String) = line.startsWith("+ ")
    private def isToken(line: String) =
      !isDocInfo(line) && !isChunk(line) && !isBasicPhrase(line) && line != "EOS"

    def extractTokens(): Node = {
      val tokens = output filter isToken
      // this is OK since half space errors are detected earlier
      val tokenSizes = tokens map (_ indexOf ' ')
      val tokenOffsets = tokenSizes.scanLeft(0) { _ + _ }

      val nodes = (output filter isToken).zipWithIndex map { case (tokenized, idx) =>

        val spaceIdx = -1 +: (0 until tokenized.size - 1).filter(tokenized(_) == ' ')
        def feat(i: Int) = tokenized substring (spaceIdx(i) + 1, spaceIdx(i + 1))

        val semantic = tokenized substring (spaceIdx(11) + 1)

        val offsetBegin = tokenOffsets(idx)
        val offsetEnd = tokenOffsets(idx + 1)

        JumanAnnotator.tokenNode(tokenId(idx), feat, semantic, offsetBegin, offsetEnd)
      }
      <tokens annotators={ name }>{ nodes }</tokens>
    }

    def extractBasicPhrases(): Node =
      <basicPhrases annotators={ name }>{
        extractPhraseNodes("basicPhrase", basicPhraseId, isBasicPhrase)
      }</basicPhrases>

    def extractChunks(): Node =
      <chunks annotators={ name }>{
        extractPhraseNodes("chunk", chunkId, isChunk)
      }</chunks>

    private def extractPhraseNodes(
      label: String,
      phraseId: Int=>String,
      isPhraseLine: String=>Boolean): NodeSeq = {

      var tokenOffset = 0

      def boundaryToPhrase(begin: Int, end: Int, idx: Int) = {
        val phraseLine = output(begin)
        val tokenIds = {
          val numTokens = (begin + 1 until end) count { i => isToken(output(i)) }
          val ids = (0 until numTokens) map { i => tokenId (i + tokenOffset) }
          tokenOffset += numTokens
          ids
        }
        <a id={ phraseId(idx) } tokens={ tokenIds.mkString(" ") }
          misc={ phraseLine.split(" ")(2) }/> copy (label = label)
      }

      val boundaries =
        (0 until output.size).filter(i => isPhraseLine(output(i))) :+ output.size

      boundaries.sliding(2).zipWithIndex.map {
        case (Seq(begin, end), idx) =>
          boundaryToPhrase(begin, end, idx)
      }.toVector
    }

    def extractBasicPhraseDependencies(): Node =
      <dependencies unit="basicPhrase" annotators={ name }>{
        depNodes(output filter isBasicPhrase, basicPhraseId, basicPhraseDepId)
      }</dependencies>

    def extractChunkDependencies(): Node =
      <dependencies unit="chunk" annotators={ name }>{
        depNodes(output filter isChunk, chunkId, depId)
      }</dependencies>

    private def depNodes(
      depLines: Iterable[String],
      unitId: Int=>String,
      depId: Int=>String) =
    depLines.zipWithIndex map { case (line, i) =>

      val items = line split " "
      val head = items(1).init.toInt match {
        case -1 => "root"
        case h => unitId(h)
      }
      val dep = unitId(i)
      val rel = items(1).last.toString

      <dependency id={depId(i)} head={head} dependent={dep} deprel={rel} />
    }

    /**
      *
      * @param tokensNode <tokens>...</tokens>
      * @param basicPhrasesNode <basicPhrases>...</basicPhrases>
      * @param nPrevSentenceId the id of the i-th sentence from this (to left).
      * This is necessary since arguments for some predicate may point to
      * the entities in the previous sentences.
      */
    def extractCaseRelations(
      tokensNode: Node,
      basicPhrasesNode: Node,
      nPrevSentenceId: Int=>String): Node = {

      val idGen = jigg.util.LocalIDGenerator(caseId)

      // "格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-
      def extractCaseStr(line: String): Option[String] =
        line indexOf "<格解析結果:" match {
          case -1 => None
          case begin =>
            val end = line indexOf (">", begin + 7) // 7 is the index of char next to :
            val items = line substring (begin, end) split ":"
            Some(items(3))
        }

      def basicPhraseToCaseNodes(basicPhrase: String, idx: Int): NodeSeq =
        extractCaseStr(basicPhrase) map (extractCaseNodes(_, idx)) getOrElse Seq()

      def extractCaseNodes(casesStr: String, idx: Int): NodeSeq = {
        val predId = basicPhraseId(idx)

        val nodes = casesStr split ";" map (_ split "/") map { items =>
          // items = [ガ,C,太郎,0,0,1]
          val label = items(0)
          val flag = items(1) match {
            case "E" => "E/" + items(2) // e.g., E/不特定:人
            case f => f // in other tags, items(2) is redundancy so we remove
          }
          val text = items(2)

          def findArgumentTokenId(argPhraseIdx: Int, nPrev: Int): String = {
            val argPhraseId = nPrev match {
              case 0 => basicPhraseId(argPhraseIdx)
              case n => new IdGen(nPrevSentenceId(n)) basicPhraseId argPhraseIdx
            }

            val argPhrase = basicPhrasesNode \\ "basicPhrase" filter {
              _ \@ "id" == argPhraseId
            }
            val tokenIds = argPhrase \@ "tokens" split " "

            val matchedToken = tokensNode \\ "token" filter { t =>
              // TODO: form may be after preprocessed text while text may not.
              tokenIds.contains(t \@ "id") && t \@ "form" == text
            }
            matchedToken \@ "id"
          }

          val argId = (items(3), items(4)) match {
            case (_, "-") | ("-", _) => "unk"
            case (a, b) =>
              val argPhraseIdx = a.toInt
              val nPrev = b.toInt

              findArgumentTokenId(argPhraseIdx, nPrev) match {
                case "" => "unk"
                case id => id
              }
          }
          <caseRelation id={ idGen.next } pred={ predId } arg={ argId } deprel={ label }
          flag={ flag }/>
        }
        nodes.toSeq
      }

      val casesNode = for (
        (basicPhrase, idx) <- output.filter(isBasicPhrase).zipWithIndex;
        caseNode <- basicPhraseToCaseNodes(basicPhrase, idx)
      ) yield caseNode

      <caseRelations annotators="knp">{ casesNode }</caseRelations>
    }

    def extractNEs(tokensNode: Node): Node = {
      val tokens = tokensNode \\ "token"
      val labelTags: Seq[Option[(String, String)]] =
        tokens map (_ \@ "misc") map { misc =>
          misc indexOf "<NE:" match {
            case -1 => None
            case begin =>
              val labelBegin = begin + 4 // 4 is the position next to first :
              val end = misc indexOf ('>', labelBegin)
              val seg = misc indexOf (':', labelBegin)
              Some((misc substring (labelBegin, seg), misc substring (seg + 1, end)))
          }
        }

      val nes: Seq[(String, String)] = (0 until labelTags.size) collect { begin =>
        labelTags(begin) match {
          case Some((label, "S")) => (tokens(begin) \@ "id", label)
          case Some((label, "B")) =>
            val end = (begin + 1 until labelTags.size) find { i =>
              labelTags(i) match {
                case Some((`label`, "E")) => true
                case _ => false
              }
            } getOrElse -1
            val internal =
              (begin + 1 until end) filter { labelTags(_) == (label, "I") }

            val tokenIds = (begin +: internal :+ end) map (tokens(_) \@ "id")

            (tokenIds mkString " ", label)
        }
      }
      val neNodes = nes.zipWithIndex map { case ((tokens, label), i) =>
        <NE id={ neId(i) } tokens={ tokens } label={ label } />
      }
      <NEs annotators="knp">{ neNodes }</NEs>
    }
  }

  class IdGen(sid: String) {
    def tokenId(idx: Int) = _id("knpt", idx)
    def chunkId(idx: Int) = _id("knpc", idx)
    def basicPhraseId(idx: Int): String = _id("knpbp", idx)
    def basicPhraseDepId(idx: Int) = _id("knpbpdep", idx)
    def depId(idx: Int) = _id("knpdep", idx)
    def caseId(idx: Int) = _id("knpcr", idx)
    def neId(idx: Int) = _id("knpne", idx)

    private def _id(k: String, v: Int): String = sid + "_" + k + v
  }

  override def requires = Set(JaRequirement.Juman)
  override def requirementsSatisfied = {
    import JaRequirement._
    Set(KNPChunk, ChunkDependencies, BasicPhrase, BasicPhraseDependencies, Requirement.NER)
  }
}
