package jigg.nlp.ccg.tagger

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

import jigg.nlp.ccg.lexicon._
//import jigg.nlp.ccg.util.Indexer
import jigg.ml.{LogLinearClassifier, OnlineLogLinearTrainer, Example, FeatureIndexer}

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.util.Random

class MaxEntMultiTagger(
  val indexer: FeatureIndexer[LF],
  val extractors: FeatureExtractors,
  val classifier: LogLinearClassifier[Int],
  val dict: Dictionary) {

  val reusableFeatureIdxs = new ArrayBuffer[Int]

  trait Instance {
    def items:Array[Example[Int]]
    def goldLabel:Int = 0
  }
  case class TestInstance(override val items:Array[Example[Int]]) extends Instance

  def getTestInstance(sentence:TaggedSentence, i:Int): TestInstance = {
    val candidateLabels = dict.getCategoryCandidates(sentence.base(i), sentence.pos(i)) map { _.id }
    val unlabeled = extractors.extractUnlabeledFeatures(sentence, i).toArray
    unlabeledToTestInstance(unlabeled, candidateLabels)
  }

  def unlabeledToTestInstance(features:Array[UF], candidateLabels:Array[Int]) =
    TestInstance(getItems(features, candidateLabels, { f => indexer.get(f) }))

  def getItems(features:Array[UF], candidateLabels:Array[Int], f2index:(LF => Int)): Array[Example[Int]] = candidateLabels map { label =>
    //val indexes = new Array[Int](features.size)
    reusableFeatureIdxs.clear
    var i = 0
    while (i < features.size) {
      val f = f2index(features(i).assignLabel(label))
      if (f >= 0) reusableFeatureIdxs += f // discard -1 = unknown features
      i += 1
    }
    Example(reusableFeatureIdxs.toArray, label)
  }
  def candSeq(sentence:TaggedSentence, beta:Double, maxK: Int): Array[Seq[Category]] =
    (0 until sentence.size).map { i =>
      val instance = getTestInstance(sentence, i)
      val dist = classifier.labelProbs(instance.items)
      val (max, argmax) = dist.zipWithIndex.foldLeft((0.0, 0)) { case ((max, argmax), (p,i)) => if (p > max) (p, i) else (max, argmax) }
      val threshold = max * beta
      val numTake = if (maxK == -1) dist.size else maxK
      instance.items.zip(dist).filter { case (e, p) => p >= threshold }.take(numTake).map {
        case (e, _) => dict.getCategory(e.label)
      }.toSeq
    }.toArray
}

class MaxEntMultiTaggerTrainer(
  indexer: FeatureIndexer[LF],
  extractors: FeatureExtractors,
  override val classifier: OnlineLogLinearTrainer[Int],
  dict: Dictionary) extends MaxEntMultiTagger(indexer, extractors, classifier, dict) {

  case class TrainingInstance(override val items:Array[Example[Int]],
                              override val goldLabel:Int) extends Instance

  def trainWithCache(sentences:Seq[GoldSuperTaggedSentence], numIters:Int) = {
    println("feature extraction start...")
    val cachedInstances:Seq[Option[Instance]] = sentences.zipWithIndex.flatMap { case (sentence, j) =>
      if (j % 100 == 0) print(j + "\t/" + sentences.size + " done \r")
        (0 until sentence.size) map { i => getTrainingInstance(sentence, i, sentence.cat(i).id) }
    }
    println("\ndone.")
    val numEffectiveInstances = cachedInstances.filter(_ != None).size

    println("# all training instances: " + numEffectiveInstances + "; " + (cachedInstances.size - numEffectiveInstances) + " instances were discarded by look-up errors of candidate categories.")
    println("# features: " + indexer.size)
    println("# average of candidate labels: " + (cachedInstances.foldLeft(0) {
      case (sum, o) => sum + o.map { _.items.size }.getOrElse(0) } ).toDouble / numEffectiveInstances.toDouble )

    // import scala.collection.immutable.TreeMap
    // var labelNum2Count = new TreeMap[Int,Int]
    // cachedInstances.foreach { _.foreach { _.items.size match { case k => labelNum2Count += k -> (labelNum2Count.getOrElse(k, 0) + 1) } } }
    // println(labelNum2Count)

    (0 until numIters).foreach { j =>
      val shuffledInstances = Random.shuffle(cachedInstances)
      var correct = 0
      shuffledInstances.foreach {
        _ foreach { e => if (trainInstance(e)) correct += 1 }
      }
      println("accuracy (" + j + "): " + (correct.toDouble / numEffectiveInstances.toDouble))
    }
  }
  def trainInstance(instance:Instance):Boolean = {
    val pred = classifier.predict(instance.items)._1
    classifier.update(instance.items, instance.goldLabel)
    pred == instance.goldLabel
  }
  def getTrainingInstance(sentence:TaggedSentence, i:Int, goldLabel:Int): Option[TrainingInstance] = {
    val candidateLabels = dict.getCategoryCandidates(sentence.base(i), sentence.pos(i)) map { _.id }
    if (candidateLabels.isEmpty || !candidateLabels.contains(goldLabel)) None else {
      val unlabeled = extractors.extractUnlabeledFeatures(sentence, i).toArray
      Some(unlabeledToTrainingInstance(unlabeled, candidateLabels, goldLabel))
    }
  }
  def unlabeledToTrainingInstance(features:Array[UF], candidateLabels:Array[Int], goldLabel:Int) =
    TrainingInstance(getItems(features, candidateLabels, { f => indexer.getIndex(f) }), goldLabel)
}
