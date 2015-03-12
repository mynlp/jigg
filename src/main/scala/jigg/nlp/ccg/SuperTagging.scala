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
import tagger.{LF => Feature, _}
import jigg.ml

import scala.io.Source
import scala.collection.mutable.{ArraySeq, ArrayBuffer, HashMap}
import scala.reflect.ClassTag
import java.io.{ObjectInputStream, ObjectOutputStream, FileWriter}

trait SuperTagging extends Problem { outer =>
  type DictionaryType <: Dictionary

  type WeightVector = ml.WeightVector[Float]

  var dict: DictionaryType = _
  var featureMap: HashMap[Feature, Int] = _
  var weights: WeightVector = _ // featureId -> weight; these 3 variables are serialized/deserialized

  def featureExtractors = { // you can change features by modifying this function
    val extractionMethods = Array(new UnigramWordExtractor(5), // unigram feature of window size 5
      // new BigramWordExtractor(3),
      new UnigramPoSExtractor(5),  // unigram pos feature of window size 5
      new BigramPoSExtractor(5))   // bigram pos feature using window size 5
    new FeatureExtractorsWithCustomPoSLevel(
      extractionMethods,
      dict.getWord("@@BOS@@"),
      dict.getPoS("@@NULL@@"),
      { pos => pos.secondWithConj.id })
    //new FeatureExtractors(extractionMethods, dict.getWord("@@BOS@@"), dict.getPoS("@@NULL@@"))
  }

  override def train = {
    dict = newDictionary

    System.err.println("Reading CCGBank...")
    val trainSentences = readSentencesFromCCGBank(trainPath, InputOptions.trainSize, true)
    System.err.println("done; # train sentences: " + trainSentences.size)

    System.err.println("Setting word -> category mapping...")
    setCategoryDictionary(trainSentences)
    System.err.println("done.")

    val numTrainInstances = trainSentences.foldLeft(0) { _ + _.size }

    featureMap = new HashMap[Feature, Int]
    val indexer = new ml.ExactFeatureIndexer(featureMap)
    weights = ml.WeightVector.growable[Float]()

    val trainer = getClassifierTrainer(numTrainInstances)
    val tagger = new MaxEntMultiTaggerTrainer(indexer, featureExtractors, trainer, dict)

    tagger.trainWithCache(trainSentences, TrainingOptions.numIters)

    trainer.postProcess // including lazy-updates of all weights
    reduceFeatures
    save
  }

  def reduceFeatures = {
    val buffer = weights.asInstanceOf[ml.GrowableWeightVector[Float]].array // 0 1.0 2.0 0 0 1.0 ...
    val activeIdxs = buffer.zipWithIndex.filter(_._1 != 0).map(_._2)  // 1 2 5
    println(s"# features reduced from ${buffer.size} to ${activeIdxs.size}")
    val idxMap = activeIdxs.zipWithIndex.toMap // {1->0, 2->1 5->2}

    featureMap = featureMap.collect { case (f, oldIdx) if idxMap.isDefinedAt(oldIdx) => (f, idxMap(oldIdx)) }
    weights = new ml.FixedWeightVector[Float](activeIdxs.map(buffer(_)).toArray)
  }

  override def predict = {
    //loadModel
  }
  override def evaluate = {
    load

    System.err.println("Reading CCGBank ...")
    val evalSentences = readSentencesFromCCGBank(developPath, InputOptions.testSize, false)
    val numInstances = evalSentences.foldLeft(0) { _ + _.size }
    System.err.println("done; # evaluating sentences: " + evalSentences.size)

    val before = System.currentTimeMillis

    val assignedSentences = superTagToSentences(evalSentences).toArray // evalSentences.map { s => new TrainSentence(s, tagger.candSeq(s, TaggerOptions.beta)) }

    val taggingTime = System.currentTimeMillis - before
    val sentencePerSec = (evalSentences.size.toDouble / (taggingTime / 1000)).formatted("%.1f")
    val wordPerSec = (numInstances.toDouble / (taggingTime / 1000)).formatted("%.1f")

    System.err.println("tagging time: " + taggingTime + "ms; " + sentencePerSec + "s/sec; " + wordPerSec + "w/sec")
    evaluateTokenSentenceAccuracy(assignedSentences)
    outputPredictions(assignedSentences)
  }
  def superTagToSentences[S<:TaggedSentence](sentences:Array[S]):ArraySeq[S#AssignedSentence] = {
    // TODO: serialize featureExtractors setting at training
    val tagger = getTagger
    val assignedSentences = sentences.map { s =>
      s.assignCands(tagger.candSeq(s, TaggerOptions.beta, TaggerOptions.maxK))
    }
    assignedSentences
  }
  def getTagger = new MaxEntMultiTagger(new ml.ExactFeatureIndexer(featureMap), featureExtractors, getClassifier, dict)
  def getClassifier = new ml.LogLinearClassifier[Int] {
    override val weights = outer.weights
  } // new FixedLogLinerClassifier(weights)
  def getClassifierTrainer(numInstances: Int): ml.OnlineLogLinearTrainer[Int] = {
    import OptionEnumTypes.TaggerTrainAlgorithm
    TaggerOptions.taggerTrainAlg match {
      case TaggerTrainAlgorithm.sgd =>
        new ml.LogLinearSGD[Int](TaggerOptions.stepSizeA.toFloat) {
          override val weights = outer.weights
        }
      case TaggerTrainAlgorithm.adaGradL1 =>
        new ml.LogLinearAdaGradL1[Int](TaggerOptions.lambda.toFloat, TaggerOptions.eta.toFloat) {
          override val weights = outer.weights
        }
    }
  }

  def evaluateTokenSentenceAccuracy(sentences:Array[TrainSentence]) = {
    var sumUnk = 0
    var correctUnk = 0
    val unkType = dict.unkType
    val (numCorrect, numComplete) = sentences.foldLeft(0, 0) {
      case ((cor, comp), sent) =>
        val numCorrectToken = sent.numCandidatesContainGold
        (cor + numCorrectToken, comp + (if (numCorrectToken == sent.size) 1 else 0))
    }
    val (numUnks, numCorrectUnks): (Int,Int) = sentences.foldLeft(0, 0) {
      case ((sum, cor), sent) =>
        val unkIdxes = (0 until sent.size).filter { sent.word(_) == unkType }
        val unkCorrects = unkIdxes.foldLeft(0) {
          case (n, i) if (sent.cand(i).contains(sent.cat(i))) => n + 1
          case (n, _) => n
        }
        (sum + unkIdxes.size, cor + unkCorrects)
    }
    val numInstances = sentences.foldLeft(0) { _ + _.size }
    System.err.println()
    System.err.println("token accuracy: " + numCorrect.toDouble / numInstances.toDouble)
    System.err.println("sentence accuracy: " + numComplete.toDouble / sentences.size.toDouble)
    System.err.println("unknown accuracy: " + numCorrectUnks.toDouble / numUnks.toDouble + " (" + numCorrectUnks + "/" + numUnks + ")")

    val sumCandidates = sentences.foldLeft(0) { case (sum, s) => sum + s.candSeq.map(_.size).sum }
    System.err.println("# average of candidate labels after super-tagging: " + (sumCandidates.toDouble / numInstances.toDouble).formatted("%.2f"))
    System.err.println()
  }
  override def save = {
    import java.io._
    // saveFeaturesToText
    System.err.println("saving tagger model to " + OutputOptions.saveModelPath)

    val os = jigg.util.IOUtil.openBinOut(OutputOptions.saveModelPath)
    saveModel(os)
    os.close
  }
  def saveModel(os: ObjectOutputStream) = {
    os.writeObject(dict)

    os.writeObject(featureMap)
    os.writeObject(weights)
  }
  // def saveFeaturesToText = if (OutputOptions.taggerFeaturePath != "") {
  //   System.err.println("saving features in text to " + OutputOptions.taggerFeaturePath)
  //   val fw = new FileWriter(OutputOptions.taggerFeaturePath)
  //   indexer.foreach {
  //     case (k, v) =>
  //       val featureString = k match {
  //         case SuperTaggingFeature(unlabeled, label) => (unlabeled match {
  //           case unlabeled: FeatureWithoutDictionary => unlabeled.mkString
  //           case unlabeled: FeatureOnDictionary => unlabeled.mkString(dict)
  //         }) + "_=>_" + dict.getCategory(label)
  //       }
  //       fw.write(featureString + " " + weights(v) + "\n")
  //   }
  //   fw.flush
  //   fw.close
  //   System.err.println("done.")
  // }
  def outputPredictions[S<:CandAssignedSentence](sentences:Array[S]) = if (OutputOptions.outputPath != "") {
    System.err.println("saving tagger prediction results to " + OutputOptions.outputPath)
    val fw = new FileWriter(OutputOptions.outputPath)
    sentences.foreach { sentence =>
      (0 until sentence.size).foreach { i =>
        fw.write(sentence.word(i) + " " + sentence.cand(i).mkString(" ") + "\n")
      }
      fw.write("\n")
    }
    fw.flush
    fw.close
    System.err.println("done.")
  }
  def load = {
    import java.io._
    val in = jigg.util.IOUtil.openBinIn(InputOptions.loadModelPath)
    loadModel(in)
    in.close
  }
  def loadModel(in: ObjectInputStream) = jigg.util.LogUtil.track("Loading supertagger ...") {
    dict = in.readObject.asInstanceOf[DictionaryType]
    featureMap = in.readObject.asInstanceOf[HashMap[Feature, Int]]
    weights = in.readObject.asInstanceOf[ml.WeightVector[Float]]
    assert(featureMap.size == weights.size)
  }
  def setCategoryDictionary(sentences: Seq[GoldSuperTaggedSentence]): Unit =
    dict.categoryDictionary.resetWithSentences(sentences, DictionaryOptions.unkThreathold)

  def newDictionary: DictionaryType

  // TODO: separate dictionary part into another class
  protected def readSentencesFromCCGBank(path:String, n:Int, train:Boolean): Array[GoldSuperTaggedSentence] = readAndConvertCCGBankTrees(path, n, train, parseTreeConverter.toSentenceFromStringTree _)

  def readParseTreesFromCCGBank(path:String, n:Int, train:Boolean): Array[ParseTree[NodeLabel]] = readAndConvertCCGBankTrees(path, n, train, parseTreeConverter.toLabelTree _)

  private def readAndConvertCCGBankTrees[A:ClassTag](path:String, n:Int, train:Boolean, convert:ParseTree[String]=>A): Array[A] = {
    newCCGBankReader.readParseTrees(path, n, train).map { convert(_) }.toArray
  }

  def newCCGBankReader: CCGBankReader
  def parseTreeConverter: ParseTreeConverter // language specific tree converter

  def readPoSTaggedSentences(in:Source, n:Int): Array[PoSTaggedSentence]
}

class JapaneseSuperTagging extends SuperTagging {
  override type DictionaryType = JapaneseDictionary

  def newDictionary = new JapaneseDictionary(newCategoryDictionary)

  def newCategoryDictionary = {
    import OptionEnumTypes.CategoryLookUpMethod
    DictionaryOptions.lookupMethod match {
      case CategoryLookUpMethod.surfaceOnly => new Word2CategoryDictionary
      case CategoryLookUpMethod.surfaceAndPoS => new WordPoS2CategoryDictionary
      case CategoryLookUpMethod.surfaceAndSecondFineTag => new WordSecondFineTag2CategoryDictionary
      case CategoryLookUpMethod.surfaceAndSecondWithConj => new WordSecondWithConj2CategoryDictionary
    }
  }
  override def setCategoryDictionary(sentences: Seq[GoldSuperTaggedSentence]): Unit =
    if (DictionaryOptions.useLexiconFiles) setCategoryDictionaryFromLexiconFiles
    else super.setCategoryDictionary(sentences)
  def setCategoryDictionaryFromLexiconFiles = {
    val lexiconPath = pathWithBankDirPathAsDefault(InputOptions.lexiconPath, "Japanese.lexicon")
    val templatePath = pathWithBankDirPathAsDefault(InputOptions.templatePath, "template.lst")
    dict.readLexicon(lexiconPath, templatePath)
  }

  override def newCCGBankReader = new CCGBankReader(dict) // default reader
  override def parseTreeConverter = new JapaneseParseTreeConverter(dict)

  override def readPoSTaggedSentences(in:Source, n:Int): Array[PoSTaggedSentence] = {
    val reader = new MecabReader(dict)
    reader.readSentences(in, n)
  }
}

class EnglishSuperTagging extends SuperTagging {
  override type DictionaryType = SimpleDictionary

  override def featureExtractors = {
    val extractionMethods = Array(new UnigramWordExtractor(5),
      new UnigramPoSExtractor(5),
      new BigramPoSExtractor(5))
    new FeatureExtractorsWithCustomPoSLevel(
      extractionMethods,
      dict.getWord("@@BOS@@"),
      dict.getPoS("@@NULL@@"),
      { pos => pos.id })
  }

  def newDictionary = new SimpleDictionary

  override def newCCGBankReader = new EnglishCCGBankReader(dict)
  override def parseTreeConverter = new EnglishParseTreeConverter(dict)

  override def readPoSTaggedSentences(in:Source, n:Int): Array[PoSTaggedSentence] =
    sys.error("not yet implemented.")
}
