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
  def superTagging: SuperTagging
  def headFinder: parser.HeadFinder

  override def train = { // asuming the model of SuperTagging is saved
    val (trainingSentences, derivations) = getTrainingSentenceAndDerivations

    indexer = new parser.FeatureIndexer
    weights = new ml.WeightVector
    
    val perceptron = new ml.Perceptron[parser.ActionLabel](weights)
    val oracleGen = parser.StaticOracleGenerator
    
    println("extracting CFG rules from all derivations ...")
    val rule = parser.CFGRule.extractRulesFromDerivations(derivations, headFinder)
    println("done.")

    val initialState = parser.InitialFullState
    
    println("training start!")
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
    println("reading CCGBank sentences ...")
    val (sentences, derivations) = tagging.readCCGBank(trainPath, true)
    println("done.")
    
    println("super tagging: assign candidate categories to sentences ...")
    val trainSentences = tagging.superTagToSentences(sentences)
    println("done.")
    
    val containGoldTrainSentence = trainSentences.map { _.pickUpGoldCategory }.toArray
    (containGoldTrainSentence, derivations)
  }
  override def evaluate = {}
  override def predict = {}
  override def save = {}
}

class JapaneseShiftReduceParsing extends ShiftReduceParsing {
  override def superTagging = new JapaneseSuperTagging
  override def headFinder:parser.HeadFinder = parser.JapaneseHeadFinder
  
}
