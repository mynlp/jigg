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

trait Classifier[L] {

  protected val weights: WeightVector[Float]

  def predict(examples: Seq[Example[L]]): (L, Float)
}

trait LinearClassifier[L] extends Classifier[L] {

  override def predict(examples: Seq[Example[L]]): (L, Float) =
    if (examples.isEmpty) (null.asInstanceOf[L], 0F)
    else examples.map { e => (e.label, featureScore(e.featVec)) }.maxBy(_._2)

  def featureScore(feature: Array[Int]): Float = {
    var a = 0F
    var i = 0
    while (i < feature.size) {
      a += weight(feature(i))
      i += 1
    }
    a
  }
  /** Control the behavior of the access to weight.
    * You *MUST* use this method to access weight inside the classifier, and *NEVER* call like weights(i) directly (except updating the value)
    * This is because in some classifiers, such as AdaGradL1, the values must be preprocessed (e.g., lazy update) before used.
    * You can add such a preprocessing by overriding this method in a subclass.
    */
  protected def weight(idx: Int): Float = weights(idx)
}

/** A classifier in which weight vector backbone is implemented by array, hopefully faster than growable counterpart.
  */
class FixedClassifier[L](val array: Array[Float]) extends LinearClassifier[L] {
  override val weights = new FixedWeightVector(array)
}
