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
import scala.collection.JavaConverters._
import scala.collection.immutable.SortedMap
import scala.collection.mutable.ArrayBuffer
import scala.xml._
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode
import edu.stanford.nlp.hcoref.data.CorefChain
import edu.stanford.nlp.hcoref.CorefCoreAnnotations
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.trees.{GrammaticalRelation, Tree, Trees, TreeCoreAnnotations,
  TypedDependency}
import edu.stanford.nlp.ling.{CoreAnnotations, IndexedWord}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.semgraph.{SemanticGraph, SemanticGraphCoreAnnotations}

import edu.stanford.nlp.process.CoreLabelTokenFactory

import edu.stanford.nlp.{pipeline => core}

class StanfordCoreNLPAnnotator(
  override val name: String,
  override val props: Properties,
  val annotatorNames: Seq[String]) extends Annotator {

  val coreNLPProps: Properties = {
    val keys = props.keys.asScala.toSeq.map(_.toString) filter (_ startsWith (name + "."))
    val p = new Properties
    val offset = (name + ".").size
    for (k <- keys) {
      val v = props.getProperty(k)
      p.put(k.substring(offset), v)
    }
    p.put("annotators", annotatorNames.mkString(","))
    p.put("threads", nThreads + "")
    p
  }

  def supportedAnnotators = Seq("tokenize", "ssplit", "pos", "lemma", "ner", "regexner", "parse", "depparse", "dcoref", "coref")

  val coreNLP = new StanfordCoreNLP(coreNLPProps, false)

  val annotators: Seq[core.Annotator] = // annotatorNames map (coreNLP.getPool.get)
    annotatorNames map StanfordCoreNLP.getExistingAnnotator

  val requirementMap: Map[Requirement, CoreNLPRequirement] = Map(
    Requirement.Tokenize -> new Tokenize,
    Requirement.Ssplit -> new Ssplit,
    Requirement.POS -> new POS,
    Requirement.Lemma -> new Lemma,
    Requirement.StanfordNER -> new NER,
    Requirement.Parse -> new Parse,
    Requirement.BasicDependencies -> new BasicDependencies,
    Requirement.CollapsedDependencies -> new CollapsedDependencies,
    Requirement.CollapsedCCProcessedDependencies -> new CollapsedCCProcessedDependencies,
    Requirement.Mention -> new MentionCandidates,
    Requirement.Coreference -> new Coreference
  )

  val requirementOrder = Seq(
    Requirement.Tokenize,
    Requirement.Ssplit,
    Requirement.POS,
    Requirement.Lemma,
    Requirement.StanfordNER,
    Requirement.Parse,
    Requirement.BasicDependencies,
    Requirement.CollapsedDependencies,
    Requirement.CollapsedCCProcessedDependencies,
    Requirement.Mention,
    Requirement.Coreference
  )

  def sortRequirements(elems: Set[Requirement]): Seq[Requirement] =
    elems.toSeq.sortBy(requirementOrder.indexOf(_))

  private val requiresByEach: Seq[Set[Requirement]] =
    convRequirements(annotators map (_.requires.asScala.toSet))
  private val requirementsSatisfiedByEach: Seq[Set[Requirement]] = {
    def reqSet(a: core.Annotator): Set[core.Annotator.Requirement] = a match {
      case a: core.TokensRegexNERAnnotator =>
        Set(StanfordCoreNLPAnnotator.REGEX_REQUIREMENT)
      case _ => a.requirementsSatisfied.asScala.toSet
    }
    convRequirements(annotators map reqSet)
  }

  private def convRequirements(seq: Seq[Set[core.Annotator.Requirement]]):
      Seq[Set[Requirement]] = {

    def conv(set: Set[core.Annotator.Requirement], name: String): Set[Requirement] =
      set flatMap (StanfordCoreNLPAnnotator.requirementMap.getOrElse(_,
        throw new ArgumentError("$name in Stanford CoreNLP is yet unsupported in jigg.")))

    assert(seq.size == annotatorNames.size)
    (0 until seq.size) map { i => conv(seq(i), annotatorNames(i)) }
  }

  override val requires = requiresByEach.headOption.getOrElse(Set[Requirement]()) ++
    (1 until requiresByEach.size).foldLeft(Set[Requirement]()) { (newones, i) =>
      val unsatisified =
        requiresByEach(i) --
          (0 until i).map(requirementsSatisfiedByEach).foldLeft(Set[Requirement]())(_ ++ _)
      newones ++ unsatisified
    }

  override val requirementsSatisfied =
    requirementsSatisfiedByEach.foldLeft(Set[Requirement]())(_ ++ _)

  override def description = s"""${super.description}

  A wrapper for Stanford CoreNLP. Currently the following (internal) annotators are supported:
    ${supportedAnnotators mkString ", "}

  All properties prefixed with $name are passed to Stanford CoreNLP. For example, the
  following command:
    jigg.pipeline.Pipeline -annotators "${name}[tokenize,ssplit,pos]" -${name}.tokenize.whitespace true -${name}.pos.model ./corenlp-model.tagger

  will create a CoreNLP instance with the properties "-tokenize.whitespace true" and
  "-pos.model ./corenlp-model.tagger".

  See the original documentation in Stanford CoreNLP website
  (http://stanfordnlp.github.io/CoreNLP/annotators.html) for the supported options for
  each annotator.

  Different CoreNLP instances can be combined in a pipeline as follows:
    -annotators "corenlp[tokenize,ssplit],berkeleyparser,corenlp[lemma,ner,dcoref]"

  This annotation proceeds as follows: (1) the first CoreNLP performs tokenize and ssplit;
  (2) Berkeley parser performs POS tagging and parsing; and finally (3) the second
  CoreNLP performs lemma, ner, dcoref given the annotations so far.

  Note that the model file for Stanford CoreNLP is not included Jigg. Please download
  it from:
    http://nlp.stanford.edu/software/stanford-english-corenlp-2016-01-10-models.jar
  and include it into your path!
""" + (if (annotatorNames.isEmpty) """
  To see the requirements and satisfied requirements for this annotator, please call
  the help with specific annotators, e.g., -help "${name}[parse,lemma]".

  This will tell you which requirements should be satisfied beforehand.
""" else "")

  override def annotate(root: Node) = {

    root.replaceAll("document") { e =>
      val coreAnnotation = new core.Annotation(e.text)
      for (r <- sortRequirements(requires))
        requirementMap(r).addToCoreMap(coreAnnotation, e)

      coreNLP.annotate(coreAnnotation)

      sortRequirements(requirementsSatisfied).foldLeft(e: Node) { (node, r) =>
        requirementMap(r).addToNode(node, coreAnnotation)
      }
    }
  }

  override def checkRequirements(satisfiedSoFar: RequirementSet): RequirementSet =
    (0 until annotators.size).foldLeft(satisfiedSoFar) { (sofar, i) =>
      val requires = requiresByEach(i)
      sofar.lackedIn(requires) match {
        case a if a.isEmpty =>
          sofar | requirementsSatisfiedByEach(i)
        case lacked =>
          throw new RequirementError("annotator %s in %s requires %s"
            .format(annotatorNames(i), name, lacked.mkString(", ")))
      }
    }

  trait CoreNLPRequirement {
    def addToCoreMap(annotation: core.Annotation, node: Node): Unit
    def addToNode(node: Node, annotation: core.Annotation): Node
  }

  class DoNothing extends CoreNLPRequirement {
    def addToCoreMap(annotation: core.Annotation, node: Node): Unit = {}
    def addToNode(node: Node, annotation: core.Annotation): Node = node
  }

  class Tokenize extends CoreNLPRequirement {

    def addToCoreMap(annotation: core.Annotation, document: Node): Unit = {
      val tf = new CoreLabelTokenFactory

      // TODO: this is dangarous if tokens nodes are assigned by several annotators
      val sentences = document \\ "sentence"
      val corelabels: java.util.List[CoreLabel] = sentences.flatMap { s =>
        val offset = (s \@ "characterOffsetBegin").toInt

        val tokens = s \\ "token"
        tokens map { t =>
          val begin = (t \@ "characterOffsetBegin").toInt + offset
          val end = (t \@ "characterOffsetEnd").toInt + offset

          tf.makeToken(t \@ "form", begin, end - begin)
        }
      }.asJava

      annotation.set(classOf[CoreAnnotations.TokensAnnotation], corelabels)
    }

    /** CoreNLP's pipeline is tokenize -> ssplit, but jigg assumes tokens nodes are
      * placed in each sentence node. We thus treat the result of tokenization by
      * CoreNLP as tokens in a very long sentence (entire document), which is later
      * segmented by ssplit.
      */
    def addToNode(document: Node, annotation: core.Annotation): Node = {

      def toTokenNode(corelabel: CoreLabel): Node = {
        val begin = corelabel.beginPosition
        val end = corelabel.endPosition
        <token
        id={ Annotation.Token.nextId }
        form={ corelabel.word }
        characterOffsetBegin={ begin+"" }
        characterOffsetEnd={ end+"" }/>
      }
      val corelabels =
        annotation.get(classOf[CoreAnnotations.TokensAnnotation]).asScala

      val tokens = <tokens annotators={name}>{ corelabels map toTokenNode }</tokens>

      val end = corelabels.lastOption map (_.endPosition) getOrElse 0
      // this sentence id is dummy (would be changed by followd Ssplit)
      val sentences =
        <sentences>
          <sentence id="s0" characterOffsetBegin="0" characterOffsetEnd={end+""}>
            { tokens }
           </sentence>
        </sentences>

      document addChild Seq(sentences)
    }
  }

  class Ssplit extends CoreNLPRequirement {

    // TODO: broken if document has multiple <sentences> annotaiton?
    /** Assuming annotation has already TokensAnnotation
      * The job of this annotator is to add SentencesAnnotation and assign index (in
      * a sentence) into each token.
      */
    def addToCoreMap(annotation: core.Annotation, document: Node): Unit = {

      val sentences = document \\ "sentence"

      val coreTokens: java.util.List[CoreLabel] =
        annotation get classOf[CoreAnnotations.TokensAnnotation]

      val sentenceOffsets = sentences.map { s => (s \\ "token").size }.scanLeft(0)(_ + _)

      val coreSentences: java.util.List[CoreMap] = (0 until sentences.size).map { i =>
        val sentence = sentences(i)

        val text = sentence.text
        val begin = sentence \@ "characterOffsetBegin"
        val end = sentence \@ "characterOffsetEnd"

        val tokenOffsetBegin = sentenceOffsets(i)
        val tokenOffsetEnd = sentenceOffsets(i) + (sentence \\ "token").size

        val sentenceTokens = coreTokens subList (tokenOffsetBegin, tokenOffsetEnd)

        val ann: CoreMap = new core.Annotation(text)
        ann.set(
          classOf[CoreAnnotations.CharacterOffsetBeginAnnotation], new Integer(begin))
        ann.set(classOf[CoreAnnotations.CharacterOffsetEndAnnotation], new Integer(end))
        ann.set(classOf[CoreAnnotations.TokensAnnotation], sentenceTokens)
        ann.set(
          classOf[CoreAnnotations.TokenBeginAnnotation], new Integer(tokenOffsetBegin))
        ann.set(classOf[CoreAnnotations.TokenEndAnnotation], new Integer(tokenOffsetEnd))
        ann.set(classOf[CoreAnnotations.SentenceIndexAnnotation], new Integer(i))

        ann.set(classOf[CoreAnnotations.DocIDAnnotation], document \@ "id")

        sentenceTokens.asScala.zipWithIndex foreach { case (token, j) =>
          token setIndex (j + 1) // start from 1
          token setSentIndex (i)
        }
        ann
      }.asJava

      annotation.set(classOf[CoreAnnotations.SentencesAnnotation], coreSentences)
    }

    /** Assuming document has one sentence, which is not yet segmented.
      * Let us segment this very long sentence into sentences using information
      * of annotation (segmented CoreMap).
      */
    def addToNode(document: Node, annotation: core.Annotation): Node = {
      val docId = document \@ "id"

      // TODO: only deal with the first sentences block. is it ok?
      val sentencesNode = (document \\ "sentences").head.asInstanceOf[Elem]

      val sentences = sentencesNode \ "sentence"
      assert(sentences.size == 1)

      val currentTokens = sentences(0) \\ "token"
      val tokenBegins = currentTokens map { t => (t \@ "characterOffsetBegin").toInt }

      val coreSentences: Seq[CoreMap] =
        annotation.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala

      var tokenOffset = 0

      val sentenceNodes = coreSentences.zipWithIndex map { case (coreSentence, i) =>

        val characterOffsetBegin =
          coreSentence get classOf[CoreAnnotations.CharacterOffsetBeginAnnotation]
        val characterOffsetEnd =
          coreSentence get classOf[CoreAnnotations.CharacterOffsetEndAnnotation]

        // We recover tokenBegin here because CoreAnnotations.TokenBeginAnnotation
        // does not necessarily represent the token offset correctly.
        val tokenBegin = tokenBegins indexWhere (_ == characterOffsetBegin, tokenOffset)
        val tokenEnd = tokenBegin +
          (coreSentence get classOf[CoreAnnotations.TokensAnnotation]).size
        tokenOffset = tokenEnd

        val tokens = (tokenBegin until tokenEnd) map { case i =>
          val currentToken = currentTokens(i)
          val currentOffsetBegin = (currentToken \@ "characterOffsetBegin").toInt
          val currentOffsetEnd = (currentToken \@ "characterOffsetEnd").toInt

          // update offset values
          currentToken addAttributes Map(
            "characterOffsetBegin" ->
              (currentOffsetBegin - characterOffsetBegin).toString,
            "characterOffsetEnd" ->
              (currentOffsetEnd - characterOffsetBegin).toString
          )
        }
        val text = coreSentence get classOf[CoreAnnotations.TextAnnotation]

        <sentence
          id={ Annotation.Sentence.nextId }
          characterOffsetBegin={ characterOffsetBegin+"" }
          characterOffsetEnd={ characterOffsetEnd+"" }>
          { text }
          <tokens annotators={ name }>{ tokens }</tokens>
        </sentence>
      }
      document addOrOverwriteChild sentencesNode.copy(child=sentenceNodes)
    }
  }

  trait SentenceRequirement extends CoreNLPRequirement {

    def addToCoreMap(annotation: core.Annotation, document: Node): Unit = {
      val sentences = document \\ "sentence"
      val coreSentences =
        (annotation get classOf[CoreAnnotations.SentencesAnnotation]).asScala
      assert(sentences.size == coreSentences.size)

      coreSentences zip sentences foreach { case (coreSentence, sentence) =>
        addToCoreSentence(coreSentence, sentence)
      }
    }

    def addToNode(document: Node, annotation: core.Annotation): Node = {
      val coreSentences =
        (annotation get classOf[CoreAnnotations.SentencesAnnotation]).asScala

      document.replaceAll("sentences") { e =>
        val sentenceSeq = e \ "sentence"
        assert(sentenceSeq.size == coreSentences.size)
        val newChild = sentenceSeq zip coreSentences map {
          case (sentence, coreSentence) => newSentenceAnnotation(sentence, coreSentence)
        }
        e copy (child = newChild)
      }
    }

    protected def addToCoreSentence(coreSentence: CoreMap, sentence: Node): Unit

    protected def newSentenceAnnotation(sentence: Node, coreSentence: CoreMap): Node
  }

  trait TokenAttrRequirement extends SentenceRequirement {

    def addToCoreSentence(coreSentence: CoreMap, sentence: Node): Unit = {
      val tokens = sentence \\ "token"
      val coreTokens =
        (coreSentence get classOf[CoreAnnotations.TokensAnnotation]).asScala

      coreTokens zip tokens foreach { case (coreToken, token) =>
        updateCoreLabel(coreToken, token)
        // coreToken set (classOf[CoreAnnotations.PartOfSpeechAnnotation], token \@ "pos")
      }
    }

    def newSentenceAnnotation(sentence: Node, coreSentence: CoreMap): Node = {
      val tokensNode = (sentence \ "tokens").head
      val tokens = tokensNode \ "token"
      val coreTokens =
        (coreSentence get classOf[CoreAnnotations.TokensAnnotation]).asScala

      val modifiedTokens = tokens zip coreTokens map { case (token, coreToken) =>
        updateToken(token, coreToken)
      }
      val newTokensNode = {
        val namedTokensNode = tokensNode addAnnotatorName name
        namedTokensNode replaceChild modifiedTokens
      }
      sentence addOrOverwriteChild newTokensNode
    }

    protected def updateCoreLabel(coreToken: CoreLabel, token: Node): Unit

    protected def updateToken(token: Node, coreToken: CoreLabel): Node
  }

  class POS extends TokenAttrRequirement {

    def updateCoreLabel(coreToken: CoreLabel, token: Node) =
      coreToken set (classOf[CoreAnnotations.PartOfSpeechAnnotation], token \@ "pos")

    def updateToken(token: Node, coreToken: CoreLabel) =
      token addAttribute ("pos",
        coreToken get classOf[CoreAnnotations.PartOfSpeechAnnotation])
  }

  class Lemma extends TokenAttrRequirement {

    def updateCoreLabel(coreToken: CoreLabel, token: Node) =
      coreToken set (classOf[CoreAnnotations.LemmaAnnotation], token \@ "lemma")

    def updateToken(token: Node, coreToken: CoreLabel) =
      token addAttribute ("lemma", coreToken get classOf[CoreAnnotations.LemmaAnnotation])
  }

  // CoreNLP annotates NER information for each token, but but Jigg
  // tries to separate it in NEs tag under the sentence.
  class NER extends SentenceRequirement {

    def addToCoreSentence(coreSentence: CoreMap, sentence: Node): Unit = {
      val tokens = sentence \\ "token"
      val nes = sentence \\ "NE"

      val coreTokens =
        (coreSentence get classOf[CoreAnnotations.TokensAnnotation]).asScala

      for (ne: Node <- nes) {
        val neTokens = (ne \@ "tokens") split " "
        val label = ne \@ "label"
        val normalizedLabel = ne \ "@normalizedLabel"
        val begin = tokens.indexWhere { t => (t \@ "id") == neTokens(0) }
        val end = tokens.indexWhere ({ t => (t \@ "id") == neTokens.last}, begin)

        for (i <- begin to end) {
          coreTokens(i) set (classOf[CoreAnnotations.NamedEntityTagAnnotation], label)
          normalizedLabel match {
            case Seq() =>
            case n: NodeSeq =>
              coreTokens(i) set (
                classOf[CoreAnnotations.NormalizedNamedEntityTagAnnotation], n.head+"")
          }
        }
      }
    }

    def newSentenceAnnotation(sentence: Node, coreSentence: CoreMap) = {
      val sentenceId = sentence \@ "id"
      val tokensNode = (sentence \ "tokens").head
      val tokens = tokensNode \ "token"
      val coreTokens =
        (coreSentence get classOf[CoreAnnotations.TokensAnnotation]).asScala

      val tags: Seq[(String, String)] = coreTokens map { t =>
        (t get classOf[CoreAnnotations.NamedEntityTagAnnotation],
          t get classOf[CoreAnnotations.NormalizedNamedEntityTagAnnotation])
      }

      val segments: Seq[Int] = 0 +: (1 until tags.size).filter { i =>
        tags(i - 1) != tags(i)
      }

      val neSpans: Seq[(Int, Int)] = (0 until segments.size) filter { i =>
        val t = tags(segments(i))._1
        t != "O" && t != null
      } map { i =>
        val begin = segments(i)
        val end = i match {
          case j if j == segments.size - 1 => tokens.size // special treat for last
          case _ => segments(i + 1)
        }
        (begin, end)
      }

      val neSeq = neSpans.zipWithIndex map { case ((begin, end), i) =>
        val id = sentenceId + "_corene" + i

        val neTokens = (begin until end) map { i => tokens(i) \@ "id" } mkString " "
        val label = tags(begin)._1
        val normalizedLabel = tags(begin)._2 match {
          case null => None
          case l => Some(Text(l))
        }
        <NE id={ id }
          label={ label } normalizedLabel={ normalizedLabel } tokens={ neTokens }/>
      }
      val nesNode = <NEs annotators={ name }>{ neSeq }</NEs>
      sentence addOrOverwriteChild nesNode
    }
  }

  class Parse extends SentenceRequirement {

    // set TreeCoreAnnotations.TreeAnnotation to sentence
    def addToCoreSentence(coreSentence: CoreMap, sentence: Node): Unit = {
      val tokens = sentence \\ "token"
      val parse = (sentence \ "parse").head

      val coreTokens =
        (coreSentence get classOf[CoreAnnotations.TokensAnnotation]).asScala

      val parseStr = StanfordCoreNLPAnnotator.parseStr(tokens, parse)

      val tree = Trees.readTree(parseStr)
      ParserAnnotatorUtils.fillInParseAnnotations(
        false, false, null, coreSentence, Seq(tree).asJava, null)
    }

    // In default, POS attributes are overwritten.
    def newSentenceAnnotation(sentence: Node, coreSentence: CoreMap) = {
      val sentenceId = sentence \@ "id"

      val tree = coreSentence get classOf[TreeCoreAnnotations.TreeAnnotation]
      val tokens = (sentence \ "tokens").head
      val tokenSeq = tokens \ "token"

      // add pos
      val updatedTokenSeq = {
        val leaves = Trees.preTerminals(tree).asScala
        tokenSeq zip leaves map { case (token, leave) =>
          val pos = leave.label.asInstanceOf[CoreLabel].value
          token addAttribute ("pos", pos)
        }
      }

      val spans = new ArrayBuffer[Node]

      var tokIdx = -1

      def nextTokId = { tokIdx += 1; updatedTokenSeq(tokIdx) \@ "id" }

      // process a node (which has no span id), and return the assigned id
      def addSpanAndGetId(node: Tree): String = node.isPreTerminal match {
        case true => nextTokId // This traverse is ensured to be left to right,
                               // so this is ok
        case false =>
          val label = node.label.asInstanceOf[CoreLabel]
          val symbol = label.value

          val childIds = node.children map { c => addSpanAndGetId(c) } mkString " "

          val _id = Annotation.ParseSpan.nextId
          spans += <span id={ _id } symbol={ symbol } children={ childIds } />
          _id
      }
      val root = addSpanAndGetId(tree.skipRoot)

      val namedTokensNode = tokens.addAnnotatorName(name)
        .asInstanceOf[Elem].copy(child=updatedTokenSeq)
      val parseNode = <parse root={ root } annotators={ name }>{ spans }</parse>

      sentence addOrOverwriteChild Seq(namedTokensNode, parseNode)
    }
  }

  // NOTE: CoreNLP's IndexWord is 1-based (maybe 0 is used for (dummy) root)
  trait StanfordDependencies extends SentenceRequirement {

    override def addToCoreMap(annotation: core.Annotation, document: Node) = {
      val docId = document \@ "id"
      val sentences = document \\ "sentence"
      val coreSentences =
        (annotation get classOf[CoreAnnotations.SentencesAnnotation]).asScala
      assert(sentences.size == coreSentences.size)

      coreSentences.zip(sentences).zipWithIndex.foreach {
        case ((coreSentence, sentence), sentIdx) =>
          val typedDeps: java.util.Collection[TypedDependency] =
            StanfordCoreNLPAnnotator.extractTypedDependencies(
              sentence, depType, docId, sentIdx).toIterable.asJavaCollection

          val graph = new SemanticGraph(typedDeps)
          setGraph(coreSentence, graph)
      }
    }

    // this method is not used
    override def addToCoreSentence(coreSentence: CoreMap, sentence: Node): Unit = {}

    def newSentenceAnnotation(sentence: Node, coreSentence: CoreMap) = {
      val semgraph = semanticGraph(coreSentence)

      val depsNode = StanfordCoreNLPAnnotator.semanticGraphToDependenciesNode(
        sentence, semgraph, depType, name)

      // We override dependencies only when it has a different type.
      sentence addOrOverwriteChild (depsNode, Some("type"))
    }

    protected def setGraph(sentence: CoreMap, graph: SemanticGraph): Unit
    protected def semanticGraph(sentence: CoreMap): SemanticGraph
    protected def depType: String
  }

  // this is not SentenceRequirement as it uses document id and sentence idx
  class BasicDependencies extends StanfordDependencies {
    def setGraph(sentence: CoreMap, graph: SemanticGraph) =
      sentence set (
        classOf[SemanticGraphCoreAnnotations.BasicDependenciesAnnotation], graph)

    def semanticGraph(sentence: CoreMap): SemanticGraph =
      sentence get classOf[SemanticGraphCoreAnnotations.BasicDependenciesAnnotation]

    def depType: String = StanfordCoreNLPAnnotator.basicDepType
  }

  class CollapsedDependencies extends StanfordDependencies {
    def setGraph(sentence: CoreMap, graph: SemanticGraph) =
      sentence set (
        classOf[SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation], graph)

    def semanticGraph(sentence: CoreMap): SemanticGraph =
      sentence get classOf[SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation]

    def depType: String = StanfordCoreNLPAnnotator.collapsedDepType
  }

  class CollapsedCCProcessedDependencies extends StanfordDependencies {
    def setGraph(sentence: CoreMap, graph: SemanticGraph) =
      sentence set (
        classOf[SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation],
        graph)

    def semanticGraph(sentence: CoreMap): SemanticGraph =
      sentence get classOf[SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation]

    def depType: String = StanfordCoreNLPAnnotator.ccCollapsedDepType
  }

  class MentionCandidates extends CoreNLPRequirement {

    def addToCoreMap(annotation: core.Annotation, node: Node) =
      throw new ArgumentError(
        "Annotators which rely on mention (maybe coref only?) should be called along with it, e.g., corenlp[mention,coref], not separately.")

    // TODO: Assuming that mention annotator always is used along with coref.
    // This will immediately cause errors when giving "corenlp[mention],corenlp[coref]".
    // We now do not assume such separation for now.
    def addToNode(document: Node, annotation: core.Annotation): Node = document
  }

  class Coreference extends CoreNLPRequirement {

    def addToCoreMap(annotation: core.Annotation, node: Node) =
      throw new ArgumentError(
        "Annotators which rely on coref should not exist in corenlp?")

    def addToNode(document: Node, annotation: core.Annotation): Node = {

      val sentences = document \\ "sentence"
      val positionToTokenId: Map[(Int, Int), String] =
        sentences.zipWithIndex.flatMap { case (sentence, sentIdx) =>
          val tokens = sentence \\ "token"
          tokens.zipWithIndex.map { case (token, tokenIdx) =>
            (sentIdx, tokenIdx) -> token \@ "id"
          }
        }.toMap

      val corefChains =
        (annotation get classOf[CorefCoreAnnotations.CorefChainAnnotation])
          .asScala
          .map { case (id, corefChain) => corefChain }

      def toMentionNode(mention: CorefChain.CorefMention): Node = {
        val sentIdx = mention.sentNum - 1
        val head = mention.headIndex - 1
        val begin = mention.startIndex - 1
        val end = mention.endIndex - 1

        val tokens = (begin until end) map (positionToTokenId(sentIdx, _)) mkString " "
        val headToken = positionToTokenId(sentIdx, head)

        <mention id={ Annotation.Mention.nextId } tokens={ tokens } head={ headToken }/>
      }

      // We create a map here for removing duplicate mentions by unique ids
      val mentionMap: SortedMap[Int, CorefChain.CorefMention] = {
        val entries = corefChains
          .flatMap { _.getMentionsInTextualOrder.asScala.toSeq }
          .map { mention =>
          mention.mentionID -> mention
        }.toSeq
        SortedMap(entries:_*)
      }

      val mentionNodeMap: SortedMap[Int, Node] =
        mentionMap.transform { case (k, v) => toMentionNode(v) }

      val mentionSeq = mentionNodeMap.toSeq map (_._2)
      val mentionsNode = <mentions annotators={ name }>{ mentionSeq }</mentions>

      val corefChainSeq = corefChains.map { chain =>
        val mentions = chain.getMentionsInTextualOrder.asScala
        val representative = chain.getRepresentativeMention

        val mentionIds = mentions.map { m =>
          mentionNodeMap(m.mentionID) \@ "id"
        } mkString " "
        val representativeId = mentionNodeMap(representative.mentionID) \@ "id"

        <coreference id={ Annotation.Coreference.nextId } mentions={ mentionIds } representative={ representativeId }/>
      }
      val coreferencesNode =
        <coreferences annotators={ name }>
          { corefChainSeq }
        </coreferences>

      document addOrOverwriteChild Seq(mentionsNode, coreferencesNode)
    }
  }

}

object StanfordCoreNLPAnnotator extends AnnotatorCompanion[StanfordCoreNLPAnnotator] {

  val R = Requirement

  // This is an imaginary requirement for representing requirement satisfied by
  // TokensRegexNERAnnotator. This is required because in the original class
  // "satisfied" field is empty so we cannot know whether any annotation is performed.
  val REGEX_REQUIREMENT = new core.Annotator.Requirement("JIGG_REGEX_REQUIREMENT")

  val requirementMap: Map[core.Annotator.Requirement, Seq[Requirement]] = Map(
    core.Annotator.TOKENIZE_REQUIREMENT -> Seq(R.Tokenize),
    // core.Annotator.CLEAN_XML_REQUIREMENT -> // unsupported
    core.Annotator.SSPLIT_REQUIREMENT -> Seq(R.Ssplit),
    core.Annotator.POS_REQUIREMENT -> Seq(R.POS),
    core.Annotator.LEMMA_REQUIREMENT -> Seq(R.Lemma),
    core.Annotator.NER_REQUIREMENT -> Seq(R.StanfordNER),
    REGEX_REQUIREMENT -> Seq(R.StanfordNER),
    // core.Annotator.GENDER_REQUIREMENT -> // unsupported
    // core.Annotator.TRUECASE_REQUIREMENT -> // unsupported
    core.Annotator.PARSE_REQUIREMENT -> Seq(R.Parse),
    core.Annotator.DEPENDENCY_REQUIREMENT ->
      Seq(R.BasicDependencies,
        R.CollapsedDependencies,
        R.CollapsedCCProcessedDependencies),
    core.Annotator.MENTION_REQUIREMENT -> Seq(R.Mention), // unsupported
    // core.Annotator.ENTITY_MENTIONS_REQUIREMENT -> Seq(R.Mention)  // unsupported
    core.Annotator.DETERMINISTIC_COREF_REQUIREMENT -> Seq(R.Coreference), // TODO: maybe we need CoreNLP specific Coreference? for e.g., representing Gender
    core.Annotator.COREF_REQUIREMENT -> Seq(R.Coreference)
  )

  val basicDepType = "basic"
  val collapsedDepType = "collapsed"
  val ccCollapsedDepType = "collapsed-ccprocessed"

  /** name may have the form corenlp[tokenize,ssplit]
    */
  override def fromProps(name: String, props: Properties) = {
    name.indexOf('[') match {
      case -1 => new StanfordCoreNLPAnnotator(
        name,
        props,
        PropertiesUtil.findProperty(s"$name.annotators", props)
          map(_.split("""[,\s]+""").toSeq) getOrElse Seq[String]()) // .getOrElse (Seq.empty[String]))
      case b => new StanfordCoreNLPAnnotator(
        name.substring(0, b),
        props,
        name.substring(b + 1, name.size - 1).split("""[,\s]+"""))
    }
  }

  def parseStr(tokens: NodeSeq, parse: Node): String = {
    val root = parse \@ "root"
    val spans = parse \ "span"
    val spanIds = spans map (_ \@ "id")
    val tokenIds = tokens map (_ \@ "id")

    def findSome(id: String, ids: Seq[String], nodes: Seq[Node]) =
      ids.indexWhere(_ == id) match {
        case -1 => None
        case idx => Some(nodes(idx))
      }
    def findToken(id: String): Option[Node] = findSome(id, tokenIds, tokens)
    def findSpan(id: String): Option[Node] = findSome(id, spanIds, spans)

    def makeStr(node: Node): String = node.label match {
      case "token" =>
        val pos = node \@ "pos"
        val form = node \@ "form"
        s"($pos $form)"
      case "span" =>
        val children = (node \@ "children") split " "

        val childNodes = children map { id =>
          findSpan(id) getOrElse (findToken(id).get)
        }

        "(" + node \@ "symbol" + " " + childNodes.map(makeStr).mkString(" ") + ")"
    }
    "(ROOT " + makeStr(findSpan(root).get) + ")"
  }

  def extractTypedDependencies(
    sentence: Node,
    depType: String,
    docId: String = "",
    sentIdx: Int = 0): Seq[TypedDependency] = {

    val tokens = sentence \\ "token"

    val tokenIdx: String=>Int = tokens.zipWithIndex.map { case (t, i) =>
      ((t \@ "id"), i + 1)
    }.toMap

    val depsNode = (sentence \ "dependencies").find(_ \@ "type" == depType).get

    (depsNode \ "dependency") map { dep =>
      val deprel = dep \@ "deprel"
      val head = dep \@ "head"
      val dependent = dep \@ "dependent"

      def mkIndexedWord(id: String): IndexedWord = {
        val idx = tokenIdx(id)
        val word = new IndexedWord(docId, sentIdx, idx)
        val t = tokens(idx - 1)
        word.setValue(t \@ "form")
        word.set(classOf[CoreAnnotations.PartOfSpeechAnnotation], t \@ "pos")
        word
      }

      head match {
        case "ROOT" =>
          val root = new IndexedWord(docId, sentIdx, 0)
          root.setValue("ROOT")

          val depWord = mkIndexedWord(dependent)
          new TypedDependency(GrammaticalRelation.ROOT, root, depWord)
        case _ =>
          val headWord = mkIndexedWord(head)
          val depWord = mkIndexedWord(dependent)

          val relation = GrammaticalRelation.valueOf(deprel)
          new TypedDependency(relation, headWord, depWord)
      }
    }
  }

  def semanticGraphToDependenciesNode(
    sentence: Node,
    semgraph: SemanticGraph,
    depType: String,
    name: String) = {

    val sentenceId = sentence \@ "id"

    val tokens = (sentence \ "tokens").head
    val tokenSeq = tokens \ "token"

    val rootNodes: NodeSeq = semgraph.getRoots.asScala.toSeq map { root =>
      val relation = GrammaticalRelation.ROOT.getLongName()
      val dep = tokenSeq(root.index - 1) \@ "id"
      val head = "ROOT"
      <dependency id={Annotation.Dependency.nextId}
      head={ head } dependent={ dep } deprel={ relation }/>
    }
    val depNodes: NodeSeq = semgraph.edgeIterable.asScala.toSeq map { edge =>
      val relation = edge.getRelation.getShortName

      val head = tokenSeq(edge.getGovernor.index - 1) \@ "id"
      val dep = tokenSeq(edge.getDependent.index - 1) \@ "id"

      <dependency id={Annotation.Dependency.nextId}
      head={ head } dependent={ dep } deprel={ relation }/>
    }
    <dependencies type={ depType }
      annotators={ name }>{ rootNodes ++ depNodes }</dependencies>
  }
}
