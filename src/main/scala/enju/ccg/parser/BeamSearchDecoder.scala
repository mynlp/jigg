package enju.ccg.parser

import enju.ccg.lexicon.{PoS, Word, Category}
import scala.collection.mutable.ArrayBuffer

import enju.ccg.tagger.Feature
trait Features
class NonLabeledFeatures extends ArrayBuffer[Feature] with Features
class LabeledFeatures(baseFeatures:NonLabeledFeatures, action:Action) extends ArrayBuffer[Int] with Features {
  baseFeatures.foreach { f => this += 0 }
}

class FeatureExtractor {
  def get(state:State, sentence:TaggedSentence):NonLabeledFeatures =
    new NonLabeledFeatures
}
class Classifier {
  def score(features:Seq[Int]):Double = 0
}

class BeamSearchDecoder(val extractor:FeatureExtractor,
                        val classifier:Classifier,
                        val oracleGen:OracleGenerator,
                        override val rule:Rule,
                        val beamSize:Int) extends TransitionBasedParser {
  
  case class Candidate(path:StatePath, wrappedAction:WrappedAction, score:Double)

  case class Beam(kbest:List[StatePath]) {
    def isEmpty:Boolean = kbest.isEmpty

    def reset(candidates:List[Candidate]) = resetQuick(candidates.sortWith(_.score > _.score))
    def resetQuick(sortedCandidates:List[Candidate]) = {
      val newKBest = sortedCandidates.sortWith(_.score > _.score).take(beamSize).map {
        case Candidate(path, wrappedAction, score) => {
          val newState = path.state.proceed(wrappedAction.v, wrappedAction.isGold)
          StatePath(newState, wrappedAction :: path.actionPath, score)
        }
      }
      Beam(newKBest)
    }
    def existsGold = kbest.exists(_.state.isGold)

    def collectCandidatesTrain(sentence:TrainSentence, oracle:Oracle) = kbest.flatMap { path => {
      val goldActions:Seq[Action] = if (path.state.isGold) oracle.goldActions(path.state) else Nil
      // partial features (without label)
      val featuresWithoutLabel = extractor.get(path.state, sentence)

      possibleActions(path.state, sentence).map { action => {
        val isGold = goldActions.contains(action) // support non-deterministic oracle; currently, goldActions only contain one element so this operation is simple equality check
        val feature = new LabeledFeatures(featuresWithoutLabel, action)
        val sumScore = path.score + classifier.score(feature)
        Candidate(path, WrappedAction(action, isGold, feature), sumScore)
      }}
    }}
    def collectCandidatesTest(sentence:TestSentence) = kbest.flatMap { path => {
      val featuresWithoutLabel = extractor.get(path.state, sentence)
      possibleActions(path.state, sentence).map { action => {
        val feature = new LabeledFeatures(featuresWithoutLabel, action)
        val sumScore = path.score + classifier.score(feature)
        Candidate(path, WrappedAction(action, false, Nil), sumScore) // do not preserve (partial) features at test time
      }}
    }}
  }
  object Beam {
    def init(initState:State):Beam = new Beam(StatePath(initState, Nil) :: Nil)
  }
  case class TrainingInstance(winPath:Option[StatePath], goldPath:Option[StatePath])
  
  def getTrainingInstance(sentence:TrainSentence,
                          gold:Derivation,
                          initialState:State): TrainingInstance = {
    var beam = Beam.init(initialState)
    val oracle = oracleGen.gen(sentence, gold, rule)
    var outputPath:Option[StatePath] = None
    var goldPath:Option[StatePath] = None
    def currentOutputScore = outputPath map { _.score } getOrElse(0.0)

    while (!beam.isEmpty) {
      val candidates:List[Candidate] = beam.collectCandidatesTrain(sentence, oracle)
      val (finished, unfinished) = candidates.partition { cand => cand match {
        case Candidate(_, WrappedAction(Finish(), _, _), _) => true
        case _ => false
      }}
      finished.sortWith(_.score > _.score) match {
        case top :: _ => {
          if (top.score > currentOutputScore) outputPath = Some(top.path)
        }
        case Nil => // can be safely ignored
      }
      goldPath = finished.find(_.wrappedAction.isGold) map { _.path } // the most high scored path is regarded as gold (NOTE: current oracle find only one gold; so this process is redundant)

      val sortedUnfinished = unfinished.sortWith(_.score > _.score)
      beam = beam.resetQuick(sortedUnfinished)

      if (!beam.existsGold && goldPath == None) { // early-update check; when goldPath has value, we wait for exhausting the beam
        if (outputPath == None || (outputPath != None && beam.kbest(0).score > currentOutputScore)) {
          outputPath = Some(beam.kbest(0))
        }
        if (goldPath == None) {
          goldPath = unfinished.find(_.wrappedAction.isGold) map { _.path }
        }
        if (goldPath == None) {
          println("BeamSearchDecoder.scala: cannot find gold tree; this may or may not be correct.")
        }
        TrainingInstance(outputPath, goldPath)
      }
    }
    TrainingInstance(outputPath, goldPath)
  }
}
