package enju.ccg

import lexicon._

import scala.collection.mutable.HashMap

trait ShiftReduceParsing extends Problem {
  var indexer: parser.FeatureIndexer = _ 
  var weights: ml.WeightVector = _
  
  def featureExtractors = {
    val extractionMethods = Array(new parser.ZhangExtractor)
    new parser.FeatureExtractors(extractionMethods)
  }
  def superTagging:SuperTagging
  def headFinder:parser.HeadFinder

  override def train = { // asuming the model of SuperTagging is saved
    val (trainingSentences, derivations) = getTrainingSentenceAndDerivations
    
    val perceptron = new ml.Perceptron[parser.ActionLabel](weights)
    val oracleGen = parser.StaticOracleGenerator
    val rule = parser.CFGRule.extractRulesFromDerivations(derivations, headFinder)
    val initialState = parser.InitialFullState
    
    val decoder = new parser.BeamSearchDecoder(indexer,
                                               featureExtractors,
                                               perceptron,
                                               oracleGen,
                                               rule,
                                               ParserOptions.beam,
                                               parser.InitialFullState)
    decoder.trainSentences(trainingSentences, derivations, TrainingOptions.numIters)
  }
  def getTrainingSentenceAndDerivations:(Array[TrainSentence],Array[Derivation]) = {
    val tagging = superTagging
    tagging.loadModel
    val (sentences, derivations) = tagging.readCCGBank(trainPath, true)
    (tagging.superTagToSentences(sentences).toArray, derivations)
  }
  override def evaluate = {}
  override def predict = {}
  override def save = {}
}

class JapaneseShiftReduceParsing extends ShiftReduceParsing {
  override def superTagging = new JapaneseSuperTagging
  override def headFinder:parser.HeadFinder = parser.JapaneseHeadFinder
  
}
