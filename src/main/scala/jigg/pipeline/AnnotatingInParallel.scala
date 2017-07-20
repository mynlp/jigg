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
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Success, Failure}
import jigg.util.XMLUtil.RichNode

/** This trait provides an easy way to make any annotators that are not thread-safe
  * to work in parallel.
  *
  * The internal mechanism to do this is that this annotator wraps a set of
  * "localAnnotators", each of which may be, say, SentencesAnnotator, which does
  * actual annotation. After collecting the set of annotating elemenets, which are
  * a sequence of `<sentence .../>` when annotation is sentence-level, we first
  * divide this sequence and distribute them on the local annotators, which can
  * then perform annotation independently with each other.
  *
  * In most cases this trait would not be directly used. Use a subclass
  * [[jigg.pipeline.AnnotatingSentencesInParallel]] for sentence-level annotators
  * or [[jigg.pipeline.AnnotatingDocumentsinparallel]] for document-level annotators.
  *
  * For example, BerkeleyParserAnnotator implements `AnnotatingSentencesInParallel`.
  * Each localAnnotator keeps a specific parser object, which is not interfered with
  * other local annotators.
  */
trait AnnotatingInParallel extends Annotator { self=>

  type A <: BaseLocalAnnotator

  trait BaseLocalAnnotator extends Annotator {
    override final def name = self.name
    override final def nThreads = 1
  }

  // We don't create local annotator here, since each local annotator may depend on
  // some variables that are not yet instantitated; e.g., `command` in
  // `LocalMecabAnnotator`.
  lazy protected val localAnnotators: Seq[A] =
    (0 until nThreads).par.map(_=>mkLocalAnnotator()).seq

  protected def mkLocalAnnotator(): A

  /** `annotateSeq` specifies how to annotate each local segments of nodes by a local
    * annotator.
    */
  def annotateInParallel(
    elems: Seq[Node],
    annotateSeq: (Seq[Node], A)=>Seq[Node]): Seq[Node] = {

    val dividedElems = divideBy(elems, nThreads)
    assert(dividedElems.size <= nThreads)

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

  private def divideBy(elems: Seq[Node], n: Int): Seq[Seq[Node]] = {
    val divided: Seq[Seq[Node]] =
      if (elems.size < n) Array(elems) //+: Array.fill(n-1)(Seq[Node]())
      else elems.grouped(elems.size / n).toIndexedSeq
    // assert(divided.size == n || divided.size == n + 1)
    assert(divided.size <= n + 1)
    if (divided.size == n + 1) divided.take(n - 1) :+ (divided(n - 1) ++ divided(n))
    else divided
  }

  // protected def annotateSeq(annotations: Seq[Node], annotator: A): Seq[Node]

  // TODO
  private def annotateErrorToBatch(original: Seq[Node], error: Throwable): Seq[Node] = {
    throw error
    // original
  }
}

trait AnnotatingSentencesInParallel extends AnnotatingInParallel {

  type A = LocalAnnotator

  // trait LocalAnnotator extends SentencesAnnotator with BaseLocalAnnotator
  // It seems LocalAnnotator does not have to extend SentencesAnnotator explicitly
  // (which loses some flexibility).
  trait LocalAnnotator extends BaseLocalAnnotator

  /** This is more complex than `annotate` in `AnnotatingDocumentsInParallel`
    */
  override def annotate(annotation: Node): Node = {
    // First gather all <sentence> elements.
    val sentencesSeq = new ArrayBuffer[Node]
    annotation.traverse("sentences") { sentencesSeq += _ }

    val sentenceSeqs = sentencesSeq map (_ \ "sentence")
    val sentenceSeq: Seq[Node] = sentenceSeqs.flatten

    val offsets = sentenceSeqs.map(_.size).scanLeft(0)(_+_)

    // Perform annotation for all gathered sentences at once
    val newSentenceSeq = annotateInParallel(sentenceSeq, annotateSeq)

    // Update annotation with `replaceAll`, which guarantees the node traverse order
    // is consistent with `traverse` (used to obtain `sentencesSeq` above).
    var i = 0
    annotation.replaceAll("sentences") { case e: Elem =>
      val begin = offsets(i)
      val end = offsets(i + 1)
      i += 1
      val newSentenceSeqInDoc = (begin until end) map (newSentenceSeq)
      e.copy(child = newSentenceSeqInDoc)
    }
  }

  val annotateSeq = (annotations: Seq[Node], annotator: A) => {
    (annotator annotate <sentences>{ annotations }</sentences>).child
  }
}

trait AnnotatingDocumentsInParallel extends AnnotatingInParallel {

  type A = LocalAnnotator
  trait LocalAnnotator extends DocumentAnnotator with BaseLocalAnnotator

  override def annotate(annotation: Node): Node = {
    annotation.replaceAll("root") { case e: Elem =>
      val documents = e \\ "document"
      val annotatedDocuments = annotateInParallel(documents, annotateSeq)
      e.copy(child = annotatedDocuments)
    }
  }

  val annotateSeq = (annotations: Seq[Node], annotator: A) => {
    (annotator annotate <root>{ annotations }</root>).child
  }
}
