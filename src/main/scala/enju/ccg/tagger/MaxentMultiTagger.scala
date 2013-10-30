package enju.ccg.tagger

import enju.ccg.lexicon._
import enju.ccg.util.Indexer
import enju.ccg.ml.{LogisticSGD, Example}

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import scala.util.Random

class MaxentMultiTagger(indexer: Indexer[LF],
                        extractors: FeatureExtractors,
                        classifier: LogisticSGD[Int],
                        dict: Dictionary) {

  case class TrainingInstance(items:Seq[Example[Int]], goldLabel:Int)

  def trainWithCache(sentences:Seq[GoldSuperTaggedSentence], numIters:Int) = {
    val cachedInstances:Seq[Option[TrainingInstance]] = sentences.zipWithIndex.flatMap { case (sentence, j) => {
      (0 until sentence.size) map { i => getTrainingInstance(sentence, i, sentence.cat(i).id) }
    }}
    (0 until numIters).foreach { j => {
      val shuffledInstances = Random.shuffle(cachedInstances)
      var correct = 0
      shuffledInstances.foreach {
        _ foreach { e => if (trainInstance(e)) correct += 1 }
      }
      println("accuracy (" + j + "): " + (correct.toDouble / shuffledInstances.size.toDouble))
    }}
  }

  def trainInstance(instance:TrainingInstance):Boolean = {
    val pred = classifier.predict(instance.items).getP1
    classifier.update(instance.items, instance.goldLabel)
    pred == instance.goldLabel
  }

  def getTrainingInstance(sentence:TaggedSentence, i:Int, goldLabel:Int):Option[TrainingInstance] = {
    val candidateLabels = dict.getCategoryCandidates(sentence.word(i), sentence.pos(i)) map { _.id }
    if (candidateLabels.isEmpty) None
    val unlabeled = extractors.extractUnlabeledFeatures(sentence, i)
    Some(unlabeledToTrainingInstance(unlabeled, candidateLabels, goldLabel))
  }
  def unlabeledToTrainingInstance(features:Seq[UF], candidateLabels:Seq[Int], goldLabel:Int):TrainingInstance = {
    val items:Seq[Example[Int]] = candidateLabels map {
      label => {
        // TODO: this is the most lower level of the algorithm; might be considerable to optimize with Array
        val indexes:Seq[Int] = features map { unlabeled => SuperTaggingFeature(unlabeled, label) } map { labeled => indexer.indexOf(labeled) }
        var e = new Example(label)
        e.setFeatureQuick(indexes.toArray); e
      }
    }
    TrainingInstance(items, goldLabel)
  }
  
  def assignTagCandidates(sentence:TaggedSentence, beta:Double):CandAssignedSentence = {
    val candSeq:Seq[Seq[Category]] = (0 until sentence.size).map { 
      i => getTrainingInstance(sentence, i, 0) match {
        case Some(TrainingInstance(items, _)) => {
          val dist = classifier.calcLabelProbs(items)
          val (max, argmax) = dist.zipWithIndex.foldLeft((0.0, 0)) { case ((max, argmax), (p,i)) => if (p > max) (p, i) else (max, argmax) }
          val threshold = max * beta
          items.zip(dist).filter { case (e, p) => p > threshold }.map {
            case (e, _) => dict.getCategory(e.getLabel)
          }
        }
        case None => Nil
      }
    }
    sentence.assignCandidates(candSeq)
  }
}
