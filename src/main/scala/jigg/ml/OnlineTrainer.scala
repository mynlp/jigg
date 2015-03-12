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

/** A trait which support parameter update, and the interface of Classifier.
  * Currently two subclasses exists: OnlineLoglinearTrainer is used for log-linear models, while Perceptron is used to train the perceptron including structured perceptron with beam-search.
  */
trait OnlineTrainer[L] extends Classifier[L] {
  def update(examples: Seq[Example[L]], gold:L): Unit
  def postProcess: Unit = Unit
}
