package enju.ccg

import lexicon._
import tagger._
import ml._
import util.Indexer

trait SuperTagging extends Problem {
  type DictionaryType <: Dictionary

  var dict:DictionaryType
  var indexer: Indexer[SuperTaggingFeature] = _ // feature -> int
  var weights: WeightVector = _                 // featureId -> weight; these 3 variables are serialized/deserialized

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
    val trainSentences = readTrainingSentences
    println("done; # sentences: " + trainSentences.size)

    val numTrainInstances = trainSentences.foldLeft(0) { _ + _.size }

    indexer = new Indexer[SuperTaggingFeature]
    weights = new WeightVector
    val classifier = new LogisticSGD[Int](numTrainInstances, weights, stepsize(numTrainInstances))
    val tagger = new MaxentMultiTagger(indexer, featureExtractors, classifier, dict)

    tagger.trainWithCache(trainSentences, TrainingOptions.numIters)
  }
  
  override def predict = {
    
  }

  override def evaluate = {
    
  }

  override def save = {
    import java.io._
    val os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(OutputOptions.trainedModelPath)))
    os.writeObject(dict)
    os.writeObject(indexer)
    os.writeObject(weights)
  }
  
  def stepsize(n:Int) = {
    import OptionEnumTypes.StepSizeFunction
    (TrainingOptions.stepSizeA, TrainingOptions.stepSizeB) match {
      case (a, b) => TrainingOptions.stepSizeFunc match { 
        case StepSizeFunction.stepSize1 => new LogisticSGD.StepSize1(a, n)
        case StepSizeFunction.stepSize2 => new LogisticSGD.StepSize2(a, b, n)
        case StepSizeFunction.stepSize3 => new LogisticSGD.StepSize3(a)
      }
    }
  }
  
  protected def initializeDictionary: Unit
  protected def readTrainingSentences: Array[GoldSuperTaggedSentence] = {
    val reader = new CCGBankReader(dict)
    
    val trainPath = (InputOptions.bankDirPath, InputOptions.trainPath) match {
      case (dir, "") if dir != "" => dir + "/train.ccgbank"
      case (_, path) => path
    }
    reader.readSentences(trainPath)
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
    }
    dict = new JapaneseDictionary(categoryDictionary)
    
    val lexiconPath = (InputOptions.bankDirPath, InputOptions.lexiconPath) match {
      case (dir, "") if dir != "" => dir + "/Japanese.lexicon"
      case (_, path) => path
    }
    val templatePath = (InputOptions.bankDirPath, InputOptions.templatePath) match {
      case (dir, "") if dir != "" => dir + "/template.lst"
      case (_, path) => path
    }
    dict.readLexicon(lexiconPath, templatePath)
  }
}

// class EnglishSuperTagging extends SuperTagging {
// }
