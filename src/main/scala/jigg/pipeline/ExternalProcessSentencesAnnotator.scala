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

import scala.xml.{Node, Elem}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Success, Failure}
import jigg.util.XMLUtil.RichNode

trait ExternalProcessSentencesAnnotator extends Annotator { self=>

  // each local annotator runs in serial
  trait LocalAnnotator extends SentencesAnnotator {
    override def name = self.name
    override final def nThreads = 1
  }

  def mkLocalAnnotator(): LocalAnnotator

  // We don't create local annotator here, since each local annotator may depend on
  // some variables that are not yet instantitated; e.g., `command` in `LocalMecabAnnotator`.
  lazy val localAnnotators = (0 until nThreads).map(_=>mkLocalAnnotator())

  override def annotate(annotation: Node): Node = {
    val documentSeq = annotation \\ "document"
    val sentencesSeq = documentSeq map { d => (d \ "sentences").head } // seq of "sentences" nodes
    val sentenceSeqs = sentencesSeq map (_ \ "sentence") // seq of "seq of sentences"

    val sentences = sentenceSeqs.flatten
    val offsets = sentenceSeqs.map(_.size).scanLeft(0)(_+_)

    val annotatedSentences = annotateInParallel(sentences)

    val newDocumentSeq: Seq[Node] = (0 until documentSeq.size) map { docidx =>
      val begin = offsets(docidx)
      val end = offsets(docidx + 1)
      val newSentenceSeqInDoc = (begin until end) map (annotatedSentences)
      val newSentences = sentencesSeq(docidx) replaceChild newSentenceSeqInDoc

      documentSeq(docidx) addOrOverwriteChild newSentences
    }
    annotation replaceChild newDocumentSeq
  }

  def annotateInParallel(sentences: Seq[Node]): Seq[Node] = {
    val dividedSentences = divideBy(sentences, nThreads)
    assert(dividedSentences.size == nThreads)
    assert(localAnnotators.size == nThreads)

    implicit val context = scala.concurrent.ExecutionContext.global

    val maybeAnnotatedSentences: Seq[Future[Seq[Node]]] =
      dividedSentences zip localAnnotators map {
        case (sentences, ann) => Future(sentences map ann.newSentenceAnnotation) recover {
          case e => annotateErrorToBatch(sentences, e)
        }
      }
    for (a <- maybeAnnotatedSentences) Await.ready(a, Duration.Inf)
    maybeAnnotatedSentences.map(_.value.get.get).flatten
  }

  def divideBy(sentences: Seq[Node], n: Int): Seq[Seq[Node]] = {
    val divided: Seq[Seq[Node]] =
      if (sentences.size < n) sentences +: Array.fill(n-1)(Seq[Node]())
      else sentences.grouped(sentences.size / n).toIndexedSeq
    assert(divided.size == n || divided.size == n + 1)
    if (divided.size == n + 1) divided.take(n - 1) :+ (divided(n - 1) ++ divided(n))
    else divided
  }

  // TODO
  private def annotateErrorToBatch(original: Seq[Node], error: Throwable): Seq[Node] = {
    original
  }
}
