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

import jigg.nlp.ccg.lexicon.{PoS, Word, Category, TaggedSentence, TrainSentence, TestSentence, Derivation, CandAssignedSentence}
import jigg.ml.{Perceptron, FeatureIndexer}
import scala.collection.mutable.{ArrayBuffer, HashMap}

object UpdateMethod extends Enumeration {
  type UpdateMethod = Value
  val early = Value
  val max_vio = Value
}

class BeamSearchDecoder(val indexer:FeatureIndexer[LF],
                        val extractors:FeatureExtractors,
                        val classifier:Perceptron[ActionLabel],
                        val oracleGen:OracleGenerator,
                        override val rule:Rule,
                        val beamSize:Int,
                        val initialState:State) extends TransitionBasedParser with KBestDecoder {
  import UpdateMethod.UpdateMethod

  val updateMethod = UpdateMethod.max_vio

  case class Candidate(path:StatePath, waction:WrappedAction, score:Double) extends ACandidate {
    def isFinished = waction.v == Finish()
    def toProceededPath: StatePath = {
      val newState = path.state.proceed(waction.v, waction.isGold)
      StatePath(newState, waction, Some(path), score)
    }
    def finalize(sentence: TrainSentence): StatePath = {
      require(isFinished)
      val lastAction: WrappedAction = {
        val unlabeldFeatures = extractors.extractFeaturesFromFinishedTree(sentence, path.state)
        val features = LabeledFeatures(unlabeldFeatures.map { _.assignLabel(FinishLabel()) }.toArray)
        WrappedAction(Finish(), waction.isGold, features)
      }
      val lastScore = classifier.featureScore(lastAction.partialFeatures.expand(indexer))

      Candidate(toProceededPath, lastAction, score + lastScore).toProceededPath
    }
  }

  class Beam(val kbest:List[StatePath]) {
    def generate(kbest:List[StatePath]) = new Beam(kbest)

    def bestScore = kbest match { case t :: _ => t.score; case _ => Double.NegativeInfinity }

    def isEmpty:Boolean = kbest.isEmpty
    def reset(candidates:List[Candidate]) = resetQuick(candidates.sortWith(_.score > _.score))
    def resetQuick(sortedCandidates:List[Candidate]) =
      generate(sortedCandidates.take(beamSize).map { _.toProceededPath })

    def existsGold = kbest.exists(_.state.isGold)

    def collectCandidatesTrain(sentence:TrainSentence, oracle:Oracle) = kbest.par.flatMap { path =>
      // currently (in deterministic-oracle), oracle actions are only defined to the gold state
      val goldActions:Seq[Action] = if (path.state.isGold) oracle.goldActions(path.state) else Nil
      // partial features (without label)
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, path.state)

      possibleActions(path.state, sentence).par.map { action =>
        val isGold = goldActions.contains(action) // support non-deterministic oracle; currently, goldActions only contain one element so this operation is simple equality check
        val features = LabeledFeatures(unlabeledFeatures.map { _.assignLabel(action.toLabel) }.toArray)
        (path, action, isGold, features, features.expand(indexer))
      }
    }.toList.map {
      case (path, action, isGold, features, featureIdxs) =>
        val sumScore = path.score + classifier.featureScore(featureIdxs)
        Candidate(path, WrappedAction(action, isGold, features), sumScore)
    }
    def collectCandidatesTest(sentence:CandAssignedSentence) = kbest.flatMap { path =>
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, path.state)
      possibleActions(path.state, sentence).map { action =>

        // We don't need to cache feature values at test time; so we directly calculate featureIdxs from unlabeled features
        // This is heavily called so try a bit of optimization
        val featureIdxs = new Array[Int](unlabeledFeatures.size)
        var i = 0
        val actionLabel = action.toLabel
        while (i < featureIdxs.size) {
          featureIdxs(i) = indexer.get(unlabeledFeatures(i).assignLabel(actionLabel))
          i += 1
        }
        val sumScore = path.score + classifier.featureScore(featureIdxs)
        Candidate(path, WrappedAction(action, false), sumScore) // do not preserve (partial) features at test time
      }
    }
  }
  class GoldBeam(kbest: List[StatePath]) extends Beam(kbest) {
    override def generate(kbest: List[StatePath]) = new GoldBeam(kbest)

    override def collectCandidatesTrain(sentence: TrainSentence, oracle: Oracle) = kbest.flatMap { path =>
      assert(path.state.isGold)
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, path.state)

      oracle.goldActions(path.state).map { action =>
        val features = LabeledFeatures(unlabeledFeatures.map { _.assignLabel(action.toLabel) }.toArray)
        val sumScore = path.score + classifier.featureScore(features.expand(indexer))
        Candidate(path, WrappedAction(action, true, features), sumScore)
      }
    }
  }

  def initialBeam = new Beam(StatePath(initialState, null, None) :: Nil)
  def initialGoldBeam = new GoldBeam(StatePath(initialState, null, None) :: Nil)

  case class TrainingInstance(predPath:Option[StatePath], goldPath:Option[StatePath])

  def trainSentence(sentence: TrainSentence, gold:Derivation): Boolean = {
    def excludeCommonActions(predActions: List[WrappedAction], goldActions: List[WrappedAction]): (List[WrappedAction], List[WrappedAction]) =
      (predActions, goldActions) match {
        case (pred :: predTail, gold :: goldTail) =>
          if (pred.v == gold.v) excludeCommonActions(predTail, goldTail)
          else (predActions, goldActions)
        case (_, _) => (predActions, goldActions)
      }
    def expandFeaturesForTrain(actions: List[WrappedAction]) =
      actions.toArray.flatMap { _.partialFeatures.expandForTrain(indexer) }

    trainingInstance(sentence, gold) match {
      case TrainingInstance(Some(pred), Some(gold)) =>
        if (!pred.state.isGold) {
          excludeCommonActions(pred.actionPath, gold.actionPath) match {
            case (predActions, goldActions) =>
              classifier.update(expandFeaturesForTrain(predActions), expandFeaturesForTrain(goldActions))
          }
        }
        else classifier.c += 1.0F // average parameter must be updated
        return pred.state.isGold
      case _ => sys.error("")
    }
  }

  def trainingInstance(sentence: TrainSentence, gold: Derivation): TrainingInstance = updateMethod match {
    case UpdateMethod.early => findEarlyUpdatePoint(sentence, gold)
    case UpdateMethod.max_vio => findMaxViolationPoint(sentence, gold)
  }

  def pathScore(p:Option[StatePath]) = p map { _.score } getOrElse(Double.NegativeInfinity)

  def findEarlyUpdatePoint(sentence: TrainSentence, gold: Derivation): TrainingInstance = {
    val oracle = oracleGen.gen(sentence, gold, rule)
    def findPredictAndGold(oldBeam: Beam, current: TrainingInstance): TrainingInstance = {
      if (oldBeam.isEmpty) current
      else {
        val candidates = oldBeam.collectCandidatesTrain(sentence, oracle)

        val (finished, unfinished) = candidates.partition { _.isFinished }

        val sortedFinished = finished.map { _.finalize(sentence) }.sortWith(_.score > _.score)
        val updatedPredPath: Option[StatePath] = sortedFinished match {
          case top :: _ if top.score > pathScore(current.predPath) => Some(top)
          case _ => current.predPath
        }
        val updatedGoldPath: Option[StatePath] = sortedFinished.find(_.waction.isGold) match {
          case Some(topGold) if topGold.score > pathScore(current.goldPath) => Some(topGold)
          case _ => current.goldPath
        } // the most high scored path is regarded as gold (NOTE: current oracle find only one gold; so this process is redundant)

        val sortedUnfinished = unfinished.sortWith(_.score > _.score)
        val newBeam = oldBeam.resetQuick(sortedUnfinished)

        // early-update check; when goldPath has value, we wait for exhausting the beam
        if (!newBeam.existsGold && updatedGoldPath == None) {
          val proceededGoldPath = unfinished.find(_.waction.isGold).map { _.toProceededPath }
          val returnOutputPath = (updatedPredPath, newBeam.kbest) match {
            case (None, best :: _) => Some(best)
            case (pred, best :: _) if (best.score > pathScore(pred)) => Some(best)
            case _ => updatedPredPath
          }
          TrainingInstance(returnOutputPath, proceededGoldPath)
        } else findPredictAndGold(newBeam, TrainingInstance(updatedPredPath, updatedGoldPath))
      }
    }
    findPredictAndGold(initialBeam, TrainingInstance(None, None))
  }

  def findMaxViolationPoint(sentence: TrainSentence, gold: Derivation): TrainingInstance = {
    val goldStateSeq = goldMaxScoreStateSeq(sentence, gold)
    val predStateSeq = predMaxScoreStateSeq(sentence, gold)

    val (min, argMin) = goldStateSeq.zip(predStateSeq).map {
      case (g, p) => g.score - p.score
    }.zipWithIndex.reverse.minBy(_._1) // We reverse here for doing an update even in the case where all scores are 0 (initial condition)
    TrainingInstance(Some(predStateSeq(argMin)), Some(goldStateSeq(argMin)))
  }

  private def maxScoreStateSeq(sentence: TrainSentence, gold: Derivation, beam: Beam): Seq[StatePath] = {
    val oracle = oracleGen.gen(sentence, gold, rule)

    def findMaxScoreSeq(oldBeam: Beam, maxScoreSeq: List[StatePath], bestFinished: Option[StatePath]): List[StatePath] =
      if (oldBeam.isEmpty) { assert(maxScoreSeq(0).score == bestFinished.get.score); maxScoreSeq }
      else {
        val candidates = oldBeam.collectCandidatesTrain(sentence, oracle)

        val (finished, unfinished) = candidates.partition { _.isFinished }
        val sortedUnfinished = unfinished.sortWith(_.score > _.score)
        val newBeam = oldBeam.resetQuick(sortedUnfinished)

        val sortedFinished = finished.map { _.finalize(sentence) }.sortWith(_.score > _.score)

        // we record the finished state with max score at each step, as if it were remained at the top of beam.
        val updatedFinished = sortedFinished match {
          case top :: _ if top.score > pathScore(bestFinished) => Some(top)
          case _ => bestFinished
        }
        // This operation ensures that the first element of the returned list is a finished state with maximum score, i.e., it is the same as the result of predictStatePath if initialBeam is the normal one (not gold).
        val best = (newBeam.bestScore, pathScore(updatedFinished)) match {
          case (u, f) if u > f => newBeam.kbest(0)
          case _ => updatedFinished.get
        }
        findMaxScoreSeq(newBeam, best :: maxScoreSeq, updatedFinished)
      }
    findMaxScoreSeq(beam, Nil, None).toIndexedSeq.reverse
  }

  def goldMaxScoreStateSeq(sentence: TrainSentence, gold: Derivation) =
    maxScoreStateSeq(sentence, gold, initialGoldBeam)
  def predMaxScoreStateSeq(sentence: TrainSentence, gold: Derivation) =
    maxScoreStateSeq(sentence, gold, initialBeam)

  /** These two methods return a path to a finished state with maximum score.
    */
  def goldArgMaxStatePath(sentence: TrainSentence, gold: Derivation) =
    goldMaxScoreStateSeq(sentence, gold).last
  def predArgMaxStatePath(sentence: TrainSentence, gold: Derivation) =
    predMaxScoreStateSeq(sentence, gold).last

  def search(sentence: CandAssignedSentence): Seq[Candidate] = {
    def beamSearch(oldBeam:Beam, finishedCandidates: List[Candidate]): List[Candidate] = {
      if (oldBeam.isEmpty) return finishedCandidates
      else {
        val candidates:List[Candidate] = oldBeam.collectCandidatesTest(sentence)
        val (finished, unfinished) = candidates.partition { _.isFinished }
        beamSearch(oldBeam.reset(unfinished), finished ::: finishedCandidates)
      }
    }
    beamSearch(initialBeam, Nil)
  }

  // predict is inhereted from KBestDecoder

  // override def predict(sentence: CandAssignedSentence): Derivation =
  //   search(sentence).sortWith(_.score > _.score)(0).path.state.toDerivation

  // override def predictConnected(sentence: CandAssignedSentence): Derivation =
  //   search(sentence).sortWith(comparePreferringConnected)(0).path.state.toDerivation

  // override def predictKbest(k: Int, sentence: CandAssignedSentence, preferConnected: Boolean): Seq[(Derivation, Double)] = {
  //   val sorted = preferConnected match {
  //     case true => search(sentence).sortWith(comparePreferringConnected)
  //     case false => search(sentence).sortWith(_.score > _.score)
  //   }
  //   sorted.take(k) map { c => (c.path.state.toDerivation, c.score) }
  // }
}
