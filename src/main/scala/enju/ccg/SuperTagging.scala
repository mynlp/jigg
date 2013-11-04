package enju.ccg

import lexicon._
import tagger.{SuperTaggingFeature, FeatureWithoutDictionary, FeatureOnDictionary}
import tagger.{FeatureExtractors, UnigramWordExtractor, UnigramPoSExtractor, BigramPoSExtractor}
import tagger.{FeatureIndexer, MaxentMultiTagger}

import scala.collection.mutable.ArraySeq

trait SuperTagging extends Problem {
  type DictionaryType <: Dictionary

  var dict:DictionaryType
  var indexer: FeatureIndexer = _  // feature -> int
  var weights: ml.WeightVector = _ // featureId -> weight; these 3 variables are serialized/deserialized

  def featureExtractors = { // you can change features by modifying this function
    val extractionMethods = Array(new UnigramWordExtractor(5), // unigram feature of window size 5
                                  new UnigramPoSExtractor(5),  // unigram pos feature of window size 5
                                  new BigramPoSExtractor(5))   // bigram pos feature using window size 5
    new FeatureExtractors(extractionMethods, dict.getWord("@@BOS@@"), dict.getPoS("@@NULL@@"))
  }

  override def train = {
    println("Prapare for training...")
    initializeDictionary
    println("Dictionary load done.")

    println("Reading CCGBank...")
    val trainSentences = readSentencesFromCCGBank(trainPath, true)
    println("done; # train sentences: " + trainSentences.size)

    val numTrainInstances = trainSentences.foldLeft(0) { _ + _.size }

    indexer = new FeatureIndexer
    weights = new ml.WeightVector
    val classifier = new ml.LogisticSGD[Int](numTrainInstances, weights, stepsize(numTrainInstances))
    val tagger = new MaxentMultiTagger(indexer, featureExtractors, classifier, dict)
    tagger.trainWithCache(trainSentences, TrainingOptions.numIters)
  }
  override def predict = {
    loadModel

    
  }
  override def evaluate = {
    loadModel
    
    println("Reading CCGBank...")
    val evalSentences:Array[GoldSuperTaggedSentence] = readSentencesFromCCGBank(developPath, false)
    val numInstances = evalSentences.foldLeft(0) { _ + _.size }
    println("done; # evaluating sentences: " + evalSentences.size)

    val before = System.currentTimeMillis    
    val classifier = new ml.LogisticSGD[Int](0, weights, stepsize(0))
    // TODO: serialize featureExtractors setting at training
    val tagger = new MaxentMultiTagger(indexer, featureExtractors, classifier, dict)

    val assignedSentences = superTagToSentences(evalSentences).toArray // evalSentences.map { s => new TrainSentence(s, tagger.candSeq(s, TaggerOptions.beta)) }
    
    val parsingTime = System.currentTimeMillis - before
    val sentencePerSec = (evalSentences.size.toDouble / (parsingTime / 1000)).formatted("%.1f")
    val wordPerSec = (numInstances.toDouble / (parsingTime / 1000)).formatted("%.1f")

    println("parsing time: " + parsingTime + "ms; " + sentencePerSec + "s/sec; " + wordPerSec + "w/sec")
    evaluateTokenSentenceAccuracy(assignedSentences)
  }

  def superTagToSentences[S<:TaggedSentence](sentences:Array[S]):ArraySeq[S#AssignedSentence] = {
    val classifier = new ml.LogisticSGD[Int](0, weights, stepsize(0))
    // TODO: serialize featureExtractors setting at training
    val tagger = new MaxentMultiTagger(indexer, featureExtractors, classifier, dict)
    
    val before = System.currentTimeMillis
    val assignedSentences = sentences.map { s =>
      s.assignCands(tagger.candSeq(s, TaggerOptions.beta))
    }
    assignedSentences
  }
  def evaluateTokenSentenceAccuracy(sentences:Array[TrainSentence]) = {
    val (numCorrect, numComplete) = sentences.foldLeft(0, 0) {
      case ((cor, comp), sent) => {
        val numCorrectToken = sent.numCandidatesContainGold
        (cor + numCorrectToken, comp + (if (numCorrectToken == sent.size) 1 else 0))
      }
    }
    val numInstances = sentences.foldLeft(0) { _ + _.size }
    println("token accuracy: " + numCorrect.toDouble / numInstances.toDouble)
    println("sentence accuracy: " + numComplete.toDouble / sentences.size.toDouble)
  }
  override def save = {
    saveModel
    saveFeaturesToText
  }
  def saveModel = {
    import java.io._
    val os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(OutputOptions.saveModelPath)))
    os.writeObject(dict)
    os.writeObject(indexer)
    os.writeObject(weights)
    os.close
  }
  def saveFeaturesToText = {
    import java.io.FileWriter
    val fw = new FileWriter(OutputOptions.taggerFeaturePath)
    indexer.foreach { case (k, v) => {
      val featureString = k match {
        case SuperTaggingFeature(unlabeled, label) => (unlabeled match {
          case unlabeled: FeatureWithoutDictionary => unlabeled.mkString
          case unlabeled: FeatureOnDictionary => unlabeled.mkString(dict)
        }) + "_=>_" + dict.getCategory(label)
      }
      fw.write(featureString + " " + weights.get(v) + "\n")
    }}
    fw.flush
    fw.close
  }
  def loadModel = {
    AVM.readK2V(InputOptions.avmPath)
    import java.io._

    val in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(InputOptions.loadModelPath)))
    dict = in.readObject.asInstanceOf[DictionaryType]
    println("dict load done.")

    indexer = in.readObject.asInstanceOf[FeatureIndexer]
    println("indexer load done.")

    weights = in.readObject.asInstanceOf[ml.WeightVector]
    println("model weights load done.")
  }
  def stepsize(n:Int) = {
    import OptionEnumTypes.StepSizeFunction
    (TrainingOptions.stepSizeA, TrainingOptions.stepSizeB) match {
      case (a, b) => TrainingOptions.stepSizeFunc match { 
        case StepSizeFunction.stepSize1 => new ml.LogisticSGD.StepSize1(a, n)
        case StepSizeFunction.stepSize2 => new ml.LogisticSGD.StepSize2(a, b, n)
        case StepSizeFunction.stepSize3 => new ml.LogisticSGD.StepSize3(a)
      }
    }
  }
  protected def initializeDictionary: Unit

  protected def pathWithBankDirPathAsDefault(fullPath: String, nameInBankDir: String) = 
  (InputOptions.bankDirPath, fullPath) match {
    case (dir, "") if dir != "" => dir + "/" + nameInBankDir
    case (_, path) => path
  }
  protected def trainPath = pathWithBankDirPathAsDefault(InputOptions.trainPath, "train.ccgbank")
  protected def developPath = pathWithBankDirPathAsDefault(InputOptions.testPath, "develop.ccgbank")

  protected def readSentencesFromCCGBank(path:String, train:Boolean): Array[GoldSuperTaggedSentence] = {
    val reader = new CCGBankReader(dict)
    reader.readSentences(path, InputOptions.trainSize, train)
  }
  
}

class JapaneseSuperTagging extends SuperTagging {
  override type DictionaryType = JapaneseDictionary

  override var dict:DictionaryType = _

  override def initializeDictionary = {
    AVM.readK2V(InputOptions.avmPath)

    import OptionEnumTypes.CategoryLookUpMethod
    val categoryDictionary = DictionaryOptions.lookupMethod match {
      case CategoryLookUpMethod.surfaceOnly => new Word2CategoryDictionary
      case CategoryLookUpMethod.surfaceAndPoS => new WordPoS2CategoryDictionary
      case CategoryLookUpMethod.surfaceAndSecondFineTag => new WordSecondFineTag2CategoryDictionary
      case CategoryLookUpMethod.surfaceAndSecondWithConj => new WordSecondWithConj2CategoryDictionary
    }
    dict = new JapaneseDictionary(categoryDictionary)
    
    val lexiconPath = pathWithBankDirPathAsDefault(InputOptions.lexiconPath, "Japanese.lexicon")
    val templatePath = pathWithBankDirPathAsDefault(InputOptions.templatePath, "template.lst")
    dict.readLexicon(lexiconPath, templatePath)
  }
}

// class EnglishSuperTagging extends SuperTagging {
// }
