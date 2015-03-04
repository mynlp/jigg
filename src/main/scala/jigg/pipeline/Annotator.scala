package jigg.pipeline

import scala.xml.{Node, Elem}
import jigg.util.XMLUtil

trait Annotator {
  def name: String = this.getClass.toString
  def annotate(annotation: Node): Node

  def close = {} // Resource release etc; detault: do nothing

  import Annotator.Requirement
  def requires = Set.empty[Requirement]
  def requirementsSatisfied = Set.empty[Requirement]
}

// abstract class Requirement

// object Requirement {
//   case object JaSentence extends Requirement
//   case object JaTokenize extends Requirement
//   case object JaChunk extends Requirement
//   case object JaDependency extends Requirement
//   case object JaCCG extends Requirement
// }

object Annotator {
  /** Requirement is a constant (used as an enum) used to describe dependencies between
    * annotators. The design is inspired by the Stanford CoreNLP.
    */
  trait Requirement

  case object JaSentence extends Requirement
  case object JaTokenize extends Requirement
  case object JaChunk extends Requirement
  case object JaDependency extends Requirement
  case object JaCCG extends Requirement
}

/** A trait for an annotator which modifies a sentence node. If an annotator is sentence-level
  * annotator such as a parser or pos tagger, it should extend this trait and usually what you
  * should do is only to implement newSentenceAnnotation method, which rewrites a sentence
  * node and returns new one.
  */
trait SentencesAnnotator extends Annotator {
  override def annotate(annotation: Node): Node = {

    XMLUtil.replaceAll(annotation, "sentences") { e =>
      // TODO: sentence level parallelization should be handled here?
      val newChild = e.child map { c =>
        assert(c.label == "sentence") // assuming sentences node has only sentence nodes as children
        val a = newSentenceAnnotation(c)
        a
      }
      e.copy(child = newChild)
    }
  }

  def newSentenceAnnotation(sentence: Node): Node
}
