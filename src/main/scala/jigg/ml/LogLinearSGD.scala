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

abstract class LogLinearSGD[L](val a: Float) extends OnlineLogLinearTrainer[L] {

  def stepSize = Math.pow(time + 1, -a).toFloat // avoid the overflow
  def updateExampleWeights(e: Example[L], gold: L, derivative: Float): Unit = {
    val dw = stepSize * derivative
    val feats = e.featVec
    var i = 0
    while (i < feats.size) {
      weights(feats(i)) += dw
      i += 1
    }
  }
}

class FixedLogLinearSGD[L](val weightArray: Array[Float], a: Float) extends LogLinearSGD(a) {

  override val weights = new FixedWeightVector(weightArray)
}
