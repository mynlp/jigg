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

import scala.xml.{Node, Elem}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Success, Failure}
import jigg.util.XMLUtil.RichNode

/** `A` is a class that will do an actual annotation job;
  * maybe a Java parser object that receives one sentence (e.g., in BerkeleyParserAnnotator) or
  * a IO resource in case of external softwares (e.g., MecabAnnotator; see ExternalProcessAnnotator for such cases).
  *
  * How to do the actual job is defined in `annotateSeq`.
  *
  * This trait is assumed to be mix-ed in with [[SentenceLevelParallelism]] or [[DocumentLevelParallelism]],
  * which supplies `annotate` method that internally call `a`.
  */
trait AnnotatingInParallel[A] extends Annotator {

  def localAnnotators: Seq[A]

  def annotateInParallel(elems: Seq[Node]): Seq[Node] = {
    val dividedElems = divideBy(elems, nThreads)
    assert(dividedElems.size == nThreads)

    implicit val context = scala.concurrent.ExecutionContext.global

    val maybeAnnotatedElems: Seq[Future[Seq[Node]]] =
      dividedElems zip localAnnotators map {
        case (localElems, ann) => Future(annotateSeq(localElems, ann)) recover {
          case e => annotateErrorToBatch(localElems, e)
        }
      }
    for (a <- maybeAnnotatedElems) Await.ready(a, Duration.Inf)
    maybeAnnotatedElems.map(_.value.get.get).flatten
  }

  def divideBy(elems: Seq[Node], n: Int): Seq[Seq[Node]] = {
    val divided: Seq[Seq[Node]] =
      if (elems.size < n) elems +: Array.fill(n-1)(Seq[Node]())
      else elems.grouped(elems.size / n).toIndexedSeq
    assert(divided.size == n || divided.size == n + 1)
    if (divided.size == n + 1) divided.take(n - 1) :+ (divided(n - 1) ++ divided(n))
    else divided
  }

  def annotateSeq(annotations: Seq[Node], annotator: A): Seq[Node]

  // TODO
  private def annotateErrorToBatch(original: Seq[Node], error: Throwable): Seq[Node] = {
    original
  }
}

trait SentenceLevelParallelism extends Annotator {

  def annotateInParallel(elems: Seq[Node]): Seq[Node]

  override def annotate(annotation: Node): Node = {
    annotation.replaceAll("sentences") { e =>
      val sentences = e \\ "sentence"
      val annotatedSentences = annotateInParallel(sentences)
      e.copy(child = annotatedSentences)
    }
  }
}

trait DocumentLevelParallelism extends Annotator {

  def annotateInParallel(elems: Seq[Node]): Seq[Node]

  override def annotate(annotation: Node): Node = {
    annotation.replaceAll("root") { e =>
      val documents = e \\ "document"
      val annotatedDocuments = annotateInParallel(documents)
      e.copy(child = annotatedDocuments)
    }
  }
}
