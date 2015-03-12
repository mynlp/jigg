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

import jigg.nlp.ccg.lexicon.Category
import jigg.nlp.ccg.lexicon.Direction

import org.scalatest.FunSuite
import org.scalatest.Matchers._

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
    val goldActions = Array(Shift(dict.getCategory("NP[case=nc,mod=nm]{I1}_none").get),
                            Shift(dict.getCategory("(NP[case=ni,mod=nm]{I1}\\NP[case=nc,mod=nm]{I1}){I2}_none").get),
                            Combine(dict.getCategory("NP[case=ni,mod=nm]{I1}").get, Direction.Right, "<"),
                            Shift(dict.getCategory("NP[case=nc,mod=nm]{I1}_none").get),
                            Shift(dict.getCategory("(NP[case=o,mod=nm]{I1}\\NP[case=nc,mod=nm]{I1}){I2}_none").get),
                            Combine(dict.getCategory("NP[case=o,mod=nm]{I1}").get, Direction.Right, "<"),
                            Shift(dict.getCategory("(((S[mod=adn,form=base]{I1}\\NP[case=ni,mod=nm]{I2}){I1})\\NP[case=o,mod=nm]{I3}){I1}_I1(unk,I3,I2,_)").get),
                            Combine(dict.getCategory("(S[mod=adn,form=base]{I1}\\NP[case=ni,mod=nm]{I2}){I1}").get, Direction.Right, "<"),
                            Combine(dict.getCategory("S[mod=adn,form=base]{I1}").get, Direction.Right, "<"),
                            Unary(dict.getCategory("(NP[case=nc,mod=X1]{I1}/NP[case=nc,mod=X1]{I1}){I2}").get, "ADN"),
                            Shift(dict.getCategory("NP[case=nc,mod=nm]{I1}_none").get),
                            Combine(dict.getCategory("NP[case=nc,mod=nm]{I1}").get, Direction.Right, ">"),
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

    inducedDerivation.render(trainSentence) should equal ("""{> NP[mod=nm,case=nc] {ADN NP[case=nc]/NP[case=nc] {< S[mod=adn,form=base] {< NP[mod=nm,case=ni] {NP[mod=nm,case=nc] 政権/政権/名詞-一般/_} {NP[mod=nm,case=ni]\NP[mod=nm,case=nc] に/に/助詞-格助詞-一般/_}} {< S[mod=adn,form=base]\NP[mod=nm,case=ni] {< NP[mod=nm,case=o] {NP[mod=nm,case=nc] 影響/影響/名詞-サ変接続/_} {NP[mod=nm,case=o]\NP[mod=nm,case=nc] を/を/助詞-格助詞-一般/_}} {(S[mod=adn,form=base]\NP[mod=nm,case=ni])\NP[mod=nm,case=o] 及ぼす/及ぼす/動詞-自立/基本形}}}} {NP[mod=nm,case=nc] こと/こと/名詞-非自立-一般/_}}""")
  }
}
