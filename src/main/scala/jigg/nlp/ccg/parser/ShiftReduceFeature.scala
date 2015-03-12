package jigg.nlp.ccg.parser

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

sealed trait ShiftReduceUnlabeledFeature extends Feature {
  type LabelType = ActionLabel
  override def assignLabel(label:LabelType) = ShiftReduceFeature(this, label)
  //lazy val hashCode_ = hashCode
}

@SerialVersionUID(6076803500331881365L)
case class ShiftReduceFeature(override val unlabeled:ShiftReduceUnlabeledFeature,
                              override val label:ActionLabel) extends LabeledFeature[ActionLabel] {
  // The below tried to reduce the cost of feature calcuations; but the result was negative
  //override def hashCode = scala.util.hashing.MurmurHash3.productHash((unlabeled.hashCode_, label))
}

// {
// TODO: to implement this to separate concatination of "feature" and "label" from the interface level.
//   def mkString(dict: Dictionary)
// }

trait FeatureWithoutDictionary extends ShiftReduceUnlabeledFeature {
  def mkString: String
}
// use this if you want to do feature-engeneering without adding FeatureTypes for your new features
case class RawFeature(unlabeledStr:String) extends FeatureWithoutDictionary {
  def mkString = unlabeledStr
}

trait FeatureOnDictionary extends ShiftReduceUnlabeledFeature {
  def mkString(dict:Dictionary): String
}

object FeatureTypes {
  def w(wordId:Int, dict:Dictionary) = dict.getWord(wordId)
  def p(posId:Int, dict:Dictionary) = dict.getPoS(posId)
  def c(categoryId:Int, dict:Dictionary) = dict.getCategory(categoryId)

  case class Empty[T](tmpl: T) extends FeatureWithoutDictionary {
    override def mkString = tmpl.toString
  }
  case class Bias() extends FeatureWithoutDictionary {
    override def mkString = "bias"
  }
  case object FinishBias extends FeatureWithoutDictionary {
    override def mkString = "finishBias"
  }
  case class WP[T](word:Char, pos:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, w(word,dict), p(pos,dict))
  }
  case class PC[T](pos:Short, category:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, p(pos,dict), c(category,dict))
  }
  case class WC[T](word:Char, category:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, w(word,dict), c(category,dict))
  }
  case class C[T](category:Short,tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, c(category, dict))
  }
  case class P[T](pos:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, p(pos,dict))
  }

  case class WCWC[T](w1:Char, c1:Short, w2:Char, c2:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), w(w2,d), c(c2,d))
  }
  case class CC[T](c1:Short, c2:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, c(c1,d), c(c2,d))
  }
  case class WCWP[T](w1:Char, c1:Short, w2:Char, p1:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), w(w2,d), p(p1,d))
  }
  case class WPC[T](w1:Char, p1:Short, c1:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), p(p1,d), c(c1,d))
  }
  case class WPCC[T](w1:Char, p1:Short, c1:Short, c2:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), p(p1,d), c(c1,d), c(c2,d))
  }
  case class WPPC[T](w1:Char, p1:Short, p2:Short, c1:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), p(p1,d), p(p2,d), c(c1,d))
  }
  case class WCCC[T](w1:Char, c1:Short, c2:Short, c3:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), c(c2,d), c(c3,d))
  }
  case class PPC[T](p1:Short, p2:Short, c1:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, p(p1,d), p(p2,d), c(c1,d))
  }
  case class PPP[T](p1:Short, p2:Short, p3:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, p(p1,d), p(p2,d), p(p3,d))
  }
  case class WCC[T](w1:Char, c1:Short, c2:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), c(c2,d))
  }
  case class PCC[T](p1:Short, c1:Short, c2:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, p(p1,d), c(c1,d), c(c2,d))
  }
  case class CCC[T](c1:Short, c2:Short, c3:Short, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, c(c1,d), c(c2,d), c(c3,d))
  }

  object ZhangTemplate extends Enumeration {
    type ZhangTemplate = Value
    val wS0_pS0, cS0, pS0_cS0, wS0_cS0 = Value
    val wS1_pS1, cS1, pS1_cS1, wS1_cS1 = Value
    val pS2_cS2, wS2_cS2 = Value
    val pS3_cS3, wS3_cS3 = Value // 1

    val wQ0_pQ0, wQ1_pQ1, wQ2_pQ2, wQ3_pQ3 = Value // 2

    val pS0L_cS0L, wS0L_cS0L, pS0R_cS0R, wS0R_cS0R, pS0U_cS0U, wS0U_cS0U, pS1L_cS1L, wS1L_cS1L, pS1R_cS1R, wS1R_cS1R, pS1U_cS1U, wS1U_cS1U = Value // 3

    val wS0_cS0_wS1_cS1, wS1_cS0, wS0_cS1, cS0_cS1 = Value
    val wS0_cS0_wQ0_pQ0, wQ0_pQ0_cS0, wS0_pQ0_cS0, pQ0_cS0 = Value
    val wS1_cS1_wQ0_pQ0, wQ0_pQ0_cS1, wS1_pQ0_cS1, pQ0_cS1 = Value // 4

    val wS0_pQ0_cS0_cS1, wS1_pQ0_cS0_cS1, wQ0_pQ0_cS0_cS1 = Value
    val pQ0_cS0_cS1, pS0_pS1_pQ0, wS0_pQ0_pQ1_cS0, wQ0_pQ0_pQ1_cS0, wQ1_pQ0_pQ1_cS0 = Value
    val pQ0_pQ1_cS0, pS0_pQ0_pQ1, wS0_cS0_cS1_cS2, wS1_cS0_cS1_cS2, wS2_cS0_cS1_cS2 = Value
    val cS0_cS1_cS2, pS0_pS1_pS2 = Value // 5

    val cS0_cS0H_cS0L, cS0_cS0H_cS0R, cS1_cS1H_cS1R = Value
    val pQ0_cS0_cS0R, wQ0_cS0_cS0R, cS0_cS0L_cS1, wS1_cS0_cS0L, cS0_cS1_cS1R, wS0_cS1_cS1R = Value
  }

  object FinishTemplate extends Enumeration {
    type FinishTemplate = Value
    val cS0 = Value
    val cS1, no_cS1 = Value
    val cS2, no_cS2 = Value

    val pS0_cS0 = Value
  }
}
