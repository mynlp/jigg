package jigg.pipeline

import jigg.util.XMLUtil
import scala.xml.Node

/** A trait for an annotator which modifies a document node. If an annotator is docuement-level
  * annotator such as a coreference resolution, it should extend this trait and usually what you
  * should do is only to implement newDocumentAnnotation method, which rewrites a document
  * node and returns new one.
  */

trait DocumentAnnotator extends Annotator {
  override def annotate(annotation: Node): Node = {

    XMLUtil.replaceAll(annotation, "root") { e =>
      // TODO: document level parallelization should be handled here?
      val newChild = e.child map { c =>
        c match {
          case c if c.label == "document" => newDocumentAnnotation(c)
          case c => c
        }
      }
      e.copy(child = newChild)
    }
  }

  def newDocumentAnnotation(sentence: Node): Node
}
