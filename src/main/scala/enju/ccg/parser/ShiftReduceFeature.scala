package enju.ccg.parser

import enju.ccg.ml.{Feature, LabeledFeature}
import enju.ccg.lexicon.{Dictionary, JapaneseDictionary}

sealed trait ShiftReduceUnlabeledFeature extends Feature {
  type LabelType = ActionLabel
  override def assignLabel(label:LabelType) = ShiftReduceFeature(this, label)
}

case class ShiftReduceFeature(override val unlabeled:ShiftReduceUnlabeledFeature,
                              override val label:ActionLabel) extends LabeledFeature[ActionLabel]

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

  case class Bias() extends FeatureWithoutDictionary {
    override def mkString = "bias"
  }
  case class WP[T](word:Int, pos:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, w(word,dict), p(pos,dict))
  }
  case class PC[T](pos:Int, category:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, p(pos,dict), c(category,dict))
  }
  case class WC[T](word:Int, category:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, w(word,dict), c(category,dict))
  }
  case class C[T](category:Int,tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, c(category, dict))
  }
  case class P[T](pos:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(dict:Dictionary) = concat(tmpl, p(pos,dict))
  }

  case class WCWC[T](w1:Int, c1:Int, w2:Int, c2:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), w(w2,d), c(c2,d))
  }
  case class CC[T](c1:Int, c2:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, c(c1,d), c(c2,d))
  }
  case class WCWP[T](w1:Int, c1:Int, w2:Int, p1:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), w(w2,d), p(p1,d))
  }
  case class WPC[T](w1:Int, p1:Int, c1:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), p(p1,d), c(c1,d))
  }
  case class WPCC[T](w1:Int, p1:Int, c1:Int, c2:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), p(p1,d), c(c1,d))
  }
  case class WCCC[T](w1:Int, c1:Int, c2:Int, c3:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), c(c2,d), c(c3,d))
  }
  case class PPC[T](p1:Int, p2:Int, c1:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, p(p1,d), p(p2,d), c(c1,d))
  }
  case class PPP[T](p1:Int, p2:Int, p3:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, p(p1,d), p(p2,d), p(p3,d))
  }
  case class WCC[T](w1:Int, c1:Int, c2:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, w(w1,d), c(c1,d), c(c2,d))
  }
  case class PCC[T](p1:Int, c1:Int, c2:Int, tmpl:T) extends FeatureOnDictionary {
    override def mkString(d:Dictionary) = concat(tmpl, p(p1,d), c(c1,d), c(c2,d))
  }
  
  object ZhangTemplate extends Enumeration {
    type ZhangTemplate = Value
    val wS0_pS0, cS0, pS0_cS0, wS0_cS0, wS1_pS1, cS1, pS1_cS1, wS1_cS1, pS2_cS2, wS2_cS2, pS3_cS3, wS3_cS3 = Value // 1

    val wQ0_pQ0, wQ1_pQ1, wQ2_pQ2, wQ3_pQ3 = Value // 2

    val pS0L_cS0L, wS0L_cS0L, pS0R_cS0R, wS0R_cS0R, pS0U_cS0U, wS0U_cS0U, pS1L_cS1L, wS1L_cS1L, pS1R_cS1R, wS1R_cS1R, pS1U_cS1U, wS1U_cS1U = Value // 3

    val wS0_cS0_wS1_cS1, wS1_cS0, wS0_cS1, cS0_cS1 = Value
    val wS0_cS0_wQ0_pQ0, wQ0_pQ0_cS0, wS0_cS0_pQ0, pQ0_cS0 = Value
    val wS1_cS1_wQ0_pQ0, wQ0_pQ0_cS1, wS1_cS1_pQ0, pQ0_cS1 = Value // 4
    
    val wS0_pQ0_cS0_cS1, wS1_pQ0_cS0_cS1, wQ0_pQ0_cS0_cS1 = Value
    val pQ0_cS0_cS1, pS0_pS1_pQ0, wS0_pQ0_pQ1_cS0, wQ0_pQ0_pQ1_cS0, wQ1_pQ0_pQ1_cS0 = Value
    val pQ0_pQ1_cS0, pS0_pQ0_pQ1, wS0_cS0_cS1_cS2, wS1_cS0_cS1_cS2, wS2_cS0_cS1_cS2 = Value
    val cS0_cS1_cS2, pS0_pS1_S2p = Value // 5
    
    val cS0_cS0H_cS0L, c_S0_cS0H_cS0R, cS1_cS1H_cS1R = Value
    val pQ0_cS0_cS0R, wQ0_cS0_cS0R, cS0_cS0L_cS1, wS1_cS0_cS0L, cS0_cS1_cS1R, wS0_cS1_cS1R = Value
  }
}

