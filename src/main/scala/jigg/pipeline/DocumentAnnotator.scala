package jigg.pipeline

/*
 Copyright 2013-2017 Hiroshi Noji

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

import scala.xml.{Elem, Node}
import jigg.util.XMLUtil.RichNode

/** A trait for an annotator which modifies a document node. Use this trait if an annotator
  * is a document-level annotator.
  */
trait DocumentAnnotator extends Annotator {
  override def annotate(annotation: Node): Node = {

    annotation.replaceAll("root") { case e: Elem =>
      val newChild = Annotator.makePar(e.child, nThreads).map { c =>
        c match {
          case c if c.label == "document" =>
            try newDocumentAnnotation(c) catch {
              case e: AnnotationError =>
                System.err.println(s"Failed to annotate a document by $name.")
                Annotator.annotateError(c, name, e)
            }
          case c => c
        }
      }.seq
      e.copy(child = newChild)
    }
  }

  def newDocumentAnnotation(sentence: Node): Node
}

trait SeqDocumentAnnotator extends DocumentAnnotator {
  override def nThreads = 1
}
