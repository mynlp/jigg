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

import scala.xml.Node
import jigg.util.XMLUtil.RichNode

/** A trait for an annotator which modifies a sentence node.
  *
  * If an annotator is sentence-level annotator such as a parser or pos tagger, it should
  * extend this trait and usually what you should do is only to implement
  * newSentenceAnnotation method, which rewrites a sentence node and returns new one.
  *
  * This annotates given sentences in parallel. If you want to avoid this perhaps
  * because the annotator is not thread-safe, use [[jigg.pipeline.SeqSentencesannotator]]
  * instead, which does annotates sequentially.
  */
trait SentencesAnnotator extends Annotator {
  def annotate(annotation: Node): Node = {

    annotation.replaceAll("sentences") { e =>
      val annotatedChild = Annotator.makePar(e.child, nThreads).map {
        case s if s.label == "sentence" =>
          try newSentenceAnnotation(s) catch {
            case e: AnnotationError =>
              System.err.println(s"Failed to annotate a document by $name.")
              Annotator.annotateError(s, name, e)
          }
        case s => s
      }.seq
      e.copy(child = annotatedChild)
    }
  }

  def newSentenceAnnotation(sentence: Node): Node
}

/** This trait annotates the inputs sequentially.
  */
trait SeqSentencesAnnotator extends SentencesAnnotator {
  override def nThreads = 1
}

