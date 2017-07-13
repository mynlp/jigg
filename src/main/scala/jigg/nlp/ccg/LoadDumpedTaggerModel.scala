package jigg.nlp.ccg

import lexicon._
import tagger.{LF => Feature, _}
import jigg.ml._

import breeze.config.{CommandLineParser, Help}

import scala.collection.mutable.{HashMap, ArrayBuffer}

import java.io.File

object LoadDumpedTaggerModel {

  case class Params(
    @Help(text="Load model path") model: File = new File(""),
    @Help(text="Save model path") output: File = new File(""),
    @Help(text="Feature extractor") feat: FeatureExtractor = new DefaultExtractor(),
    @Help(text="Additional extractors") more: Seq[FeatureExtractor] = List(),
    bank: Opts.BankInfo, // these settings should be consistent with
    dict: Opts.DictParams // the dumped model on the (previous) version.
  )

  def main(args: Array[String]) = {

    val params = CommandLineParser.readIn[Params](args)

    val model = defaultModel(params)

    val (featureMap, weights) = readModel(params.model)
    val newModel = model.copy(featureMap = featureMap, weights = weights)

    SuperTaggerModel.saveTo(params.output.getPath, newModel)
  }

  // obtain the default model (with no learned params) using SuperTaggerTrainer
  def defaultModel(params: Params) = {
    val trainParams = new SuperTaggerTrainer.Params(
      new File(""), 0.1, 0.2, 0.000000005, "adaGradL1", 20, params.feat, params.more,
      params.bank, params.dict
    )
    val trainer = new JapaneseSuperTaggerTrainer(trainParams)
    val trainSentences = trainer.ccgbank.trainSentences

    trainer.setCategoryDictionary(trainSentences)

    trainer.newModel()
  }

  def readModel(input: File): (HashMap[Feature, Int], WeightVector[Float]) = {
    val in = jigg.util.IOUtil.openIterator(input.getPath)

    val featureMap = new HashMap[Feature, Int]
    val weights = new ArrayBuffer[Float]

    // Each line looks like:
    // SuperTaggingFeature(BigramPoSFeature(58,116,pPrev2_pPrev1),187) 578695
    def addFeat(line: String): Unit = {
      val items = line.split(" ")
      val featStr = items(0)
      val idx = items(1).toInt

      val feat = toFeature(featStr)
      featureMap += feat -> idx
    }

    def addWeight(line: String): Unit = {
      weights += line.toFloat
    }

    var addFun: String=>Unit = addFeat

    for (line <- in) {
      if (line.isEmpty) addFun = addWeight
      else addFun(line)
    }
    (featureMap, new FixedWeightVector(weights.toArray))
  }

  // featStr looks like:
  // SuperTaggingFeature(BigramPoSFeature(58,116,pPrev2_pPrev1),187)
  def toFeature(featStr: String) = {
    val firstP = featStr.indexOf('(', 0)
    val secondP = featStr.indexOf('(', firstP+1)
    val firstE = featStr.indexOf(')', secondP)

    val basename = featStr.substring(0, firstP)
    assert(basename == "SuperTaggingFeature")
    val name = featStr.substring(firstP+1, secondP)
    val argsStr = featStr.substring(secondP+1, firstE)
    val args: Array[String] = argsStr.split(",")
    val label = featStr.substring(firstE+2, featStr.size-1)

    val unlabeled = name match {
      case "RawFeature" =>
        RawFeature(argsStr)
      case "BiasFeature" =>
        BiasFeature(convT(args(0)))
      case "UnigramWordFeature" =>
        UnigramWordFeature(args(0).toInt, convT(args(1)))
      case "BigramWordFeature" =>
        BigramWordFeature(args(0).toInt, args(1).toInt, convT(args(2)))
      case "TrigramWordFeature" =>
        TrigramWordFeature(args(0).toInt, args(1).toInt, args(2).toInt, convT(args(3)))
      case "UnigramPoSFeature" =>
        UnigramPoSFeature(args(0).toInt, convT(args(1)))
      case "BigramPoSFeature" =>
        BigramPoSFeature(args(0).toInt, args(1).toInt, convT(args(2)))
      case "TrigramPoSFeature" =>
        TrigramPoSFeature(args(0).toInt, args(1).toInt, args(2).toInt, convT(args(3)))
    }
    unlabeled.assignLabel(label.toInt)
  }

  def convT(tmpl: String) = {
    import Template.Template
    tmpl match {
      case "bias" => Template.bias
      case "w" => Template.w
      case "wPrev1" => Template.wPrev1
      case "wPrev2" => Template.wPrev2
      case "wNext1" => Template.wNext1
      case "wNext2" => Template.wNext2
      case "wPrev2_wPrev1" => Template.wPrev2_wPrev1
      case "wPrev1_w" => Template.wPrev1_w
      case "w_wNext1" => Template.w_wNext1
      case "wNext1_wNext2" => Template.wNext1_wNext2
      case "p" => Template.p
      case "pPrev1" => Template.pPrev1
      case "pPrev2" => Template.pPrev2
      case "pNext1" => Template.pNext1
      case "pNext2" => Template.pNext2
      case "pPrev2_pPrev1" => Template.pPrev2_pPrev1
      case "pPrev1_p" => Template.pPrev1_p
      case "p_pNext1" => Template.p_pNext1
      case "pNext1_pNext2" => Template.pNext1_pNext2
      case "pPrev2_pPrev1_p" => Template.pPrev2_pPrev1_p
      case "pPrev1_p_pNext1" => Template.pPrev1_p_pNext1
      case "p_pNext1_pNext2" => Template.p_pNext1_pNext2
    }
  }
}
