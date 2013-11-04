package enju.ccg.tagger

import enju.ccg.lexicon.{PoS, Word, TaggedSentence}

import scala.collection.mutable.ArrayBuffer

object Template extends Enumeration {
  type Template = Value
  val bias, w, wPrev1, wPrev2, wNext1, wNext2, wPrev2_wPrev1, wPrev1_w, w_wNext1, wNext1_wNext2, p, pPrev1, pPrev2, pNext1, pNext2, pPrev2_pPrev1, pPrev1_p, p_pNext1, pNext1_pNext2, pPrev2_pPrev1_p, pPrev1_p_pNext1, p_pNext1_pNext2 = Value
}

import Template.Template

//import TemplateTypes.Template

trait Context {
  def sentence:TaggedSentence
  def i:Int              // current position on the sentence
  def word(bias:Int):Int // if bias is: 0 => i-th word, 1 => next word, -1 => previous word, and that
  def pos(bias:Int):Int  // the same as above
}

trait FeatureExtractor {
  type Buf = ArrayBuffer[UF]
  def addFeatures(c:Context, features:Buf):Unit
}

class UnigramWordExtractor(val windowSize:Int) extends FeatureExtractor {
  require(windowSize % 2 == 1 && windowSize <= 5, "invalid windowSize")
  def addFeatures(c:Context, features:ArrayBuffer[UF]) = {
    features += UnigramWordFeature(c.word(0), Template.w)
    if (windowSize >= 3) {
      features += UnigramWordFeature(c.word(-1), Template.wPrev1)
      features += UnigramWordFeature(c.word(1), Template.wNext1)
    }
    if (windowSize >= 5) {
      features += UnigramWordFeature(c.word(-2), Template.wPrev2)
      features += UnigramWordFeature(c.word(2), Template.wNext2)
    }
  }
}

class UnigramPoSExtractor(val windowSize:Int) extends FeatureExtractor {
  require(windowSize % 2 == 1 && windowSize <= 5, "invalid windowSize")
  def addFeatures(c:Context, features:ArrayBuffer[UF]) = {
    features += UnigramPoSFeature(c.pos(0), Template.p)
    if (windowSize >= 3) {
      features += UnigramPoSFeature(c.pos(-1), Template.pPrev1)
      features += UnigramPoSFeature(c.pos(1), Template.pNext1)
    }
    if (windowSize >= 5) {
      features += UnigramPoSFeature(c.pos(-2), Template.pPrev2)
      features += UnigramPoSFeature(c.pos(2), Template.pNext2)
    }
  }
}

class BigramPoSExtractor(val windowSize:Int) extends FeatureExtractor {
  require(windowSize == 3 || windowSize == 5, "invalid windowSize")
  def addFeatures(c:Context, features:ArrayBuffer[UF]) = {
    if (windowSize >= 3) {
      features += BigramPoSFeature(c.pos(-1), c.pos(0), Template.pPrev1_p)
      features += BigramPoSFeature(c.pos(0), c.pos(1), Template.p_pNext1)
    }
    if (windowSize >= 5) {
      features += BigramPoSFeature(c.pos(-2), c.pos(-1), Template.pPrev2_pPrev1)
      features += BigramPoSFeature(c.pos(1), c.pos(2), Template.pNext1_pNext2)
    }
  }
}

// user can define his own extractor extends FeatureExtractor
class FeatureExtractors(val methods:Seq[FeatureExtractor], val bos:Word, val bosPoS:PoS) {
  var featureSize = 0

  class ContextWithBOS(override val sentence:TaggedSentence, override val i:Int) extends Context {
    override def word(bias:Int) = (i + bias) match {
      case p => if (p < 0 || p >= sentence.size) bos.id else sentence.word(p).id }
    override def pos(bias:Int) = (i + bias) match {
      case p => if (p < 0 || p >= sentence.size) bosPoS.id else sentence.pos(p).id }
  }
  
  // please override this in subclass if you want to use differenct context object
  def context(sentence:TaggedSentence, i:Int):Context = new ContextWithBOS(sentence, i)

  def extractUnlabeledFeatures(sentence:TaggedSentence, i:Int):Seq[UF] = {
    val features = new ArrayBuffer[UF](featureSize)
    features += BiasFeature(Template.bias)

    val ctx = context(sentence, i)
    methods.foreach { _.addFeatures(ctx, features) }
    featureSize = features.size
    features
  }
}

class FeatureExtractorsWithCustomPoSLevel(methods:Seq[FeatureExtractor], bos:Word, bosPoS:PoS, val pos2id:(PoS=>Int)) extends FeatureExtractors(methods, bos, bosPoS) {
  
  class ContextWithCustomPoSLevel(override val sentence:TaggedSentence, override val i:Int) extends ContextWithBOS(sentence, i) {
    override def pos(bias:Int) = (i + bias) match {
      case p => if (p < 0 || p >= sentence.size) bosPoS.id else pos2id(sentence.pos(p)) }
  }
  override def context(sentence:TaggedSentence, i:Int):Context = new ContextWithCustomPoSLevel(sentence, i)
}

class FeatureExtractorsWithSecondLevelPoS(
  methods:Seq[FeatureExtractor], bos:Word, bosPoS:PoS, 
  override val pos2id:(PoS=>Int) = { pos => pos.second.id }) extends FeatureExtractorsWithCustomPoSLevel(methods, bos, bosPoS, pos2id)
