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

import tagger.{LF=>Feature, MaxEntMultiTagger, MaxEntMultiTaggerTrainer, FeatureExtractors}
import lexicon._
import jigg.ml._

import scala.collection.mutable.HashMap

case class SuperTaggerModel(
  dict: Dictionary,
  featureMap: HashMap[Feature, Int],
  weights: WeightVec,
  extractors: FeatureExtractors) { self =>

  def reduceFeatures(): SuperTaggerModel = {

    val buffer = weights.asInstanceOf[GrowableWeightVector[Float]].array // 0 1.0 2.0 0 0 1.0 ...
    val activeIdxs = buffer.zipWithIndex filter (_._1 != 0) map (_._2)  // 1 2 5
    println(s"# features reduced from ${buffer.size} to ${activeIdxs.size}")
    val idxMap = activeIdxs.zipWithIndex.toMap // {1->0, 2->1 5->2}

    val newFeatureMap = featureMap collect {
      case (f, oldIdx) if idxMap.isDefinedAt(oldIdx) => (f, idxMap(oldIdx))
    }
    val newWeights = new FixedWeightVector[Float](activeIdxs.map(buffer).toArray)

    this copy (featureMap = newFeatureMap, weights = newWeights)
  }

  def mkMultiTaggerTrainer(classifierTrainer: OnlineLogLinearTrainer[Int]) =
    new MaxEntMultiTaggerTrainer(mkIndexer(), extractors, classifierTrainer, dict)

  def mkMultiTagger() =
    new MaxEntMultiTagger(mkIndexer(), extractors, mkClassifier(), dict)

  def mkClassifier() = new LogLinearClassifier[Int] {
    override val weights = self.weights
  }

  private def mkIndexer() = new ExactFeatureIndexer(featureMap)
}

object SuperTaggerModel {

  def saveTo(path: String, model: SuperTaggerModel) = {
    System.err.println("Saving tagger model to " + path)
    val os = jigg.util.IOUtil.openBinOut(path)
    os.writeObject(model)
    os.close
  }

  def loadFrom(path: String): SuperTaggerModel = {
    jigg.util.LogUtil.track("Loading supertagger model ...") {
      val in = jigg.util.IOUtil.openBinIn(path)
      val model = in.readObject.asInstanceOf[SuperTaggerModel]
      in.close
      model
    }
  }
}
