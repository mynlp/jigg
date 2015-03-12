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

import jigg.nlp.ccg.lexicon.{PoS, Word, Category, CandAssignedSentence, AppliedRule, TrainSentence, Derivation}
import jigg.ml.{Perceptron, FeatureIndexer, Example}
import scala.collection.mutable.ArrayBuffer

case class LabeledFeatures(features: Array[LF] = Array.empty[LF]) {
  def expand(indexer: FeatureIndexer[LF]) = features.map { indexer.get(_) }
  def expandForTrain(indexer: FeatureIndexer[LF]) = features.map { indexer.getIndex(_) }
}

trait TransitionBasedParser {
  def rule:Rule

  def possibleActions(state:State, sentence:CandAssignedSentence):List[Action] = {
    def possibleCombine:List[Action] = {
      val actions:Option[List[Action]] = for {
        s1 <- state.s1; s0 <- state.s0
        (combinedCategories) <- rule.unify(s1.category, s0.category)
      } yield combinedCategories.map {
        case (category, ruleType) =>
          val leftNodeInfo = HeadFinder.NodeInfo(
            sentence.pos(s1.head), s1.category, s1.headCategory)
          val rightNodeInfo = HeadFinder.NodeInfo(
            sentence.pos(s0.head), s0.category, s0.headCategory)
          Combine(category, rule.headFinder.get(leftNodeInfo, rightNodeInfo), ruleType)
      }.toList
      actions getOrElse Nil
    }
    def possibleUnary:List[Action] = {
      val actions:Option[List[Action]] = for {
        s0 <- state.s0
        parentCategories <- rule.raise(s0.category)
      } yield parentCategories.map {
        case (category, ruleType) => Unary(category, ruleType)
      }.toList
      actions getOrElse Nil
    }
    def possibleShifts:List[Action] = if (state.j < sentence.size) sentence.cand(state.j).map { Shift(_) }.toList else Nil
    def addFinishIfNecessary(actions:List[Action]) =
      // TODO: this procedure might be unappropriate
      if (actions == Nil) Finish() :: Nil else actions

      // this latter adds Finish action regardness of whether there are other actions
      // if (state.j >= sentence.size) Finish() :: actions else actions
    val actions = possibleCombine ::: possibleUnary ::: possibleShifts
    addFinishIfNecessary(actions)
  }

  def trainSentences(sentences: Array[TrainSentence], golds:Array[Derivation]):Int = {
    var correct = 0
    sentences.zip(golds).zipWithIndex.foreach {
      case ((sentence, derivation), numProcessed) =>
        if (trainSentence(sentence, derivation)) correct += 1
    }
    correct
  }
  def trainSentence(sentence: TrainSentence, gold:Derivation): Boolean
  def predict(sentence: CandAssignedSentence): (Derivation, Double)
  // def kbest(sentence: CandAssignedSentence, preferConnected: Boolean = false): Seq[Derivation] =
  //   Seq(predict(sentence, preferConnected)) // default: 1-best
}

class DeterministicDecoder(
  val indexer:FeatureIndexer[LF],
  val extractors:FeatureExtractors,
  val classifier:Perceptron[ActionLabel],
  val oracleGen:OracleGenerator,
  override val rule:Rule,
  val initialState:State) extends TransitionBasedParser {

  def trainSentence(sentence: TrainSentence, gold:Derivation): Boolean = {
    val oracle = oracleGen.gen(sentence, gold, rule)

    val stateOracleSeq: Seq[(State, Action)] = {
      val stateSeq = ArrayBuffer(initialState)
      val oracleSeq = new ArrayBuffer[Action]

      while (oracleSeq.isEmpty || oracleSeq.last != Finish()) {
        val goldAction = oracle.goldActions(stateSeq.last)(0)
        oracleSeq += goldAction
        if (goldAction != Finish())
          stateSeq += stateSeq.last.proceed(goldAction, true)
      }
      stateSeq zip oracleSeq
    }

    var allCorrect = true
    stateOracleSeq foreach { case (state, goldAction) =>
      val goldLabel = goldAction.toLabel
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, state)
      val examples = possibleActions(state, sentence).map { action =>
        val featureIdxs = unlabeledFeatures.map { _.assignLabel(action.toLabel) }.map { indexer.getIndex(_) }.toArray
        Example(featureIdxs, action.toLabel)
      }
      val predict = classifier.predict(examples)._1
      classifier.update(examples, goldLabel)
      classifier.c -= 1.0F
      allCorrect = allCorrect && predict == goldLabel
    }
    classifier.c += 1.0F // only update c when one sentence is processed
    allCorrect
  }
  def predict(sentence: CandAssignedSentence) = sys.error("Please use BeamSearchDecoder with k=1 when predict!")

}
