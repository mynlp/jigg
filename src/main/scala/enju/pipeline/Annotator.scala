package enju.pipeline

import scala.xml.{Node, Elem}
import scala.xml.transform.{RewriteRule, RuleTransformer}

sealed trait Annotator[Input, Output] {
  def name: String
  def annotate(annotation: Input): Output

  import Annotator.Requirement
  def requires: Set[Requirement]
  def requirementsSatisfied: Set[Requirement]
}

object Annotator {
  /** Requirement is a constant (used as an enum) used to describe dependencies between
    * annotators. The design is inspired by the Stanford CoreNLP.
    */
  trait Requirement

  case object JaTokenize extends Requirement
  case object JaDependency extends Requirement
  case object JaCCG extends Requirement
}

trait StringAnnotator extends Annotator[String, Node]

trait XMLAnnotator extends Annotator[Node, Node]

/** A trait for an annotator which modifies a sentence node. If an annotator is sentence-level
  * annotator such as a parser or pos tagger, it should extend this trait and usually what you
  * should do is only to implement newSentenceAnnotation method, which rewrites a sentence
  * node and returns new one.
  */
trait SentencesAnnotator extends XMLAnnotator {
  override def annotate(annotation: Node): Node = {

    /** The idiom below is from:
      * https://gist.github.com/zentrope/728034
      * http://stackoverflow.com/questions/2199040/scala-xml-building-adding-children-to-existing-nodes
      *
      * TODO: sentence level parallelization should be handled here?
      */
    object replaceSentences extends RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case e: Elem if e.label == "sentences" =>
          val newChild = e.child map { c =>
            assert(c.label == "sentence") // assuming sentences node has only sentence nodes as children
            val a = newSentenceAnnotation(c)
            a
          }
          e.copy(child = newChild)
        case other => other
      }
    }
    new RuleTransformer(replaceSentences).transform(annotation).head
  }

  def newSentenceAnnotation(sentence: Node): Node
}

// class Kuromoji extends StringToXMLAnnotator {
//   override def annotate(rawInput: String): Node = rawInput.split("\n")
// }
