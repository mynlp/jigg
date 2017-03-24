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

/** `A` is class of localAnnotator (maybe SentencesAnnotator or DocumentAnnotator), which manages
  * annotators communicating with an external software.
  */
trait ExternalProcessParallelAnnotator[A<:Annotator]
    extends AnnotatingInParallel[A] { self=>

  // each local annotator runs in serial
  trait BaseLocalAnnotator extends Annotator {
    override final def name = self.name
    override final def nThreads = 1
  }

  def mkLocalAnnotator(): A with BaseLocalAnnotator
  override def close() = for (a <- localAnnotators) a.close()

  // We don't create local annotator here, since each local annotator may depend on
  // some variables that are not yet instantitated; e.g., `command` in
  // `LocalMecabAnnotator`.
  lazy val localAnnotators: Seq[A] =
    (0 until nThreads).map(_=>mkLocalAnnotator())
}

trait ExternalProcessSentencesAnnotator
    extends ExternalProcessParallelAnnotator[SentencesAnnotator] with SentenceLevelParallelism { self=>

  trait LocalAnnotator extends SentencesAnnotator with BaseLocalAnnotator

  def annotateSeq(annotations: Seq[Node], ann: SentencesAnnotator) =
    annotations map ann.newSentenceAnnotation
}

trait ExternalProcessDocumentAnnotator
    extends ExternalProcessParallelAnnotator[DocumentAnnotator] with DocumentLevelParallelism { self=>

  trait LocalAnnotator extends DocumentAnnotator with BaseLocalAnnotator

  def annotateSeq(annotations: Seq[Node], ann: DocumentAnnotator) =
    annotations map ann.newDocumentAnnotation
}
