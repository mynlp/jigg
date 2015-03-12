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

/** Augument LinearClassifier with a method to return label probabilities.
  * (implies loss function of log loss)
  */
trait LogLinearClassifier[L] extends LinearClassifier[L] {
  val weights: WeightVector[Float]

  def labelProbs(examples: Seq[Example[L]]): Array[Float] = {
    val unnormalized: Array[Float] = examples.map { e =>
      val p = Math.exp(featureScore(e.featVec)).toFloat
      if (p < 1e-100) 1e-100F else p
    }.toArray
    val z = unnormalized.sum
    unnormalized.map(_ / z)
  }
}

class FixedLogLinerClassifier[L](val weightArray: Array[Float]) extends LogLinearClassifier[L] {
  override val weights = new FixedWeightVector(weightArray)
}
