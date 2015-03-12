package jigg.nlp.ccg

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

import lexicon._
import parser.{LF => Feature, KBestDecoder}
import jigg.ml
import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.{ObjectInputStream, ObjectOutputStream, FileWriter}

trait ShiftReduceParsing extends Problem {

  var tagging: SuperTagging = _
  var weights: Array[Float] = _
  var indexer: ml.HashedFeatureIndexer[Feature] = _
  var rule: parser.Rule = _

  def featureExtractors = {
    val extractionMethods = Array(new parser.ZhangExtractor)
    new parser.FeatureExtractors(extractionMethods, { pos => pos.secondWithConj.id })
  }
  def instantiateSuperTagging: SuperTagging
  def getHeadFinder(trees: Seq[ParseTree[NodeLabel]]): parser.HeadFinder

  override def train = { // asuming the model of SuperTagging is saved
    loadSuperTagging
    val parseTrees = readParseTreesFromCCGBank(trainPath, InputOptions.trainSize, true)

    val sentences = parseTrees.map { tagging.parseTreeConverter.toSentenceFromLabelTree(_) }
    val derivations = parseTrees.map { tagging.parseTreeConverter.toDerivation(_) }

    val trainingSentences = superTaggingToSentences(sentences) // assign candidates

    System.err.println("extracting CFG rules from all derivations ...")
    rule = parser.CFGRule.extractRulesFromDerivations(derivations, getHeadFinder(parseTrees))
    System.err.println("done.")

    indexer = ml.HashedFeatureIndexer[Feature]()

    weights = new Array[Float](indexer.size)
    val perceptron = new ml.FixedPerceptron[parser.ActionLabel](weights)

    val decoder = getDecoder(indexer, perceptron)

    System.err.println("training start!")

    (0 until TrainingOptions.numIters) foreach { i =>
      val correct = decoder.trainSentences(trainingSentences, derivations)
      System.err.println("accuracy (" + i + "): " + correct.toDouble / sentences.size.toDouble + " [" + correct + "]")
      //System.err.println("# features: " + indexer.size)
      if (TrainingOptions.removeZero) {
        System.err.println(weights.size + " " + perceptron.averageWeights.size)
        assert(weights.size == perceptron.averageWeights.size)
      }
    }
    perceptron.takeAverage // averaging weight
    save
  }
  def readParseTreesFromCCGBank(path: String, n:Int, train:Boolean) = {
    System.err.println("reading CCGBank sentences ...")
    val trees = tagging.readParseTreesFromCCGBank(path, n, train)
    System.err.println("done.")
    trees
  }

  def superTaggingToSentences(sentences:Array[GoldSuperTaggedSentence]): Array[TrainSentence] = {
    System.err.println("super tagging: assign candidate categories to sentences ...")
    val taggedSentences:Seq[TrainSentence] = tagging.superTagToSentences(sentences)
    System.err.println("done.")
    val sumCandidates = taggedSentences.foldLeft(0) { case (sum, s) => sum + s.candSeq.map(_.size).sum }
    val numInstances = sentences.foldLeft(0) { _ + _.size }
    System.err.println("# average of candidate labels after super-tagging: " + sumCandidates.toDouble / numInstances.toDouble)

    taggedSentences.map { _.pickUpGoldCategory }.toArray // for training
  }
  override def evaluate = {
    if (OutputOptions.outputPath != "") prepareDirectoryOutput(OutputOptions.outputPath)
    load

    val parseTrees = readParseTreesFromCCGBank(developPath, InputOptions.testSize, false)

    val sentences = parseTrees.map { tagging.parseTreeConverter.toSentenceFromLabelTree(_) }
    val derivations = parseTrees.map { tagging.parseTreeConverter.toDerivation(_) }

    val numInstances = sentences.foldLeft(0) { _ + _.size }

    val before = System.currentTimeMillis
    val predDerivations = getPredDerivations(sentences)
    val parsingTime = System.currentTimeMillis - before

    val sentencePerSec = (sentences.size.toDouble / (parsingTime / 1000)).formatted("%.1f")
    val wordPerSec = (numInstances.toDouble / (parsingTime / 1000)).formatted("%.1f")

    System.err.println("parsing time: " + parsingTime + "ms; " + sentencePerSec + "s/sec; " + wordPerSec + "w/sec")

    evaluateCategoryAccuracy(sentences, predDerivations)
    outputDerivations(sentences, predDerivations)

    evaluateBunsetsuDeps(sentences, derivations, predDerivations)
  }
  // return prediction time
  def getPredDerivations[S<:TaggedSentence](sentences:Array[S]): Array[Derivation] = {
    val tagger = tagging.getTagger
    val decoder = getPredDecoder

    val preferConnected = ParserOptions.preferConnected

    val predDerivations = sentences.zipWithIndex map {
      case (sentence, i) =>
        if (i % 100 == 0) System.err.print(i + "\t/" + sentences.size + " have been processed.\r")
        val superTaggedSentence = sentence.assignCands(tagger.candSeq(sentence, TaggerOptions.beta, TaggerOptions.maxK))

        val derivation = decoder match {
          case decoder: KBestDecoder =>
            if (preferConnected) decoder.predictConnected(superTaggedSentence)
            else decoder.predict(superTaggedSentence)
          case decoder => decoder.predict(superTaggedSentence)
        }
        derivation._1
    }
    System.err.println()
    predDerivations
  }
  def getPredDecoder = getDecoder(indexer, new ml.FixedPerceptron[parser.ActionLabel](weights), false)

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
    System.err.println("\ncategory accuracies:")
    System.err.println("-----------------------")
    System.err.println("token accuracy: " + numCorrects.toDouble / numInstances.toDouble)
    System.err.println("sentence accuracy: " + numCompletes.toDouble / sentences.size.toDouble)
  }
  def evaluateBunsetsuDeps(sentences:Array[GoldSuperTaggedSentence], golds:Array[Derivation], derivations:Array[Derivation]) = {} // defualt = do nothing

  override def predict = {
    load

    val sentences = tagging.readPoSTaggedSentences(
      //if (InputOptions.testPath == "") Source.stdin else Source.fromFile(InputOptions.testPath),
      Source.fromFile(InputOptions.testPath),
      InputOptions.testSize)

    val before = System.currentTimeMillis
    val predDerivations = getPredDerivations(sentences)
    val parsingTime = System.currentTimeMillis - before

    outputDerivations(sentences, predDerivations)
  }
  override def save = {
    import java.io._
    //saveFeaturesToText

    System.err.println("saving tagger+parser model to " + OutputOptions.saveModelPath)
    val os = jigg.util.IOUtil.openBinOut(OutputOptions.saveModelPath)
    tagging.saveModel(os)
    saveModel(os)
    os.close
  }
  def saveModel(os:ObjectOutputStream) = {
    os.writeObject(weights)
    os.writeObject(indexer)
    os.writeObject(rule)
  }
  // def saveFeaturesToText = if (OutputOptions.parserFeaturePath != "") {
  //   System.err.println("saving features in text to " + OutputOptions.parserFeaturePath)
  //   val dict = tagging.dict
  //   val fw = new FileWriter(OutputOptions.parserFeaturePath)
  //   indexer.foreach {
  //     case (k, v) =>
  //       val featureString = k match {
  //         case parser.ShiftReduceFeature(unlabeled, label) => (unlabeled match {
  //           case unlabeled: parser.FeatureWithoutDictionary => unlabeled.mkString
  //           case unlabeled: parser.FeatureOnDictionary => unlabeled.mkString(dict)
  //         }) + "_=>_" + label.mkString(dict)
  //       }
  //       fw.write(featureString + " " + weights(v) + "\n")
  //   }
  //   fw.flush
  //   fw.close
  //   System.err.println("done.")
  // }
  def outputDerivations[S<:TaggedSentence](sentences:Array[S], derivations:Array[Derivation]) = if (OutputOptions.outputPath != "") {
    def outputInFormat(path: String, conv: (S, Derivation, Int)=>String) = {
      val fw = new FileWriter(path)
      sentences.zip(derivations).zipWithIndex.map {
        case ((s, deriv), i) =>
          fw.write(conv(s, deriv, i) + "\n")
      }
      fw.flush
      fw.close
    }
    outputInFormat(OutputOptions.outputPath + "/pred", (s, deriv, i) => deriv.render(s))
    outputInFormat(OutputOptions.outputPath + "/pred.xml", (s, deriv, i) => deriv.renderEnjuXML(s, i))
  }

  def load = {
    import java.io._

    val begin = System.currentTimeMillis

    val in = InputOptions.loadModelPath match {
      case "" =>
        val loader = Thread.currentThread.getContextClassLoader
        val modelName = s"ccg-models/parser/beam=${ParserOptions.beam}.ser.gz"
        System.err.println("Search for model: " + modelName)

        val input = loader.getResourceAsStream(modelName)

        jigg.util.IOUtil.openZipBinIn(input)

      case path => jigg.util.IOUtil.openBinIn(path)
    }

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
    import jigg.util.LogUtil._
    track("Loading feature weights of CCG parser ...") {
      weights = in.readObject.asInstanceOf[Array[Float]]
    }
    indexer = in.readObject.asInstanceOf[ml.HashedFeatureIndexer[Feature]]

    // TODO: branch according to the setting of rule (cfg or not)
    rule = in.readObject.asInstanceOf[parser.CFGRule]
    // val binary = in.readObject.asInstanceOf[Map[(Int,Int), Array[(Category,String)]]]
    // val unary = in.readObject.asInstanceOf[Map[Int, Array[(Category,String)]]]
    // rule = parser.CFGRule(binary, unary, headFinder)
  }
  def getDecoder(indexer: ml.FeatureIndexer[Feature], perceptron:ml.Perceptron[parser.ActionLabel], train:Boolean = true) =
    if (train && ParserOptions.beam == 1)
      new parser.DeterministicDecoder(indexer,
        featureExtractors,
        perceptron,
        parser.StaticOracleGenerator,
        rule,
        parser.InitialFullState)
    else
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
  override def getHeadFinder(trees: Seq[ParseTree[NodeLabel]]) = parser.JapaneseHeadFinder

  override def evaluateBunsetsuDeps(sentences:Array[GoldSuperTaggedSentence], golds:Array[Derivation], derivations:Array[Derivation]) = {
    val goldCabochaSentences = readCabochaSentences(InputOptions.cabochaPath, sentences)

    def cabochaSentencesFromDerives(derives: Seq[Derivation]) = goldCabochaSentences.zip(derives).map {
      case (sent, deriv) => BunsetsuSentence(sent.bunsetsuSeq).parseWithCCGDerivation(deriv)
    }

    val goldBankSentences = cabochaSentencesFromDerives(golds)

    val predCabochaSentences = cabochaSentencesFromDerives(derivations)
    val connectedCabochaSentences = cabochaSentencesFromDerives(derivations.map { _.toSingleRoot })

    def outputDependencyAccuracies(goldSentences: Seq[ParsedBunsetsuSentence]) = {
      evaluateBunsetsuDepAccuracy(predCabochaSentences, goldSentences)

      System.err.println("\nevaluation with modified derivations:")
      evaluateBunsetsuDepAccuracy(connectedCabochaSentences, goldSentences)
    }

    // System.err.println("\ndependency accuracies against Kyodai dependency:")
    // System.err.println("-----------------------")
    // outputDependencyAccuracies(goldCabochaSentences)

    System.err.println("\ndependency accuracies against CCGBank dependency:")
    System.err.println("-----------------------")
    outputDependencyAccuracies(goldBankSentences)

    System.err.println("\ndependency accuracies for only category-covered setences:")
    System.err.println("-----------------------")

    val tagger = tagging.getTagger
    val targetIdxs = sentences.zipWithIndex.withFilter { case (s, i) =>
      s.assignCands(tagger.candSeq(s, TaggerOptions.beta, TaggerOptions.maxK)).numCandidatesContainGold == s.size
    }.map(_._2)
    // TODO: share the code with the above evaluations
    evaluateBunsetsuDepAccuracy(targetIdxs.map(predCabochaSentences(_)), targetIdxs.map(goldCabochaSentences(_)))

    System.err.println("\nevaluation with modified derivations:")
    evaluateBunsetsuDepAccuracy(targetIdxs.map(connectedCabochaSentences(_)), targetIdxs.map(goldCabochaSentences(_)))

    outputBunsetsuDeps(predCabochaSentences)
  }

  // TODO: segment into a cabocha-specific reader class
  def readCabochaSentences[S<:TaggedSentence](path: String, ccgSentences: Seq[S]): Seq[ParsedBunsetsuSentence] =
    new CabochaReader(ccgSentences).readSentences(path)

  def evaluateBunsetsuDepAccuracy(preds: Seq[ParsedBunsetsuSentence], golds: Seq[ParsedBunsetsuSentence]) = {
    val (numCorrects, numCompletes) = preds.zip(golds).foldLeft(0, 0) {
      case ((corrects, completes), (pred, gold)) =>
        val numCorrectHeads = gold.headSeq.dropRight(1).zip(pred.headSeq).count { a => a._1 == a._2 }
        (corrects + numCorrectHeads, completes + (if (numCorrectHeads == pred.size - 1) 1 else 0))
    }
    val (numConnectedB, numConnectedS) = preds.foldLeft(0, 0) {
      case ((connectedB, connectedS), pred) =>
        val numConnectedHeads = pred.headSeq.dropRight(1).count(_ != -1)
        (connectedB + numConnectedHeads, connectedS + (if (numConnectedHeads == pred.size - 1) 1 else 0))
    }

    val numInstances = preds.map(_.size - 1).sum

    def format_acc(numer: Int, denom: Int) = "%f (%d/%d)".format(numer.toDouble / denom.toDouble, numer, denom)

    System.err.println("bunsetsu accuracy: " + format_acc(numCorrects, numInstances))
    System.err.println("sentence accuracy: " + format_acc(numCompletes, preds.size))
    System.err.println("Coverage of fully connected analysis (bunsetsu): " + format_acc(numConnectedB, numInstances))
    System.err.println("Coverage of fully connected analysis (sentence): " + format_acc(numConnectedS, preds.size))

    val (activeNumCorrect, activeSum) = preds.zip(golds).foldLeft(0, 0) {
      case ((corrects, sum), (pred, gold)) =>
        val activeHeadIdxs = (0 until pred.headSeq.size - 1).filter(pred.headSeq(_) != -1)
        val numCorrectHeads = activeHeadIdxs.count { i => pred.headSeq(i) == gold.headSeq(i) }
        (corrects + numCorrectHeads, sum + activeHeadIdxs.size)
    }
    System.err.println("active bunsetsu accuracy: " + format_acc(activeNumCorrect, activeSum))
  }

  def outputBunsetsuDeps(sentences:Seq[ParsedBunsetsuSentence]) = if (OutputOptions.outputPath != "") {
    System.err.println("\nsaving predicted bunsetsu dependencies to " + OutputOptions.outputPath)

    outputBunsetsuDepsIn({ sent => sent.renderInCabocha },
      sentences,
      OutputOptions.outputPath + "/pred.cabocha")
    outputBunsetsuDepsIn({ sent => sent.renderInCoNLL },
      sentences,
      OutputOptions.outputPath + "/pred.conll")

    System.err.println("done")
  }
  def outputBunsetsuDepsIn(conv:ParsedBunsetsuSentence=>String, sentences:Seq[ParsedBunsetsuSentence], path: String) = {
    val fw = new FileWriter(path)
    sentences.foreach { sent => fw.write(conv(sent) + "\n") }
    fw.flush
    fw.close
  }

}

class EnglishShiftReduceParsing extends ShiftReduceParsing {
  override def featureExtractors = {
    val extractionMethods = Array(new parser.ZhangExtractor)
    new parser.FeatureExtractors(extractionMethods, { pos => pos.id })
  }

  override def instantiateSuperTagging = new EnglishSuperTagging
  override def getHeadFinder(trees: Seq[ParseTree[NodeLabel]]) = parser.EnglishHeadFinder.createFromParseTrees(trees)
}
