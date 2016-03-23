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
import parser.{
  ActionLabel,
  CFGRule,
  EnglishHeadFinder,
  FeatureExtractor,
  FeatureExtractors,
  KBestDecoder,
  LF => Feature,
  HeadFinder,
  JapaneseHeadFinder,
  ZhangExtractor
}
import jigg.ml.{HashedFeatureIndexer, FixedPerceptron}

import breeze.config.{CommandLineParser, Help}

import scala.io.Source
import scala.collection.mutable.HashMap

import java.io.{File, FileWriter}


trait ParserTrainer {

  def params: Params

  type Params = ParserTrainer.Params

  val taggerModel = SuperTaggerModel.loadFrom(params.taggerModel.getPath)
  val tagger = new SuperTaggerRunner(taggerModel, params.tagger)

  val dict = taggerModel.dict

  val bank = CCGBank.select(params.bank, dict)

  def trainAndSave() = {
    val model = train()
    ParserModel.saveTo(params.model.getPath, model)
  }

  def train(): ParserModel = {

    val parseTrees = bank.trainTrees
    val sentences = bank sentences parseTrees // TODO: converter should not be in the bank?
    val derivations = bank derivations parseTrees

    val trainingSentences = assignSuperTags(sentences)

    System.err.println("Extracting CFG rules from all derivations ...")
    val rule = CFGRule.extractRulesFromDerivations(derivations, mkHeadFinder(parseTrees))
    System.err.println("Done.")

    val indexer = mkIndexer()
    val weights = new Array[Float](indexer.size)
    val model = ParserModel(taggerModel, indexer, weights, mkFeatureExtractors(), rule)

    val perceptron = new FixedPerceptron[ActionLabel](weights)

    val decoder = model.mkTrainDecoder(params.beam, perceptron)

    System.err.println("Training start!")
    (0 until params.numIters) foreach { i =>
      val correct = decoder trainSentences (trainingSentences, derivations)
      System.err.println("Accuracy (" + i + "): " + correct.toDouble / sentences.size.toDouble + " [" + correct + "]")
    }
    perceptron.takeAverage
    model
  }

  def assignSuperTags(sentences: Array[GoldSuperTaggedSentence]): Array[TrainSentence] = {
    System.err.println("Super tagging: assign candidate categories to sentences ...")
    val taggedSentences = tagger.assignKBests(sentences).toArray
    System.err.println("done.")
    val sumCandidates = taggedSentences.foldLeft(0) { case (sum, s) => sum + s.candSeq.map(_.size).sum }
    val numInstances = sentences.foldLeft(0) { _ + _.size }
    System.err.println("# average of candidate labels after super-tagging: " + sumCandidates.toDouble / numInstances.toDouble)

    taggedSentences.map { _.pickUpGoldCategory }.toArray // for training
  }

  protected def mkFeatureExtractors() =
    new FeatureExtractors(
      params.feats,
      _.secondWithConj.id)

  protected def mkIndexer() = HashedFeatureIndexer[Feature](params.featureSize)

  def mkHeadFinder(trees: Seq[ParseTree[NodeLabel]]): HeadFinder
}

object ParserTrainer {

  @Help(text="Params for shift-reduce parser trainer")
  case class Params(
    @Help(text="Save model path") model: File = new File("parser.ser.gz"),
    @Help(text="Supertagger model path") taggerModel: File = new File("tagger.ser.gz"),
    @Help(text="# iters") numIters: Int = 10,
    @Help(text="Feature extractor") feat: FeatureExtractor = new ZhangExtractor(),
    @Help(text="Additional extractors") more: Seq[FeatureExtractor] = List(),
    @Help(text="# training instances, -1 for all") trainSize: Int = -1,
    @Help(text="Size of hashed feature") featureSize: Int = 2 << 23,
    @Help(text="Beam size") beam: Int = 32,
    bank: Opts.BankInfo,
    tagger: SuperTaggerRunner.Params = new SuperTaggerRunner.Params()
  ) {
    def feats = feat +: more
  }
}

class JapaneseParserTrainer(val params: ParserTrainer.Params) extends ParserTrainer {

  def mkHeadFinder(trees: Seq[ParseTree[NodeLabel]]) = JapaneseHeadFinder

}


class EnglishParserTrainer(val params: ParserTrainer.Params) extends ParserTrainer {

  def mkHeadFinder(trees: Seq[ParseTree[NodeLabel]]) = EnglishHeadFinder.createFromParseTrees(trees)

}
