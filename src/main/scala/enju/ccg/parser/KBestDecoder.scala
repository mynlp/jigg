package enju.ccg.parser

import enju.ccg.lexicon.{Derivation, CandAssignedSentence}

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

  def predict(sentence: CandAssignedSentence): Derivation =
    search(sentence).sortWith(_.score > _.score)(0).path.state.toDerivation

  /** If a fully connected tree is found, return the one with the maximum score; else return the maximum score unconnected tree
    */
  def predictConnected(sentence: CandAssignedSentence): Derivation =
    search(sentence).sortWith(comparePreferringConnected)(0).path.state.toDerivation

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
