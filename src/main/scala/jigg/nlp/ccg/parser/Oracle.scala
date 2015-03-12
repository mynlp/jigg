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

import jigg.nlp.ccg.lexicon.{PoS, Word, Category, TrainSentence, Derivation, BinaryChildrenPoints, UnaryChildPoint}
import jigg.nlp.ccg.lexicon.Direction._

/**
 * Define the oracle for transition-based systems.
 * Terms in this file are borrowed from a paper,
 *   Training Deterministic Parsers with Non-Deterministic Oracles (Goldberg and Nivre, 2013),
 * which discuss the design of oracles, especially dynamic oracle to alleviate error propagations of the deterministic (non-beam) transition-systems.
 * Dynamic oracle is originally introduced in the arc-eager system of dependency parsing.
 *
 * The trait Oracle defined below abstract away the mechanism of concrete oracle. However, crrently it is unknown whether dynamic oracle can be applied to our consituency parsing, so StaticArcStandardOracle, which returns only one action that leads to the gold parse, is the valid one.
 * For future, we may construct an oracle which deals with spurious ambiguity of CCG, so the method goldActions returns the set of valid actions, but current version (Static...) alwasy returns the set of only one element.
 */
trait Oracle {
  def goldActions(state:State):Seq[Action]
}
class StaticArcStandardOracle(val sentence:TrainSentence, val gold:Derivation, val rule:Rule) extends Oracle {
  override def goldActions(state:State) = {
    def isFinished = state.j == sentence.size && onlyContainRootNode
    def onlyContainRootNode = (state.s1, state.s0) match {
      case (None, Some(s0)) => s0.toDerivationPoint == gold.root
      case _ => false
    }
    def combineAction: Option[Action] = for {
      s1 <- state.s1; s0 <- state.s0
      (goldParent, ruleSymbol) <- gold.parentCategory(BinaryChildrenPoints(s1.toDerivationPoint, s0.toDerivationPoint))
    } yield {
      val leftNodeInfo = HeadFinder.NodeInfo(
        sentence.pos(s1.head), s1.category, s1.headCategory)
      val rightNodeInfo = HeadFinder.NodeInfo(
        sentence.pos(s0.head), s0.category, s0.headCategory)
      Combine(goldParent, rule.headFinder.get(leftNodeInfo, rightNodeInfo), ruleSymbol)
    }
    def unaryAction: Option[Action] = for {
      s0 <- state.s0
      (goldParent, ruleSymbol) <- gold.parentCategory(UnaryChildPoint(s0.toDerivationPoint))
    } yield { Unary(goldParent, ruleSymbol) }

    require (state.isGold)
    val retAction = if (isFinished) Finish() else combineAction match {
      case Some(action) => action
      case None => unaryAction match {
        case Some(action) => action
        case None if state.j < sentence.size => Shift(sentence.cat(state.j))
        case _ => sys.error("ERROR: could not find any gold actions")
      }
    }
    // println(state.print)
    // println(retAction)
    List(retAction) // only one element is valid
  }
}

// class NonDeterministicArcStandardOracle(val sentence:TrainSentence, val gold:Derivation) extends Oracle {

// }

trait OracleGenerator {
  type O <: Oracle
  def gen(sentence:TrainSentence, gold:Derivation, rule:Rule):O
}
object StaticOracleGenerator extends OracleGenerator {
  type O = StaticArcStandardOracle
  override def gen(sentence:TrainSentence, gold:Derivation, rule:Rule) = new StaticArcStandardOracle(sentence, gold, rule)
}
// object NonDeterministicOracleGenerator extends OracleGenerator {
//   override def gen(sentence:TrainSentence, gold:Derivation, rule:Rule) =
//     new NonDeterministicArcStandardOracle(sentence, gold, rule)
// }
