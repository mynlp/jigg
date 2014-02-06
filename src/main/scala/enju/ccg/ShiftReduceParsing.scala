package enju.ccg

import lexicon._
import parser.{LF => Feature}
import scala.collection.mutable.HashMap
import java.io.{ObjectInputStream, ObjectOutputStream, FileWriter}

trait ShiftReduceParsing extends Problem {
  type WeightVector = ml.NumericBuffer[Double]

  var tagging: SuperTagging = _
  var indexer: ml.FeatureIndexer[Feature] = _
  var weights: WeightVector = _
  var rule: parser.Rule = _

  def featureExtractors = {
    val extractionMethods = Array(new parser.ZhangExtractor)
    new parser.FeatureExtractors(extractionMethods, { pos => pos.secondWithConj.id })
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

    indexer = new ml.FeatureIndexer[Feature]
    weights = new WeightVector

    val perceptron = new ml.Perceptron[parser.ActionLabel](weights)
    val decoder = getDecoder(perceptron)

    println("training start!")

    (0 until TrainingOptions.numIters) foreach { i =>
      val correct = decoder.trainSentences(trainingSentences, derivations)
      println("accuracy (" + i + "): " + correct.toDouble / sentences.size.toDouble + " [" + correct + "]")
      println("# features: " + indexer.size)
      if (TrainingOptions.removeZero) {
        println(weights.size + " " + perceptron.averageWeights.size)
        assert(weights.size == perceptron.averageWeights.size)
        Problem.removeZeroWeightFeatures(indexer, weights, perceptron.averageWeights)
      }
    }
    // decoder.trainSentences(trainingSentences, derivations, TrainingOptions.numIters)
    perceptron.takeAverage // averaging weight
    Problem.removeZeroWeightFeatures(indexer, weights)
    save
  }
  def readCCGBank(path: String, n:Int, train:Boolean):(Array[GoldSuperTaggedSentence],Array[Derivation]) = {
    println("reading CCGBank sentences ...")
    val (sentences, derivations) = tagging.readCCGBank(path, n, train)
    println("done.")
    (sentences, derivations)
  }
  // TODO: segment into a cabocha-specific reader class
  def readCabochaSentences[S<:TaggedSentence](path: String, ccgSentences: Array[S]): Array[ParsedBunsetsuSentence] = {
    val bunsetsuStart = """\* (\d+) (-?\d+)[A-Z]""".r
    def addBunsetsuTo(curSent: List[(String, Int)], curBunsetsu: List[String]) = curBunsetsu.reverse match {
      case Nil => curSent
      case headIdx :: tail => (tail.mkString(""), headIdx.toInt) :: curSent
    }

    val bunsetsuSegedSentences: List[List[(String, Int)]] =
      scala.io.Source.fromFile(path).getLines.filter(_ != "").foldLeft(
        (List[List[(String, Int)]](), List[(String, Int)](), List[String]())) {
        case ((processed, curSent, curBunsetsu), line) => line match {
          case bunsetsuStart(_, nextHeadIdx) =>
            (processed, addBunsetsuTo(curSent, curBunsetsu), nextHeadIdx :: Nil) // use first elem as the head idx
          case "EOS" => (addBunsetsuTo(curSent, curBunsetsu).reverse :: processed, Nil, Nil)
          case word => (processed, curSent, word.split("\t")(0) :: curBunsetsu)
        }
      }._1.reverse

    ccgSentences.zip(bunsetsuSegedSentences).map { case (ccgSentence, bunsetsuSentence) =>
      val bunsetsuSegCharIdxs: List[Int] = bunsetsuSentence.map { _._1.size }.scanLeft(0)(_+_).tail // 5 10 ...
      val ccgWordSegCharIdxs: List[Int] = ccgSentence.wordSeq.toList.map { _.v.size }.scanLeft(0)(_+_).tail // 2 5 7 10 ...

      assert(bunsetsuSegCharIdxs.last == ccgWordSegCharIdxs.last)
      val bunsetsuSegWordIdxs: List[Int] = ccgWordSegCharIdxs.zipWithIndex.foldLeft((List[Int](), 0)) { // 1 3 ...
        case ((segWordIdxs, curBunsetsuIdx), (wordIdx, i)) =>
          if (wordIdx >= bunsetsuSegCharIdxs(curBunsetsuIdx)) (i :: segWordIdxs, curBunsetsuIdx + 1)
          else (segWordIdxs, curBunsetsuIdx) // wait until wordIdx exceeds the next bunsetsu segment
      }._1.reverse
      val bunsetsuSeq = bunsetsuSegWordIdxs.zip(-1 :: bunsetsuSegWordIdxs).map { case (bunsetsuIdx, prevIdx) =>
        val offset = prevIdx + 1
        Bunsetsu(offset,
          ccgSentence.wordSeq.slice(offset, bunsetsuIdx + 1),
          ccgSentence.posSeq.slice(offset, bunsetsuIdx + 1))
      }
      ParsedBunsetsuSentence(bunsetsuSeq, bunsetsuSentence.map { _._2 })
    }
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
        if (i % 100 == 0) print(i + "\t/" + sentences.size + " have been processed.\r")
        val superTaggedSentence = sentence.assignCands(tagger.candSeq(sentence, TaggerOptions.beta))
        decoder.predict(superTaggedSentence)
    }
    println()
    val parsingTime = System.currentTimeMillis - before
    val sentencePerSec = (sentences.size.toDouble / (parsingTime / 1000)).formatted("%.1f")
    val wordPerSec = (numInstances.toDouble / (parsingTime / 1000)).formatted("%.1f")

    println("parsing time: " + parsingTime + "ms; " + sentencePerSec + "s/sec; " + wordPerSec + "w/sec")

    val goldCabochaSentences = readCabochaSentences(InputOptions.cabochaPath, sentences)
    val predCabochaSentences = goldCabochaSentences.zip(predDerivations).map { case (sent, deriv) =>
      BunsetsuSentence(sent.bunsetsuSeq).parseWithCCGDerivation(deriv)
    }
    evaluateCategoryAccuracy(sentences, predDerivations)
    evaluateBunsetsuDepAccuracy(predCabochaSentences, goldCabochaSentences)

    outputDerivations(sentences, predDerivations)
    outputBunsetsuDeps(predCabochaSentences)
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
    println("\ncategory accuracies:")
    println("-----------------------")
    println("token accuracy: " + numCorrects.toDouble / numInstances.toDouble)
    println("sentence accuracy: " + numCompletes.toDouble / sentences.size.toDouble)
  }
  def evaluateBunsetsuDepAccuracy(preds: Array[ParsedBunsetsuSentence], golds: Array[ParsedBunsetsuSentence]) = {
    val (numCorrects, numCompletes) = preds.zip(golds).foldLeft(0, 0) {
      case ((corrects, completes), (pred, gold)) =>
        val numCorrectHeads = gold.headSeq.dropRight(1).zip(pred.headSeq).count { a => a._1 == a._2 }
        (corrects + numCorrectHeads, completes + (if (numCorrectHeads == pred.size - 1) 1 else 0))
    }
    val numInstances = preds.map(_.size - 1).sum
    println("\ndependency accuracies:")
    println("-----------------------")
    println("token accuracy: " + numCorrects.toDouble / numInstances.toDouble)
    println("sentence accuracy: " + numCompletes.toDouble / preds.size.toDouble)
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
  def outputBunsetsuDeps(sentences:Seq[ParsedBunsetsuSentence]) = {
    val depsPath = OutputOptions.outputPath + ".cabocha"
    println("saving predicted bunsetsu dependencies to " + depsPath)
    val fw = new FileWriter(depsPath)
    sentences.foreach { sent => fw.write(sent.renderInCabocha + "\n") }
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
    indexer = in.readObject.asInstanceOf[ml.FeatureIndexer[Feature]]
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
