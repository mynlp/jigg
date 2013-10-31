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

// features are classified by the way for stringizing; useful for pattern match when converting to strings.
trait FeatureOnDictionary extends SuperTaggingUnlabeledFeature {
  def mkString(dict:Dictionary):String
}
trait FeatureOnJapaneseDictionary extends SuperTaggingUnlabeledFeature {
  def mkString(dict:JapaneseDictionary):String
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
case class UnigramFinePoSFeature[T](fine:Int, tmpl:T) extends FeatureOnJapaneseDictionary {
  override def mkString(dict:JapaneseDictionary) = concat(tmpl, dict.getFineTag(fine))
}
case class BigramFinePoSFeature[T](fine1:Int, fine2:Int, tmpl:T) extends FeatureOnJapaneseDictionary {
  override def mkString(dict:JapaneseDictionary) = concat(tmpl, dict.getFineTag(fine1), dict.getFineTag(fine2))
}
case class TrigramFinePoSFeature[T](fine1:Int, fine2:Int, fine3:Int, tmpl:T) extends FeatureOnJapaneseDictionary {
  override def mkString(dict:JapaneseDictionary) = concat(tmpl, dict.getFineTag(fine1), dict.getFineTag(fine2), dict.getFineTag(3))
}
