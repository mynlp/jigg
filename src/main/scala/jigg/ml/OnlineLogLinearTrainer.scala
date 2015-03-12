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

/** This trait exploits the common procedure in trainers of log-linear models.
  */
trait OnlineLogLinearTrainer[L] extends OnlineTrainer[L] with LogLinearClassifier[L] {
  var time: Int = 0

  override def update(examples: Seq[Example[L]], gold:L): Unit = {
    val dist = labelProbs(examples)
    var i = 0
    while (i < examples.size) {
      val e = examples(i)
      val p = dist(i)
      val derivative = if (e.label == gold) (1 - p) else -p
      updateExampleWeights(e, gold, derivative)
      i += 1
    }
    reguralizeWeights(examples)
    time += 1
  }
  def updateExampleWeights(e: Example[L], gold: L, derivative: Float): Unit
  def reguralizeWeights(examples: Seq[Example[L]]): Unit = {} // Some algorithms reguralize weights after temporalily updating the values and this method defines that postprocessing. See LogLinearSGDCumulativeL1 for example.
}
