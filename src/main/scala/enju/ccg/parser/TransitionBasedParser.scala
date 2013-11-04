package enju.ccg.parser

import enju.ccg.lexicon.{PoS, Word, Category, CandAssignedSentence}
import scala.collection.mutable.ArrayBuffer

// these are return types of the parser
case class WrappedAction(v:Action, isGold:Boolean, partialFeatures:Array[Int] = Array.empty[Int])
case class StatePath(state:State, actionPath:List[WrappedAction], score:Double = 0) {
  // remove feature information (which might be heavy for later process, e.g., error analysis)
  def toLight:StatePath = {
    val lighterActionPath = actionPath.map { case WrappedAction(v, isGold, _) => WrappedAction(v, isGold) }
    StatePath(state, lighterActionPath, score)
  }
  def fullFeatures:Array[Int] = actionPath.flatMap { _.partialFeatures }.toArray
}

trait TransitionBasedParser {
  def rule:Rule

  def possibleActions(state:State, sentence:CandAssignedSentence):List[Action] = {
    def optActionToList(action:Option[Action]) = action map { _ :: Nil } getOrElse(Nil)
    def possibleCombine:List[Action] = optActionToList(for {
        s1 <- state.s1; s0 <- state.s0
        combinedCategory <- rule.unify(s1.category, s0.category)
      } yield Combine(combinedCategory, rule.headFinder.get(sentence.pos(s1.head), sentence.pos(s0.head))))
    def possibleUnary:List[Action] = optActionToList(
      state.s0 flatMap { s0 => rule.raise(s0.category) } map { Unary(_) })
    def possibleShifts:List[Action] = if (state.j < sentence.size) sentence.cand(state.j).map { Shift(_) }.toList else Nil
    def addFinishIfNecessary(actions:List[Action]) = 
      // TODO: this procedure might be unappropriate
      actions match { case Nil => Finish() :: actions; case _ => actions }
      // this latter adds Finish action regardness of whether there are other other actions
      // if (state.j >= sentence.size) Finish() :: actions else actions
    val actions = possibleCombine ::: possibleUnary ::: possibleShifts
    addFinishIfNecessary(actions)
  }
}

class DeterministicDecoder(val oracleGen:OracleGenerator, override val rule:Rule) extends TransitionBasedParser {
  
}
