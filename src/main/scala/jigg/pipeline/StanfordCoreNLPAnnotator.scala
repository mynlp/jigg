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
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
//import scala.collection.mutable.ListBuffer
//import scala.collection.immutable.Lis
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.xml._
import scala.sys.process.Process
import scala.io.Source
import scala.xml.{Node, Elem, Text, Atom}
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.TreeCoreAnnotations
import edu.stanford.nlp.trees.TreePrint
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.trees.GrammaticalRelation

import edu.stanford.nlp.process.CoreLabelTokenFactory

import edu.stanford.nlp.{pipeline => core}

import jigg.util.SecpressionUtil

class StanfordCoreNLPAnnotator(
  override val name: String,
  override val props: Properties,
  val annotatorNames: Seq[String]) extends Annotator {

  val coreNLPProps: Properties = {
    val keys = props.keys.asScala.toSeq.map(_.toString) filter (_ startsWith (name + "."))
    val p = new Properties
    val offset = (name + ".").size
    for (k <- keys) {
      val v = props(k)
      p.put(k.substring(offset), v)
    }
    p.put("annotators", annotatorNames.mkString(","))
    p.put("threads", nThreads + "")
    p
  }

  val coreNLP = new StanfordCoreNLP(coreNLPProps, false)

  val annotators = // annotatorNames map (coreNLP.getPool.get)
    annotatorNames map StanfordCoreNLP.getExistingAnnotator

  val requirementMap: Map[Requirement, CoreNLPRequirement] = Map(
    Requirement.Tokenize -> new Tokenize,
    Requirement.Ssplit -> new Ssplit
  )

  private val requiresByEach: Seq[Set[Requirement]] =
    convRequirements(annotators map (_.requires.asScala.toSet))
  private val requirementsSatisfiedByEach: Seq[Set[Requirement]] =
    convRequirements(annotators map (_.requirementsSatisfied.asScala.toSet))

  private def convRequirements(seq: Seq[Set[core.Annotator.Requirement]]):
      Seq[Set[Requirement]] = {

    def conv(set: Set[core.Annotator.Requirement], name: String): Set[Requirement] =
      set map (StanfordCoreNLPAnnotator.requirementMap.getOrElse(_,
        throw new ArgumentError("$name in Stanford CoreNLP is unsupported in jigg.")))

    assert(seq.size == annotatorNames.size)
    (0 until seq.size) map { i => conv(seq(i), annotatorNames(i)) }
  }

  override val requires = requiresByEach(0) ++
    (1 until requiresByEach.size).foldLeft(Set[Requirement]()) { (newones, i) =>
      val unsatisified =
        requiresByEach(i) --
          (0 until i - 1).map(requiresByEach).foldLeft(Set[Requirement]())(_ ++ _)
      newones ++ unsatisified
    }

  override val requirementsSatisfied =
    requirementsSatisfiedByEach.foldLeft(Set[Requirement]())(_ ++ _)

  override def annotate(root: Node) = {

    XMLUtil.replaceAll(root, "document") { e =>
      val coreAnnotation = new core.Annotation(XMLUtil.text(e))
      for (r <- requires) {
        requirementMap(r).addToCoreMap(coreAnnotation, e)
      }

      coreNLP.annotate(coreAnnotation)

      requirementsSatisfied.foldLeft(e: Node) { (node, r) =>
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

  class Tokenize extends CoreNLPRequirement {

    /** The reason why we prepare this generator is that we may call addToNode
      * several times if an input consists of several documents.
      * (We have to assign unique token ids across documents.)
      */
    private[this] val tokenIDGen = jigg.util.IDGenerator("t")

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

          tf.makeToken(t \@ "surf", begin, end - begin)
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
        id={ tokenIDGen.next }
        surf={ corelabel.word }
        characterOffsetBegin={ begin+"" }
        characterOffsetEnd={ end+"" }/>
      }
      val corelabels =
        annotation.get(classOf[CoreAnnotations.TokensAnnotation]).asScala

      val tokens = <tokens annotators={name}>{ corelabels map toTokenNode }</tokens>

      // this sentence id is dummy (would be changed by followd Ssplit)
      val sentences =
        <sentences><sentence id="s0">{ tokens }</sentence></sentences>

      XMLUtil.addChild(document, Seq(sentences))
    }
  }

  class Ssplit extends CoreNLPRequirement {

    private[this] val sentenceIDGen = jigg.util.IDGenerator("s")

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

        val text = XMLUtil.text(sentence)
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

        sentenceTokens.zipWithIndex foreach { case (token, j) =>
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

      val sentences = document \\ "sentence"
      assert(sentences.size == 1)

      val currentTokens = sentences(0) \\ "token"

      val coreSentences: Seq[CoreMap] =
        annotation.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala

      val sentenceNodes = coreSentences.zipWithIndex map { case (coreSentence, i) =>
        val tokenBegin: Int =
          coreSentence get classOf[CoreAnnotations.TokenBeginAnnotation]
        val tokenEnd: Int = coreSentence get classOf[CoreAnnotations.TokenEndAnnotation]

        val characterOffsetBegin =
          coreSentence get classOf[CoreAnnotations.CharacterOffsetBeginAnnotation]
        val characterOffsetEnd =
          coreSentence get classOf[CoreAnnotations.CharacterOffsetEndAnnotation]

        // val coreTokens =
        //   coreSentence.get(classOf[CoreAnnotations.TokensAnnotation]).asScala

        val tokens = (tokenBegin until tokenEnd) map { case i =>
          // coreTokens.zipWithIndex map { case (coreToken, i) =>
          val currentToken = currentTokens(i)
          val currentOffsetBegin = (currentToken \@ "characterOffsetBegin").toInt
          val currentOffsetEnd = (currentToken \@ "characterOffsetEnd").toInt

          // update offset values
          XMLUtil.addAttributes(
            currentToken,
            Map(
              "characterOffsetBegin" ->
                (currentOffsetBegin - characterOffsetBegin).toString,
              "characterOffsetEnd" ->
                (currentOffsetEnd - characterOffsetBegin).toString
            )
          )
        }
        val text = coreSentence get classOf[CoreAnnotations.TextAnnotation]

        <sentence
          id={ sentenceIDGen.next }
          characterOffsetBegin={ characterOffsetBegin+"" }
          characterOffsetEnd={ characterOffsetEnd+"" }>
          { text }
          <tokens>{ tokens }</tokens>
        </sentence>
      }
      val sentencesNode = <sentences>{ sentenceNodes }</sentences>
      XMLUtil.addChild(document, Seq(sentencesNode))
    }
  }

  object Coref extends CoreNLPRequirement {

    def addToCoreMap(annotation: core.Annotation, node: Node) =
      throw new ArgumentError("Recovering CoreMap of coref is yet unsupported.")

    def addToNode(node: Node, annotation: core.Annotation): Node = {
      null
    }
  }

}

object StanfordCoreNLPAnnotator extends AnnotatorCompanion[StanfordCoreNLPAnnotator] {

  val requirementMap: Map[core.Annotator.Requirement, Requirement] = Map(
    core.Annotator.TOKENIZE_REQUIREMENT -> Requirement.Tokenize,
    // core.Annotator.CLEAN_XML_REQUIREMENT -> Requirement. // unsupported
    core.Annotator.SSPLIT_REQUIREMENT -> Requirement.Ssplit,
    core.Annotator.POS_REQUIREMENT -> Requirement.POS,
    core.Annotator.LEMMA_REQUIREMENT -> Requirement.Lemma,
    core.Annotator.NER_REQUIREMENT -> Requirement.NER,
    // core.Annotator.GENDER_REQUIREMENT -> // unsupported
    // core.Annotator.TRUECASE_REQUIREMENT -> // unsupported
    core.Annotator.PARSE_REQUIREMENT -> Requirement.Parse,
    core.Annotator.DEPENDENCY_REQUIREMENT -> Requirement.Dependencies,
    // core.Annotator.MENTION_REQUIREMENT -> // unsupported
    // core.Annotator.ENTITY_MENTIONS_REQUIREMENT -> // unsupported
    core.Annotator.COREF_REQUIREMENT -> Requirement.Coreference // TODO: maybe we need CoreNLP specific Coreference? for e.g., representing Gender
  )

  /** name may have the form corenlp[tokenize,ssplit]
    */
  override def fromProps(name: String, props: Properties) = {
    name.indexOf('[') match {
      case -1 => new StanfordCoreNLPAnnotator(
        name,
        props,
        PropertiesUtil.safeFind(s"$name.annotators", props).split("""[,\s]+"""))
      case b => new StanfordCoreNLPAnnotator(
        name.substring(0, b),
        props,
        name.substring(b + 1, name.size - 1).split("""[,\s]+"""))
    }
  }
}
