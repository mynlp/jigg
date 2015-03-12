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

import jigg.ml.{Feature, LabeledFeature}
import jigg.nlp.ccg.lexicon.{Dictionary, JapaneseDictionary}

// all unlabeld feature must inherent this class
sealed trait SuperTaggingUnlabeledFeature extends Feature {
  type LabelType = Int
  override def assignLabel(label:LabelType) = SuperTaggingFeature(this, label)
}

// label = category's id
case class SuperTaggingFeature(override val unlabeled:SuperTaggingUnlabeledFeature,
                               override val label:Int) extends LabeledFeature[Int]

// features are classified by the way for stringizing; useful for pattern match when converting to strings.
trait FeatureWithoutDictionary extends SuperTaggingUnlabeledFeature {
  def mkString:String
}
case class RawFeature(unlabeledStr:String) extends FeatureWithoutDictionary {
  def mkString = unlabeledStr
}

trait FeatureOnDictionary extends SuperTaggingUnlabeledFeature {
  def mkString(dict:Dictionary):String
}
case class BiasFeature[T](tmpl:T) extends FeatureWithoutDictionary {
  override def mkString = concat(tmpl)
}
case class UnigramWordFeature[T](word:Int, tmpl:T) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getWord(word))
}
case class BigramWordFeature[T](word1:Int, word2:Int, tmpl:T) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getWord(word1), dict.getWord(word2))
}
case class TrigramWordFeature[T](word1:Int, word2:Int, word3:Int, tmpl:T) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getWord(word1), dict.getWord(word2), dict.getWord(word3))
}
case class UnigramPoSFeature[T](pos:Int, tmpl:T) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getPoS(pos))
}
case class BigramPoSFeature[T](pos1:Int, pos2:Int, tmpl:T) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getPoS(pos1), dict.getPoS(pos2))
}
case class TrigramPoSFeature[T](pos1:Int, pos2:Int, pos3:Int, tmpl:T) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getPoS(pos1), dict.getPoS(pos2), dict.getPoS(pos3))
}


// The below is a trial to speed-up deserialization, but it was slow

// case class ArrayFeature(val unlabeled:Seq[Int], val label:Int)

// object SuperTaggingFeature {
//   object Template extends Enumeration {
//     type Template = Value
//     val bias, w, wPrev1, wPrev2, wNext1, wNext2, wPrev2_wPrev1, wPrev1_w, w_wNext1, wNext1_wNext2, p, pPrev1, pPrev2, pNext1, pNext2, pPrev2_pPrev1, pPrev1_p, p_pNext1, pNext1_pNext2, pPrev2_pPrev1_p, pPrev1_p_pNext1, p_pNext1_pNext2 = Value
//   }
//   import Template.Template

//   object FeatureType {
//     val Bias = 0
//     val UnigramWord = 10; val BigramWord = 11; val TrigramWord = 12
//     val UnigramPoS = 20; val BigramPoS = 21; val TrigramPoS = 22
//   }
//   // object FeatureTemplate {
//   //   //val bias = 0
//   //   val w = 100
//   //   val wPrev1 = 101
//   //   val wPrev2 = 102
//   //   val wNext1 = 103
//   //   val wNext2 = 104
//   //   val wPrev2_wPrev1 = 105
//   //   val wPrev1_w = 106
//   //   val w_wNext1 = 107
//   //   val wNext1_wNext2 = 108

//   //   val p = 200
//   //   val pPrev1= 201
//   //   val pPrev2 = 202
//   //   val pNext1 = 203
//   //   val pNext2 = 204
//   //   val pPrev2_pPrev1 = 205
//   //   val pPrev1_p = 206
//   //   val p_pNext1 = 207
//   //   val pNext1_pNext2 = 208
//   //   val pPrev2_pPrev1_p = 209
//   //   val pPrev1_p_pNext1 = 210
//   //   val p_pNext1_pNext2 = 211
//   // }
//   import FeatureType._

//   def readFeature(feature:ArrayFeature, dict:Dictionary): String = {
//     def concat(items:Any*): String = items.mkString("_###_")
//     def v(i:Int) = feature.unlabeled(i)
//     def w(i:Int) = dict.getWord(v(i))
//     def p(i:Int) = dict.getPoS(v(i))
//     def label = dict.getCategory(feature.label)
//     def tmpl(i:Int) = Template(v(i))

//     v(0) match {
//       case Bias => concat("Bias", label)
//       case UnigramWord => concat(w(1), tmpl(2), label)
//       case UnigramPoS => concat(p(1), tmpl(2), label)
//       case BigramPoS => concat(p(1), p(2), tmpl(3), label)
//     }
//   }
//   def BiasFeature = Array(Bias) // empty template
//   def UnigramWordFeature(w:Int, tmpl:Template) = Array(UnigramWord, w, tmpl.id)
//   //def BigramWordFeature(w1:Int, w2:Int, tmpl:Int) = Array(w1, w2, tmpl)
//   def UnigramPoSFeature(p:Int, tmpl:Template) = Array(UnigramPoS, p, tmpl.id)
//   def BigramPoSFeature(p1:Int, p2:Int, tmpl:Template) = Array(BigramPoS, p1, p2, tmpl.id)
// }
