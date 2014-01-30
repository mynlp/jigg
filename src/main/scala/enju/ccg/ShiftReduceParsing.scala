package enju.ccg

import lexicon._

import scala.collection.mutable.HashMap
import java.io.{ObjectInputStream, ObjectOutputStream, FileWriter}

trait ShiftReduceParsing extends Problem {
  type WeightVector = ml.NumericBuffer[Double]

  var tagging: SuperTagging = _
  var indexer: parser.FeatureIndexer = _
  var weights: WeightVector = _
  var rule: parser.Rule = _

  def featureExtractors = {
    val extractionMethods = Array(new parser.ZhangExtractor)
    new parser.FeatureExtractors(extractionMethods)
  }
  def instantiateSuperTagging: SuperTagging
  def headFinder: parser.HeadFinder

  override def train = { // asuming the model of SuperTagging is saved
    loadSuperTagging
    val (sentences, derivations) = readCCGBank(trainPath, InputOptions.trainSize, true)
    val trainingSentences = superTaggingToSentences(sentences) // assign candidates

    println("extracting CFG rules from all derivations ...")
    rule = parser.CFGRule.extractRulesFromDerivations(derivations, headFinder)
    println("done.")

    indexer = new parser.FeatureIndexer
    weights = new WeightVector

    val perceptron = new ml.Perceptron[parser.ActionLabel](weights)
    val decoder = getDecoder(perceptron)

    println("training start!")
    decoder.trainSentences(trainingSentences, derivations, TrainingOptions.numIters)
    perceptron.takeAverage // averaging weight
    save
  }
  def readCCGBank(path: String, n:Int, train:Boolean):(Array[GoldSuperTaggedSentence],Array[Derivation]) = {
    println("reading CCGBank sentences ...")
    val (sentences, derivations) = tagging.readCCGBank(path, n, train)
    println("done.")
    (sentences, derivations)
  }
  def superTaggingToSentences(sentences:Array[GoldSuperTaggedSentence]): Array[TrainSentence] = {
    println("super tagging: assign candidate categories to sentences ...")
    val taggedSentences = tagging.superTagToSentences(sentences)
    println("done.")
    val sumCandidates = taggedSentences.foldLeft(0) { case (sum, s) => sum + s.candSeq.map(_.size).sum }
    val numInstances = sentences.foldLeft(0) { _ + _.size }
    println("# average of candidate labels after super-tagging: " + sumCandidates.toDouble / numInstances.toDouble)

    taggedSentences.map { _.pickUpGoldCategory }.toArray // for training
  }
  override def evaluate = {
    load
    val (sentences, derivations) = readCCGBank(developPath, InputOptions.testSize, false)
    val tagger = tagging.getTagger
    val decoder = getDecoder(new ml.Perceptron[parser.ActionLabel](weights))

    val numInstances = sentences.foldLeft(0) { _ + _.size }

    val before = System.currentTimeMillis
    val predDerivations = sentences.zip(derivations).zipWithIndex map {
      case ((sentence, derivation), i) =>
        if (i % 100 == 0) print(i + "\t/" + sentences.size + " have been processed.")
        val superTaggedSentence = sentence.assignCands(tagger.candSeq(sentence, TaggerOptions.beta))
        decoder.predict(superTaggedSentence)
    }
    println()
    val parsingTime = System.currentTimeMillis - before
    val sentencePerSec = (sentences.size.toDouble / (parsingTime / 1000)).formatted("%.1f")
    val wordPerSec = (numInstances.toDouble / (parsingTime / 1000)).formatted("%.1f")

    println("parsing time: " + parsingTime + "ms; " + sentencePerSec + "s/sec; " + wordPerSec + "w/sec")
    evaluateCategoryAccuracy(sentences, predDerivations)
    outputDerivations(sentences, predDerivations)
  }
  def evaluateCategoryAccuracy(sentences:Array[GoldSuperTaggedSentence], derivations:Array[Derivation]) = {
    val (numCorrects, numCompletes) = sentences.zip(derivations).foldLeft(0, 0) {
      case ((corrects, completes), (sentence, derivation)) =>
        val numCorrectTokens = sentence.catSeq.zip(derivation.categorySeq).foldLeft(0) {
          case (correctSoFar, (gold, Some(pred))) if (gold == pred) => correctSoFar + 1
          case (correctSoFar, _) => correctSoFar
        }
        (corrects + numCorrectTokens, completes + (if (numCorrectTokens == sentence.size) 1 else 0))
    }
    val numInstances = sentences.foldLeft(0) { _ + _.size }
    println("token accuracy: " + numCorrects.toDouble / numInstances.toDouble)
    println("sentence accuracy: " + numCompletes.toDouble / sentences.size.toDouble)
  }

  override def predict = {
    //load
  }
  override def save = {
    import java.io._
    saveFeaturesToText

    println("saving tagger+parser model to " + OutputOptions.saveModelPath)
    val os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(OutputOptions.saveModelPath)))
    tagging.saveModel(os)
    saveModel(os)
    os.close
  }
  def saveModel(os:ObjectOutputStream) = {
    os.writeObject(indexer)
    os.writeObject(weights)
    rule match {
      case parser.CFGRule(binary,unary,_) => {
        os.writeObject(binary)
        os.writeObject(unary)
      }
      case _ => // unification -> nothing
    }
  }
  def saveFeaturesToText = if (OutputOptions.parserFeaturePath != "") {
    println("saving features in text to " + OutputOptions.parserFeaturePath)
    val dict = tagging.dict
    val fw = new FileWriter(OutputOptions.parserFeaturePath)
    indexer.foreach {
      case (k, v) =>
        val featureString = k match {
          case parser.ShiftReduceFeature(unlabeled, label) => (unlabeled match {
            case unlabeled: parser.FeatureWithoutDictionary => unlabeled.mkString
            case unlabeled: parser.FeatureOnDictionary => unlabeled.mkString(dict)
          }) + "_=>_" + label.mkString(dict)
        }
        fw.write(featureString + " " + weights(v) + "\n")
    }
    fw.flush
    fw.close
    println("done.")
  }
  def outputDerivations[S<:TaggedSentence](sentences:Array[S], derivations:Array[Derivation]) = if (OutputOptions.outputPath != "") {
    println("saving predicted derivations to " + OutputOptions.outputPath)
    val fw = new FileWriter(OutputOptions.outputPath)
    sentences.zip(derivations).map {
      case (sentence, derivation) =>
        fw.write(derivation.render(sentence) + "\n")
    }
    fw.flush
    fw.close
    println("done")
  }
  def load = {
    AVM.readK2V(InputOptions.avmPath)
    import java.io._
    val in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(InputOptions.loadModelPath)))
    println("load start")
    tagging = instantiateSuperTagging
    tagging.loadModel(in)
    loadModel(in)
    in.close
  }
  def loadSuperTagging = {
    tagging = instantiateSuperTagging
    tagging.load
  }
  def loadModel(in:ObjectInputStream) = {
    indexer = in.readObject.asInstanceOf[parser.FeatureIndexer]
    println("parser feature templates load done.")

    weights = in.readObject.asInstanceOf[WeightVector]
    println("parser model weights load done.")

    // TODO: branch according to the setting of rule (cfg or not)
    val binary = in.readObject.asInstanceOf[Map[(Int,Int), Array[(Category,String)]]]
    val unary = in.readObject.asInstanceOf[Map[Int, Array[(Category,String)]]]
    rule = parser.CFGRule(binary, unary, headFinder)
  }
  def getDecoder(perceptron:ml.Perceptron[parser.ActionLabel]) =
    new parser.BeamSearchDecoder(indexer,
                                 featureExtractors,
                                 perceptron,
                                 parser.StaticOracleGenerator,
                                 rule,
                                 ParserOptions.beam,
                                 parser.InitialFullState)
}

class JapaneseShiftReduceParsing extends ShiftReduceParsing {
  override def instantiateSuperTagging = new JapaneseSuperTagging
  override def headFinder:parser.HeadFinder = parser.JapaneseHeadFinder
}
