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

import jigg.nlp.ccg.lexicon.{Derivation, CandAssignedSentence}

case class WrappedAction(v: Action, isGold:Boolean, partialFeatures:LabeledFeatures = LabeledFeatures())

case class StatePath(state:State, waction: WrappedAction, prev: Option[StatePath] = None, score:Double = 0) {
  def actionPath = expand.map(_.waction)
  def expand = expandRecur(Nil)
  private def expandRecur(seq: List[StatePath]): List[StatePath] = prev match {
    case None => seq // always ignoring the initial state
    case Some(prev) => prev.expandRecur(this :: seq)
  }
  def lighten = this.copy(waction = waction.copy(partialFeatures = LabeledFeatures()))
}

trait KBestDecoder {

  trait ACandidate {
    def path: StatePath
    def score: Double
    def isConnected: Boolean = path.state.s1 == None
  }

  val comparePreferringConnected: (ACandidate, ACandidate) => Boolean = {
    case (a, b) if a.isConnected && !b.isConnected => true
    case (a, b) if !a.isConnected && b.isConnected => false
    case (a, b) => a.score > b.score
  }

  def search(sentence: CandAssignedSentence): Seq[ACandidate]

  def predict(sentence: CandAssignedSentence): (Derivation, Double) = {
    val c = search(sentence).sortWith(_.score > _.score)(0)
    (c.path.state.toDerivation, c.score)
  }

  /** If a fully connected tree is found, return the one with the maximum score; else return the maximum score unconnected tree
    */
  def predictConnected(sentence: CandAssignedSentence): (Derivation, Double) = {
    val c = search(sentence).sortWith(comparePreferringConnected)(0)
    (c.path.state.toDerivation, c.score)
  }

  /** Return k-best trees according to the final state score.
    *
    * @param preferConnected if ture, fully connected trees are placed at the top of elements even if it is not the maximum score tree.
    */
  def predictKbest(k: Int, sentence: CandAssignedSentence, preferConnected: Boolean = false): Seq[(Derivation, Double)] = {
    val sorted = preferConnected match {
      case true => search(sentence).sortWith(comparePreferringConnected)
      case false => search(sentence).sortWith(_.score > _.score)
    }
    sorted.take(k) map { c => (c.path.state.toDerivation, c.score) }
  }
}
