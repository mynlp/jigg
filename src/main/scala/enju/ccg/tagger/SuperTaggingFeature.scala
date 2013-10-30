package enju.ccg.tagger
import enju.ccg.ml.{Feature, LabeledFeature}
import enju.ccg.lexicon.{Dictionary, JapaneseDictionary}

// all unlabeld feature must inherent this class
sealed trait SuperTaggingUnlabeledFeature extends Feature {
  type LabelType = Int
  override def assignLabel(label:LabelType) = SuperTaggingFeature(this, label)
}

// label = category's id
case class SuperTaggingFeature(override val unlabeled:SuperTaggingUnlabeledFeature,
                               override val label:Int) extends LabeledFeature[Int]

object Template extends Enumeration {
  type Template = Value
  val w, wPrev1, wPrev2, wNext1, wNext2, wPrev2_wPrev1, wPrev1_w, w_wNext1, wNext1_wNext2, p, pPrev1, pPrev2, pNext1, pNext2, pPrev2_pPrev1, pPrev1_p, p_pNext1, pNext1_pNext2, pPrev2_pPrev1_p, pPrev1_p_pNext1, p_pNext1_pNext2 = Value
}

import Template.Template

// features are classified by the way for stringizing; useful for pattern match when converting to strings.
trait FeatureOnDictionary extends SuperTaggingUnlabeledFeature {
  def mkString(dict:Dictionary):String
}
trait FeatureOnJapaneseDictionary extends SuperTaggingUnlabeledFeature {
  def mkString(dict:JapaneseDictionary):String
}

case class UnigramWordFeature(word:Int, tmpl:Template) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getWord(word))
}
case class BigramWordFeature(word1:Int, word2:Int, tmpl:Template) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getWord(word1), dict.getWord(word2))
}
case class TrigramWordFeature(word1:Int, word2:Int, word3:Int, tmpl:Template) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getWord(word1), dict.getWord(word2), dict.getWord(word3))
}
case class UnigramPoSFeature(pos:Int, tmpl:Template) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getPoS(pos))
}
case class BigramPoSFeature(pos1:Int, pos2:Int, tmpl:Template) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getPoS(pos1), dict.getPoS(pos2))
}
case class TrigramPoSFeature(pos1:Int, pos2:Int, pos3:Int, tmpl:Template) extends FeatureOnDictionary {
  override def mkString(dict:Dictionary) = concat(tmpl, dict.getPoS(pos1), dict.getPoS(pos2), dict.getPoS(pos3))
}
case class UnigramFinePoSFeature(fine:Int, tmpl:Template) extends FeatureOnJapaneseDictionary {
  override def mkString(dict:JapaneseDictionary) = concat(tmpl, dict.getFineTag(fine))
}
case class BigramFinePoSFeature(fine1:Int, fine2:Int, tmpl:Template) extends FeatureOnJapaneseDictionary {
  override def mkString(dict:JapaneseDictionary) = concat(tmpl, dict.getFineTag(fine1), dict.getFineTag(fine2))
}
case class TrigramFinePoSFeature(fine1:Int, fine2:Int, fine3:Int, tmpl:Template) extends FeatureOnJapaneseDictionary {
  override def mkString(dict:JapaneseDictionary) = concat(tmpl, dict.getFineTag(fine1), dict.getFineTag(fine2), dict.getFineTag(3))
}
