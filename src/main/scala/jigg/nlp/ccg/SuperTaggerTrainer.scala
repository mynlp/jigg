package jigg.nlp.ccg

/*
 Copyright 2013-2016 Hiroshi Noji

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
import jigg.ml._

import breeze.config.{CommandLineParser, Help}

import scala.collection.mutable.{ArraySeq, ArrayBuffer, HashMap}
import scala.reflect.ClassTag

import java.io.File

trait SuperTaggerTrainer {

  def params: Params

  type Params = SuperTaggerTrainer.Params

  val dict = mkDictionary()
  val ccgbank = mkBank(dict)

  def trainAndSave(): Unit = {
    val model = train()
    SuperTaggerModel.saveTo(params.model.getPath, model)
  }

  def train(): SuperTaggerModel = {

    System.err.println("Reading CCGBank ...")
    val trainSentences = ccgbank.trainSentences
    System.err.println("done; # train sentences: " + trainSentences.size)

    System.err.println("Setting word -> category mapping ...")
    setCategoryDictionary(trainSentences)
    System.err.println("done.")

    val numTrainInstances = trainSentences.foldLeft(0) { _ + _.size }

    val model = newModel()

    val ctrainer = params.mkClassifierTrainer(model.weights)
    val tagger = model.mkMultiTaggerTrainer(ctrainer)

    tagger.trainWithCache(trainSentences, params.numIters)

    ctrainer.postProcess // including lazy-updates of all weights

    model.reduceFeatures()
  }

  def setCategoryDictionary(
    sentences: Seq[GoldSuperTaggedSentence]): Unit =
    if (params.dict.useLexiconFiles) setCategoryDictionaryFromLexiconFiles()
    else setCategoryDictionaryFrom(sentences)

  def setCategoryDictionaryFromLexiconFiles(): Unit

  def setCategoryDictionaryFrom(sentences: Seq[GoldSuperTaggedSentence]) =
    dict.categoryDictionary.resetWithSentences(sentences, params.dict.unkThreathold)

  def newModel() = {
    val featureMap = new HashMap[Feature, Int]
    // val indexer = new ExactFeatureIndexer(featureMap)
    val weights = WeightVector.growable[Float]()

    new SuperTaggerModel(dict, featureMap, weights, mkFeatureExtractors())
  }

  // one can change features by modifying this function
  protected def mkFeatureExtractors() =
    new FeatureExtractorsWithCustomPoSLevel(
      params.feats,
      dict.getWord("@@BOS@@"),
      dict.getPoS("@@NULL@@"),
      _.secondWithConj.id)
  // new FeatureExtractors(param.feats, dict.getWord("@@BOS@@"), dict.getPoS("@@NULL@@"))

  def mkBank(_dict: Dictionary): CCGBank =
    CCGBank.select(params.bank, _dict)

  protected def mkDictionary(): Dictionary
}


object SuperTaggerTrainer {

  @Help(text="Params for super tagger trainer")
  case class Params(
    @Help(text="Save model path") model: File = new File("tagger.ser.gz"),
    @Help(text="Parameter eta of AdaGrad") eta: Double = 0.1,
    @Help(text="Parameter of step size function = t^(-a) (used in sgd)") stepSizeA: Double = 0.2,
    @Help(text="Reguralization strength in AdaGrad") lambda: Double = 0.000000005,
    @Help(text="Which training method is used at optimization? (adaGradL1|sgd)") trainAlg: String = "adaGradL1",
    @Help(text="# iters") numIters: Int = 10,
    @Help(text="Feature extractor") feat: FeatureExtractor = new DefaultExtractor(),
    @Help(text="Additional extractors") more: Seq[FeatureExtractor] = List(),
    // @Help(text="Feature extractors") feats: Seq[FeatureExtractor] = defaultExtractors,
    bank: Opts.BankInfo,
    dict: Opts.DictParams
    // // @Help(text="# training instances, -1 for all") trainSize: Int = -1,
  ) {
    def mkClassifierTrainer(_weights: WeightVec): OnlineLogLinearTrainer[Int] = {
      trainAlg match {
        case "sgd" =>
          new LogLinearSGD[Int](stepSizeA.toFloat) {
            override val weights = _weights
          }
        case "adaGradL1" =>
          new LogLinearAdaGradL1[Int](lambda.toFloat, eta.toFloat) {
            override val weights = _weights
          }
      }
    }

    def mkClassifier(_weights: WeightVec): LogLinearClassifier[Int] =
      new LogLinearClassifier[Int] {
        override val weights = _weights
      }

    def feats = feat +: more
  }
  def defaultExtractors = Seq(new DefaultExtractor)
}

class JapaneseSuperTaggerTrainer(val params: SuperTaggerTrainer.Params)
    extends SuperTaggerTrainer {

  def setCategoryDictionaryFromLexiconFiles(): Unit =
    JapaneseDictionary.setCategoryDictionaryFromLexicon(
      dict, ccgbank.lexicon.getPath, ccgbank.template.getPath)

  def mkDictionary() =
    new JapaneseDictionary(params.dict.categoryDictinoary)
}

class EnglishSuperTaggerTrainer(val params: SuperTaggerTrainer.Params)
    extends SuperTaggerTrainer {

  def setCategoryDictionaryFromLexiconFiles(): Unit =
    sys.error("English tagger does not support lexicon-based dictionary construction")

  def mkDictionary() = new SimpleDictionary
}
