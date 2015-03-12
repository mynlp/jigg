package jigg.ml

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

import scala.collection.mutable.ArrayBuffer

trait Perceptron[L] extends LinearClassifier[L] with OnlineTrainer[L] {

  def averageWeights: WeightVector[Float]

  var c = 1.0F

  override def update(examples: Seq[Example[L]], gold: L): Unit = {
    val pred = predict(examples)._1
    if (pred != gold) {
      var i = 0
      while (i < examples.size) {
        val label = examples(i).label
        if (label == pred) updateFeatureWeighs(examples(i).featVec, -1.0F)
        else if (label == gold) updateFeatureWeighs(examples(i).featVec, 1.0F)
        i += 1
      }
    }
    c += 1.0F
  }
  def updateFeatureWeighs(featVec: Array[Int], scale: Float): Unit = featVec.foreach { f =>
    weights(f) += scale
    averageWeights(f) += scale * c
  }
  def update(predFeatVec:Array[Int], goldFeatVec:Array[Int]): Unit = {
    updateFeatureWeighs(predFeatVec, -1.0F)
    updateFeatureWeighs(goldFeatVec, 1.0F)
    c += 1.0F
  }
  def takeAverage: Unit = (0 until weights.size) foreach { i =>
    weights(i) -= averageWeights(i) / c
  }
}

class FixedPerceptron[L](val weightArray: Array[Float]) extends Perceptron[L] {

  override val weights = new FixedWeightVector(weightArray)
  override val averageWeights = new FixedWeightVector(new Array[Float](weights.size))
}

class GrowablePerceptron[L](val weightArray: ArrayBuffer[Float]) extends Perceptron[L] {

  override val weights = new GrowableWeightVector(weightArray)
  override val averageWeights = WeightVector.growable[Float](weights.size)
}
