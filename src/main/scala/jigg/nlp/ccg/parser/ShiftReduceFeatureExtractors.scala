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

import jigg.nlp.ccg.lexicon.{CandAssignedSentence, PoS}

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
    case class Item(w: Char, p: Short, c: Short)

    @inline def w(item: Item):Char = item.w
    @inline def p(item: Item):Short = item.p
    @inline def c(item: Item):Short = item.c
    def getItemsAt(s:WrappedCategory) = Item(ctx.word(s.head).toChar, ctx.pos(s.head).toShort, s.cat.toShort)
    def wordPoSAt(i:Int) = Item(ctx.word(i).toChar, ctx.pos(i).toShort, 0.toShort)

    val s0: Option[Item] = ctx.s0 map { getItemsAt(_) }
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
    val s0h = ctx.state.s0h map { getItemsAt(_) }
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

    s1 foreach { S1 => s0 foreach { S0 => q0 foreach { Q0 =>
      features += WPCC(w(S0), p(Q0), c(S0), c(S1), TMP.wS0_pQ0_cS0_cS1)
      features += WPCC(w(S1), p(Q0), c(S0), c(S1), TMP.wS1_pQ0_cS0_cS1)
      features += WPCC(w(Q0), p(Q0), c(S0), c(S1), TMP.wQ0_pQ0_cS0_cS1)
      features += PCC(p(Q0), c(S0), c(S1), TMP.pQ0_cS0_cS1)
      features += PPP(p(S0), p(S1), p(Q0), TMP.pS0_pS1_pQ0)
    }}}
    s0 foreach { S0 => q0 foreach { Q0 => q1 foreach { Q1 =>
      features += WPPC(w(S0), p(Q0), p(Q1), c(S0), TMP.wS0_pQ0_pQ1_cS0)
      features += WPPC(w(Q0), p(Q0), p(Q1), c(S0), TMP.wQ0_pQ0_pQ1_cS0)
      features += WPPC(w(Q1), p(S0), p(Q1), c(S0), TMP.wQ1_pQ0_pQ1_cS0)
      features += PPC(p(Q0), p(Q1), c(S0), TMP.pQ0_pQ1_cS0)
      features += PPP(p(S0), p(Q0), p(Q1), TMP.pS0_pQ0_pQ1)

    }}}
    s2 foreach { S2 => s1 foreach { S1 => s0 foreach { S0 =>
      features += WCCC(w(S0), c(S0), c(S1), c(S2), TMP.wS0_cS0_cS1_cS2)
      features += WCCC(w(S1), c(S0), c(S1), c(S2), TMP.wS1_cS0_cS1_cS2)
      features += WCCC(w(S2), c(S0), c(S1), c(S2), TMP.wS2_cS0_cS1_cS2)
      features += CCC(c(S0), c(S1), c(S2), TMP.cS0_cS1_cS2)
      features += PPP(p(S0), p(S1), p(S2), TMP.pS0_pS1_pS2)
    }}}

    s0l foreach { S0L => s0h foreach { S0H => s0 foreach { S0 =>
      features += CCC(c(S0), c(S0H), c(S0L), TMP.cS0_cS0H_cS0L)
    }}}
    s0r foreach { S0R => s0h foreach { S0H => s0 foreach { S0 =>
      features += CCC(c(S0), c(S0H), c(S0R), TMP.cS0_cS0H_cS0R)
    }}}
    s0r foreach { S0R => q0 foreach { Q0 => s0 foreach { S0 =>
      features += PCC(p(Q0), c(S0), c(S0R), TMP.pQ0_cS0_cS0R)
      features += WCC(w(Q0), c(S0), c(S0R), TMP.wQ0_cS0_cS0R)
    }}}
    s0l foreach { S0L => s1 foreach { S1 => s0 foreach { S0 =>
      features += CCC(c(S0), c(S0L), c(S1), TMP.cS0_cS0L_cS1)
      features += WCC(w(S1), c(S0), c(S0L), TMP.wS1_cS0_cS0L)
    }}}
    s1r foreach { S1R => s1 foreach { S1 => s0 foreach { S0 =>
      features += CCC(c(S0), c(S1), c(S1R), TMP.cS0_cS1_cS1R)
      features += WCC(w(S0), c(S1), c(S1R), TMP.wS0_cS1_cS1R)
    }}}
  }
}

class BasicFinishedExtractor extends FeatureExtractor {
  import FeatureTypes._
  import FeatureTypes.{FinishTemplate => TMP}
  def addFeatures(ctx:Context, features:ArrayBuffer[UF]) = {
    def p(s: WrappedCategory) = ctx.pos(s.head).toShort
    def c(s: WrappedCategory) = s.category.id.toShort
    ctx.s0 match {
      case Some(s0) =>
        features += C(c(s0), TMP.cS0)
        features += PC(p(s0), c(s0), TMP.pS0_cS0)
      case None =>
    }
    ctx.s1 match {
      case Some(s1) => features += C(c(s1), TMP.cS1)
      case None => features += Empty(TMP.no_cS1)
    }
    ctx.s2 match {
      case Some(s2) => features += C(c(s2), TMP.cS2)
      case None => features += Empty(TMP.no_cS2)
    }
  }
}

trait FeatureExtractorsBase {
  def methods: Seq[FeatureExtractor]
  def pos2id: (PoS=>Int) = { pos => pos.id }

  def finishMethods: Seq[FeatureExtractor] = Seq(new BasicFinishedExtractor())

  class ContextWithCustomPosLevel(
    override val sentence:CandAssignedSentence,
    override val state:State) extends Context {
    override def pos(i:Int) = pos2id(sentence.pos(i))
  }
  def context(sentence:CandAssignedSentence, state:State): Context =
    new ContextWithCustomPosLevel(sentence, state)

  //var features = new ArrayBuffer[UF]

  def extractUnlabeledFeatures(sentence:CandAssignedSentence, state:State) =
    extractUnlabeledHelper(sentence, state, methods)
  def extractFeaturesFromFinishedTree(sentence: CandAssignedSentence, state: State) =
    extractUnlabeledHelper(sentence, state, finishMethods)

  def extractUnlabeledHelper(sentence:CandAssignedSentence, state:State, extractors: Seq[FeatureExtractor]): Seq[UF] = {
    val features = new ArrayBuffer[UF]
    features += FeatureTypes.Bias()

    val ctx = context(sentence, state)
    extractors.foreach { _.addFeatures(ctx, features) }
    features
  }
}

class FeatureExtractors(
  override val methods: Seq[FeatureExtractor],
  override val pos2id: (PoS=>Int)) extends FeatureExtractorsBase
