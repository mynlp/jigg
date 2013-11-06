package enju.ccg.parser

import enju.ccg.lexicon.{PoS, Word, Category, TaggedSentence, TrainSentence, TestSentence, Derivation}
import enju.ccg.ml.{FeatureBase, Perceptron}

import scala.collection.mutable.{ArrayBuffer, HashMap}

class FeatureIndexer extends HashMap[LF, Int] {
  def getIndex(key:LF) = getOrElse(key, size)
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
        val sumScore = path.score + classifier.calcScore(featureIdxs)
        Candidate(path, WrappedAction(action, isGold, featureIdxs), sumScore)
      }
    }
    def collectCandidatesTest(sentence:TestSentence) = kbest.flatMap { path =>
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, path.state)
      possibleActions(path.state, sentence).map { action =>
        val featureIdxs = unlabeledFeatures.map { _.assignLabel(action.toLabel) }.map { indexer.getOrElse(_, -1) }.toArray
        val sumScore = path.score + classifier.calcScore(featureIdxs)
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
      sentences.zip(golds).zipWithIndex.foreach {
        case ((sentence, derivation), numProcessed) => trainSentence(sentence, derivation)
      }
    }
  }
  def trainSentence(sentence: TrainSentence, gold:Derivation): Unit = trainInstance(getTrainingInstance(sentence, gold))

  def trainInstance(instance:TrainingInstance): Unit = instance match {
    case TrainingInstance(Some(pred), Some(gold)) => classifier.update(pred.fullFeatures, gold.fullFeatures)
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
        val updatedOutputPath:Option[StatePath] = finished.sortWith(_.score > _.score) match {
          case top :: _ if (top.score > pathScore(currentOutputPath)) => Some(top.path)
          case _ => currentOutputPath
        }
        val updatedGoldPath:Option[StatePath] = finished.find(_.wrappedAction.isGold) map { _.path } // the most high scored path is regarded as gold (NOTE: current oracle find only one gold; so this process is redundant)

        val sortedUnfinished = unfinished.sortWith(_.score > _.score)
        val newBeam = oldBeam.resetQuick(sortedUnfinished)
        
        // early-update check; when goldPath has value, we wait for exhausting the beam
        if (!newBeam.existsGold && updatedGoldPath == None) {
          val returnOutputPath = (updatedOutputPath, newBeam.kbest(0)) match {
            case (None, best) => Some(best)
            case (currentOutput, best) if (best.score > pathScore(currentOutput)) => Some(best)
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

    // var beam = Beam.init(initialState)
    // var outputPath:Option[StatePath],
    // var goldPath:Option[StatePath]
    // while (!beam.isEmpty) {
    //   val candidates:List[Candidate] = beam.collectCandidatesTrain(sentence, oracle)
    //   val (finished, unfinished) = candidates.partition {
    //     case Candidate(_, WrappedAction(Finish(), _, _), _) => true
    //     case _ => false
    //   }
    //   finished.sortWith(_.score > _.score) match {
    //     case top :: _ =>
    //       if (top.score > currentOutputScore) outputPath = Some(top.path)
    //     case Nil => // can be safely ignored
    //   }
    //   goldPath = finished.find(_.wrappedAction.isGold) map { _.path } // the most high scored path is regarded as gold (NOTE: current oracle find only one gold; so this process is redundant)

    //   val sortedUnfinished = unfinished.sortWith(_.score > _.score)
    //   beam = beam.resetQuick(sortedUnfinished)

    //   if (!beam.existsGold && goldPath == None) { // early-update check; when goldPath has value, we wait for exhausting the beam
    //     if (outputPath == None || (outputPath != None && beam.kbest(0).score > currentOutputScore)) {
    //       outputPath = Some(beam.kbest(0))
    //     }
    //     if (goldPath == None) {
    //       goldPath = unfinished.find(_.wrappedAction.isGold) map { _.path }
    //     }
    //     if (goldPath == None) {
    //       println("BeamSearchDecoder.scala: cannot find gold tree; this may or may not be correct.")
    //     }
    //     return TrainingInstance(outputPath, goldPath)
    //   }
    // }
    // TrainingInstance(outputPath, goldPath)
  }
}
