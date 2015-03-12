package jigg.nlp.ccg.parser

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

import jigg.nlp.ccg.lexicon._
import jigg.nlp.ccg.lexicon.Direction._

import org.scalatest.FunSuite
import org.scalatest.Matchers._


class KBestDecoderTest extends FunSuite {

  case class StateInfo(nodeLabels: Array[String], score: Double)

  class DecoderForTest(candidates: Seq[StateInfo]) extends KBestDecoder {

    case class Candidate(val path: StatePath, score: Double) extends ACandidate

    def newCandidate(nodeLabels: Array[String], score: Double) = {
      val a = WrappedAction(Finish(), true)

      def node(label: String) = {
        val c = AtomicCategory(0, label, JPCategoryFeature.createFromValues(Seq()))
        val wrapped = WrappedCategory(c, "", 0, c, Direction.Left, 0, 1)
        StackedNode(wrapped, None, None)
      }
      val state = FullState(nodeLabels map { node(_) }, 1, true)
      val path = StatePath(state, a)

      Candidate(path, score)
    }

    override def search(sentence: CandAssignedSentence) = candidates map { case StateInfo(labels, score) => newCandidate(labels, score) }
  }

  val emptySentence = TestSentence(Seq(), Seq(), Seq(), Seq())

  test("predictConnected prefers connected tree") {

    val candidates = Seq(
      StateInfo(Array("a", "b"), 10), // these are unconnected tree
      StateInfo(Array("c", "d"), 5),
      StateInfo(Array("e", "f"), 15),
      StateInfo(Array("g"), 15)) // this is connected tree

    val decoder = new DecoderForTest(candidates)

    val (deriv, score) = decoder.predictConnected(emptySentence)
    deriv.roots.size should be (1)
    deriv.roots(0).category.toString should be ("g")
  }

  test("predictKbest with preferConnected prefers connected trees") {

    val candidates = Seq(
      StateInfo(Array("a"), 10), // this is connected tree
      StateInfo(Array("b", "c"), 10), // these are unconnected tree
      StateInfo(Array("d"), 15),
      StateInfo(Array("e", "f"), 15),
      StateInfo(Array("g", "h"), 20))

    val decoder = new DecoderForTest(candidates)

    val derivations = decoder.predictKbest(3, emptySentence, true)

    derivations.size should be (3)

    derivations(0) match {
      case (deriv, score) =>
        deriv.roots.map { _.category.toString } should be (Array("d"))
        score should be (15)
    }

    derivations(1) match {
      case (deriv, score) =>
        deriv.roots.map { _.category.toString } should be (Array("a"))
        score should be (10)
    }

    derivations(2) match {
      case (deriv, score) =>
        deriv.roots.map { _.category.toString } should be (Array("g", "h"))
        score should be (20)
    }
  }
}
