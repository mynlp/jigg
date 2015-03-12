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

trait FeatureBase

// Unlabeld feature, but not limited to: user may want to create features always with label (e.g., in structured classification exam). In such case, please include label to this class and ignore LabeldFeature.
trait Feature extends FeatureBase {
  type LabelType
  type DictionaryType
  def assignLabel(label:LabelType): LabeledFeature[LabelType]
  def concat(items:Any*): String = items.mkString("_###_")
}

trait LabeledFeature[L] extends FeatureBase {
  def unlabeled: Feature
  def label: L
}
