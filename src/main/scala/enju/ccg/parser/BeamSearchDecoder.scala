package enju.ccg.parser

import enju.ccg.lexicon.{PoS, Word, Category, TaggedSentence, TrainSentence, TestSentence, Derivation, CandAssignedSentence}
import enju.ccg.ml.Perceptron
import scala.collection.mutable.{ArrayBuffer, HashMap}

class FeatureIndexer extends HashMap[LF, Int] {
  def getIndex(key:LF) = getOrElseUpdate(key, size)
}

class BeamSearchDecoder(val indexer:FeatureIndexer,
                        val extractors:FeatureExtractors,
                        val classifier:Perceptron[ActionLabel],
                        val oracleGen:OracleGenerator,
                        override val rule:Rule,
                        val beamSize:Int,
                        val initialState:State) extends TransitionBasedParser {

  case class Candidate(path:StatePath, wrappedAction:WrappedAction, score:Double)

  case class Beam(kbest:List[StatePath]) {
    def isEmpty:Boolean = kbest.isEmpty

    def reset(candidates:List[Candidate]) = resetQuick(candidates.sortWith(_.score > _.score))
    def resetQuick(sortedCandidates:List[Candidate]) = {
      val newKBest = sortedCandidates.take(beamSize).map {
        case Candidate(path, wrappedAction, score) =>
          val newState = path.state.proceed(wrappedAction.v, wrappedAction.isGold)
          StatePath(newState, wrappedAction :: path.actionPath, score)
      }
      Beam(newKBest)
    }
    def existsGold = kbest.exists(_.state.isGold)

    def collectCandidatesTrain(sentence:TrainSentence, oracle:Oracle) = kbest.flatMap { path =>
      // currently (in deterministic-oracle), oracle actions are only defined to the gold state
      val goldActions:Seq[Action] = if (path.state.isGold) oracle.goldActions(path.state) else Nil
      // partial features (without label)
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, path.state)

      possibleActions(path.state, sentence).map { action =>
        val isGold = goldActions.contains(action) // support non-deterministic oracle; currently, goldActions only contain one element so this operation is simple equality check
        val featureIdxs = unlabeledFeatures.map { _.assignLabel(action.toLabel) }.map { indexer.getIndex(_) }.toArray
        val sumScore = path.score + classifier.featureScore(featureIdxs)
        Candidate(path, WrappedAction(action, isGold, featureIdxs), sumScore)
      }
    }
    def collectCandidatesTest(sentence:CandAssignedSentence) = kbest.flatMap { path =>
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, path.state)
      possibleActions(path.state, sentence).map { action =>
        val featureIdxs = unlabeledFeatures.map { _.assignLabel(action.toLabel) }.map { indexer.getOrElse(_, -1) }.toArray
        val sumScore = path.score + classifier.featureScore(featureIdxs)
        Candidate(path, WrappedAction(action, false), sumScore) // do not preserve (partial) features at test time
      }
    }
  }
  object Beam {
    def init(initState:State): Beam = new Beam(StatePath(initState, Nil) :: Nil)
  }
  case class TrainingInstance(predictedPath:Option[StatePath], goldPath:Option[StatePath])

  def trainSentences(sentences: Array[TrainSentence], golds:Array[Derivation], numIters:Int):Unit = {
    (0 until numIters).foreach { i =>
      var correct = 0
      sentences.zip(golds).zipWithIndex.foreach {
        case ((sentence, derivation), numProcessed) =>
          if (trainSentence(sentence, derivation)) correct += 1
      }
      println("accuracy (" + i + "): " + correct.toDouble / sentences.size.toDouble + " [" + correct + "]")
      println("# features: " + indexer.size)
    }
  }
  def trainSentence(sentence: TrainSentence, gold:Derivation): Boolean =
    getTrainingInstance(sentence, gold) match {
      case TrainingInstance(Some(pred), Some(gold)) =>
        classifier.update(pred.fullFeatures, gold.fullFeatures)
        return pred.state.isGold
      case _ => sys.error("")
    }
  // TODO: add test in sample sentence
  def getTrainingInstance(sentence:TrainSentence, gold:Derivation): TrainingInstance = {
    val oracle = oracleGen.gen(sentence, gold, rule)

    def findOutputAndGold(oldBeam: Beam,
                          currentOutputPath:Option[StatePath],
                          currentGoldPath:Option[StatePath]): TrainingInstance = {
      def pathScore(p:Option[StatePath]) = p map { _.score } getOrElse(Double.NegativeInfinity)
      if (oldBeam.isEmpty) TrainingInstance(currentOutputPath, currentGoldPath)
      else {
        val candidates:List[Candidate] = oldBeam.collectCandidatesTrain(sentence, oracle)

        val (finished, unfinished) = candidates.partition {
          case Candidate(_, WrappedAction(Finish(), _, _), _) => true
          case _ => false
        }
        val sortedFinished = finished.sortWith(_.score > _.score)
        val updatedOutputPath:Option[StatePath] = sortedFinished match {
          case top :: _ if (top.score > pathScore(currentOutputPath)) => Some(top.path)
          case _ => currentOutputPath
        }
        val updatedGoldPath:Option[StatePath] = sortedFinished.find(_.wrappedAction.isGold) match {
          case Some(topGold) if topGold.score > pathScore(currentGoldPath) => Some(topGold.path)
          case _ => currentGoldPath
        } // the most high scored path is regarded as gold (NOTE: current oracle find only one gold; so this process is redundant)

        val sortedUnfinished = unfinished.sortWith(_.score > _.score)
        val newBeam = oldBeam.resetQuick(sortedUnfinished)

        // early-update check; when goldPath has value, we wait for exhausting the beam
        if (!newBeam.existsGold && updatedGoldPath == None) {
          val returnOutputPath = (updatedOutputPath, newBeam.kbest) match {
            case (None, best :: _) => Some(best)
            case (currentOutput, best :: _) if (best.score > pathScore(currentOutput)) => Some(best)
            case _ => updatedOutputPath
          }
          val returnGoldPath = unfinished.find(_.wrappedAction.isGold) map { _.path }
          if (returnGoldPath == None) {
            println("BeamSearchDecoder.scala: cannot find gold tree; this may or may not be correct?")
          }
          TrainingInstance(returnOutputPath, returnGoldPath)
        } else findOutputAndGold(newBeam, updatedOutputPath, updatedGoldPath)
      }
    }
    findOutputAndGold(Beam.init(initialState), None, None)
  }
  def predict(sentence: CandAssignedSentence): Derivation = {
    def beamSearch(oldBeam:Beam, finishedCandidates: List[Candidate]): List[Candidate] = {
      if (oldBeam.isEmpty) return finishedCandidates
      else {
        val candidates:List[Candidate] = oldBeam.collectCandidatesTest(sentence)
        val (finished, unfinished) = candidates.partition {
          case Candidate(_, WrappedAction(Finish(), _, _), _) => true
          case _ => false
        }
        beamSearch(oldBeam.reset(unfinished), finished ::: finishedCandidates)
      }
    }
    val allCandidates = beamSearch(Beam.init(initialState), Nil)
    allCandidates.sortWith(_.score > _.score)(0).path.state.toDerivation
  }
}
