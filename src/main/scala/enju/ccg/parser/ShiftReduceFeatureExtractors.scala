package enju.ccg.parser

import enju.ccg.lexicon.{CandAssignedSentence}

import scala.collection.mutable.ArrayBuffer

trait Context {
  def sentence: CandAssignedSentence
  def state: State
  def word(i:Int): Int = sentence.word(i).id
  def pos(i:Int): Int = sentence.pos(i).id // if you want to change the definition of pos, please override 
  val s0 = state.s0
  val s1 = state.s1
  val s2 = state.s2
  val s3 = state.s3
  
  val q0:Option[Int] = if (state.j < sentence.size) Some(state.j) else None
  val q1:Option[Int] = if (state.j+1 < sentence.size) Some(state.j+1) else None
  val q2:Option[Int] = if (state.j+2 < sentence.size) Some(state.j+2) else None
  val q3:Option[Int] = if (state.j+3 < sentence.size) Some(state.j+2) else None
}

trait FeatureExtractor {
  def addFeatures(c:Context, features:ArrayBuffer[UF]): Unit
}

class ZhangExtractor extends FeatureExtractor {
  import FeatureTypes._
  import FeatureTypes.{ZhangTemplate => TMP}
  def addFeatures(ctx:Context, features:ArrayBuffer[UF]) = {
    @inline def w(item:(Int,Int,Int)) = item._1
    @inline def p(item:(Int,Int,Int)) = item._2
    @inline def c(item:(Int,Int,Int)) = item._3
    def getItemsAt(s:WrappedCategory) = (ctx.word(s.head), ctx.pos(s.head), s.cat)
    def wordPoSAt(i:Int) = (ctx.word(i), ctx.pos(i), 0)
    
    val s0 = ctx.s0 map { getItemsAt(_) }
    val s1 = ctx.s1 map { getItemsAt(_) }
    val s2 = ctx.s2 map { getItemsAt(_) }
    val s3 = ctx.s3 map { getItemsAt(_) }
    val q0 = ctx.q0 map { wordPoSAt(_) }
    val q1 = ctx.q1 map { wordPoSAt(_) }
    val q2 = ctx.q2 map { wordPoSAt(_) }
    val q3 = ctx.q3 map { wordPoSAt(_) }
    val s0l = ctx.state.s0l map { getItemsAt(_) }
    val s0r = ctx.state.s0r map { getItemsAt(_) }
    val s0u = ctx.state.s0u map { getItemsAt(_) }
    val s1l = ctx.state.s1l map { getItemsAt(_) }
    val s1r = ctx.state.s1r map { getItemsAt(_) }
    val s1u = ctx.state.s1u map { getItemsAt(_) }
    
    s0 foreach { S0 =>
      features += WP(w(S0), p(S0), TMP.wS0_pS0)
      features += C(c(S0), TMP.cS0)
      features += PC(p(S0), c(S0), TMP.pS0_cS0)
      features += WC(w(S0), c(S0), TMP.wS0_cS0)
    }    
    s1 foreach { S1 =>
      features += WP(w(S1), p(S1), TMP.wS1_pS1)
      features += C(c(S1), TMP.cS1)
      features += PC(p(S1), c(S1), TMP.pS1_cS1)
      features += WC(w(S1), c(S1), TMP.wS1_cS1)
    }
    s2 foreach { S2 =>
      features += PC(p(S2), c(S2), TMP.pS2_cS2)
      features += WC(w(S2), c(S2), TMP.wS2_cS2)
    }
    s3 foreach { S3 =>
      features += PC(p(S3), c(S3), TMP.pS3_cS3)
      features += WC(w(S3), c(S3), TMP.wS3_cS3)
    }
    
    q0 foreach { Q0 => features += WP(w(Q0), p(Q0), TMP.wQ0_pQ0) }
    q1 foreach { Q1 => features += WP(w(Q1), p(Q1), TMP.wQ1_pQ1) }
    q2 foreach { Q2 => features += WP(w(Q2), p(Q2), TMP.wQ2_pQ2) }
    q3 foreach { Q3 => features += WP(w(Q3), p(Q3), TMP.wQ3_pQ3) }
    
    s0l foreach { S0L =>
      features += PC(p(S0L), c(S0L), TMP.pS0L_cS0L)
      features += WC(w(S0L), c(S0L), TMP.wS0L_cS0L)
    }
    s0r foreach { S0R =>
      features += PC(p(S0R), c(S0R), TMP.pS0R_cS0R)
      features += WC(w(S0R), c(S0R), TMP.wS0R_cS0R)
    }
    s0u foreach { S0U =>
      features += PC(p(S0U), c(S0U), TMP.pS0U_cS0U)
      features += WC(w(S0U), c(S0U), TMP.wS0U_cS0U)
    }
    s1l foreach { S1L =>
      features += PC(p(S1L), c(S1L), TMP.pS1L_cS1L)
      features += WC(w(S1L), c(S1L), TMP.wS1L_cS1L)
    }
    s1r foreach { S1R =>
      features += PC(p(S1R), c(S1R), TMP.pS1R_cS1R)
      features += WC(w(S1R), c(S1R), TMP.wS1R_cS1R)
    }
    s1u foreach { S1U =>
      features += PC(p(S1U), c(S1U), TMP.pS1U_cS1U)
      features += WC(w(S1U), c(S1U), TMP.wS1U_cS1U)
    }
    
    s1 foreach { S1 => s0 foreach { S0 =>
      features += WCWC(w(S0), c(S0), w(S1), c(S1), TMP.wS0_cS0_wS1_cS1)
      features += WC(w(S1), c(S0), TMP.wS1_cS0)
      features += WC(w(S0), c(S1), TMP.wS0_cS1)
      features += CC(c(S0), c(S1), TMP.cS0_cS1)
    }}
    s0 foreach { S0 => q0 foreach { Q0 =>
      features += WCWP(w(S0), c(S0), w(Q0), p(Q0), TMP.wS0_cS0_wQ0_pQ0)
      features += WPC(w(Q0), p(Q0), c(S0), TMP.wQ0_pQ0_cS0)
      features += WPC(w(S0), p(Q0), c(S0), TMP.wS0_pQ0_cS0)
      features += PC(p(Q0), c(S0), TMP.pQ0_cS0)
    }}
    s1 foreach { S1 => q0 foreach { Q0 =>
      features += WCWP(w(S1), c(S1), w(Q0), p(Q0), TMP.wS1_cS1_wQ0_pQ0)
      features += WPC(w(Q0), p(Q0), c(S1), TMP.wQ0_pQ0_cS1)
      features += WPC(w(S1), p(Q0), c(S1), TMP.wS1_pQ0_cS1)
      features += PC(p(Q0), c(S1), TMP.pQ0_cS1)
    }}
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
