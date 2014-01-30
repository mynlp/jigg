package enju.ccg.parser

import enju.ccg.lexicon.Category
import enju.ccg.lexicon.Direction

import org.scalatest.FunSuite
import org.scalatest.Matchers._

// {> NP[nc,nm]
//   {ADN NP[nc,nm]1／NP[nc,nm]1
//     {< S[adn,base]
//       {< NP[ni,nm]
//         {NP[nc,nm] 政権/政権/名詞-一般/_}
//         {NP[ni,nm]＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}
//       }
//       {< S[adn,base]＼NP[ni,nm]
//         {< NP[o,nm]
//           {NP[nc,nm] 影響/影響/名詞-サ変接続/_}
//           {NP[o,nm]＼NP[nc,nm]sem を/を/助詞-格助詞-一般/_}
//         }
//         {(S[adn,base]＼NP[ni,nm,ni])＼NP[o,nm,o] 及ぼす/及ぼす/動詞-自立/基本形}
//       }
//     }
//   }
//   {NP[nc,nm] こと/こと/名詞-非自立-一般/_}
// }

class StaticArcStandardOracleTest extends FunSuite {
  val parsedSentences = new ParsedSentences
  val dict = parsedSentences.dict
  def cat(str:String) = dict.getCategory(str).get

  test("simple sentence oracle") {
    val (sentence, derivation) = parsedSentences.simpleSentenceAndDerivation
    val trainSentence = sentence.assignCands(Array.fill(sentence.size)(Nil))

    val rule = CFGRule.extractRulesFromDerivations(Array(derivation), JapaneseHeadFinder)

    val oracleGen = StaticOracleGenerator
    val oracle = oracleGen.gen(trainSentence, derivation, rule)

    def checkAction(prev:State, expected:Action): State = {
      val goldActions = oracle.goldActions(prev)
      goldActions.size should equal(1)
      goldActions(0) should equal(expected)
      prev.proceed(goldActions(0), true)
    }
    // WARNING: this test is going to be invalid when HeadFinder is implimented in a correct way.
    val goldActions = Array(Shift(dict.getCategory("NP[nc,nm]").get),
                            Shift(dict.getCategory("NP[ni,nm]＼NP[nc,nm]sem").get),
                            Combine(dict.getCategory("NP[ni,nm]").get, Direction.Right, "<"),
                            Shift(dict.getCategory("NP[nc,nm]").get),
                            Shift(dict.getCategory("NP[o,nm]＼NP[nc,nm]sem").get),
                            Combine(dict.getCategory("NP[o,nm]").get, Direction.Right, "<"),
                            Shift(dict.getCategory("(S[adn,base]＼NP[ni,nm,ni])＼NP[o,nm,o]").get),
                            Combine(dict.getCategory("S[adn,base]＼NP[ni,nm]").get, Direction.Right, "<"),
                            Combine(dict.getCategory("S[adn,base]").get, Direction.Right, "<"),
                            Unary(dict.getCategory("NP[nc,nm]1／NP[nc,nm]1").get, "ADN"),
                            Shift(dict.getCategory("NP[nc,nm]").get),
                            Combine(dict.getCategory("NP[nc,nm]").get, Direction.Right, ">"),
                            Finish())

    var prev:State = InitialFullState
    goldActions.foreach { action =>
      prev = checkAction(prev, action)
    }

    // State.toDerivation test
    val inducedDerivation = prev.toDerivation
    derivation.map.size should equal (inducedDerivation.map.size)
    derivation.root should equal (inducedDerivation.root)
    derivation.map should equal (inducedDerivation.map)

    trainSentence.catSeq.toArray should equal (inducedDerivation.categorySeq map { _.get })

    inducedDerivation.render(trainSentence) should equal ("""{> NP[mod=nm,case=nc] {ADN NP[mod=nm,case=nc]/NP[mod=nm,case=nc] {< S[mod=adn,form=base] {< NP[mod=nm,case=ni] {NP[mod=nm,case=nc] 政権/名詞-一般/_} {NP[mod=nm,case=ni]\NP[mod=nm,case=nc] に/助詞-格助詞-一般/_}} {< S[mod=adn,form=base]\NP[mod=nm,case=ni] {< NP[mod=nm,case=o] {NP[mod=nm,case=nc] 影響/名詞-サ変接続/_} {NP[mod=nm,case=o]\NP[mod=nm,case=nc] を/助詞-格助詞-一般/_}} {(S[mod=adn,form=base]\NP[mod=nm,case=ni])\NP[mod=nm,case=o] 及ぼす/動詞-自立/基本形}}}} {NP[mod=nm,case=nc] こと/名詞-非自立-一般/_}}""")
  }
}
