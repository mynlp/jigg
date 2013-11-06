package enju.ccg.parser

import enju.ccg.lexicon.{CandAssignedSentence}

import scala.collection.mutable.ArrayBuffer

trait Context {
  def sentence:CandAssignedSentence
  def state:State
  def word(i:Int): Int = sentence.word(i).id
  def pos(i:Int): Int = sentence.pos(i).id // if you want to change the definition of pos, please override 
  val s0 = state.s0
  val s1 = state.s1
  val s2 = state.s2
  val s3 = state.s3
}

trait FeatureExtractor {
  def addFeatures(c:Context, features:ArrayBuffer[UF]): Unit
}

class ZhangExtractor extends FeatureExtractor {
  import FeatureTypes._
  import FeatureTypes.{ZhangTemplate => TMP}
  def addFeatures(ctx:Context, features:ArrayBuffer[UF]) = {
    def getItemsAt(s:WrappedCategory) = (ctx.word(s.head), ctx.pos(s.head), s.cat)
    ctx.s0 foreach { s0 =>
      val (wS0, pS0, cS0) = getItemsAt(s0)
      features += WP(wS0, pS0, TMP.wS0_pS0)
      features += C(cS0, TMP.cS0)
      features += PC(pS0, cS0, TMP.pS0_cS0)
      features += WC(wS0, cS0, TMP.wS0_cS0)
    }
    ctx.s1 foreach { s1 =>
      val (wS1, pS1, cS1) = getItemsAt(s1)
      features += WP(wS1, pS1, TMP.wS1_pS1)
      features += C(cS1, TMP.cS1)
      features += PC(pS1, cS1, TMP.pS1_cS1)
      features += WC(wS1, cS1, TMP.wS1_cS1)
    }
  }
}

class FeatureExtractors(val methods:Seq[FeatureExtractor]) {
  var featureSize = 0
  
  class ContextWithFullPoS(override val sentence:CandAssignedSentence, override val state:State) extends Context {
    override def pos(i:Int) = sentence.pos(i).id
  }
  def context(sentence:CandAssignedSentence, state:State): Context = new ContextWithFullPoS(sentence, state)
  
  def extractUnlabeledFeatures(sentence:CandAssignedSentence, state:State): Seq[UF] = {
    val features = new ArrayBuffer[UF](featureSize)
    features += FeatureTypes.Bias()
    
    val ctx = context(sentence, state)
    methods.foreach { _.addFeatures(ctx, features) }
    featureSize = features.size
    features
  }
}
