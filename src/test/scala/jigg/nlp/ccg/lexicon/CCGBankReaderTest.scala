package jigg.nlp.ccg.lexicon

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

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class CCGBankReaderTest extends FunSuite {
  val line = """{< S[mod=nm,form=base]{I1} {< S[mod=nm,form=base]{I1} {< NP[case=ga,mod=nm]{I1} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 村山/村山/名詞-固有名詞-人名-姓/_} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 富市/富市/名詞-固有名詞-人名-名/_} {NP[case=nc,mod=nm]{I1}_none 首相/首相/名詞-一般/_}}} {(NP[case=ga,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none は/は/助詞-係助詞/_}} {>Bx (S[mod=nm,form=base]{I1}\NP[case=ga,mod=nm]{I2}){I3} {ADV (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {< S[mod=adv,form=cont]{I1} {> S[mod=adv,form=cont]{I1} {ADV (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {> S[mod=adv,form=cont]{I1} {< (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {NP[case=nc,mod=nm]{I1}_none 年頭/年頭/名詞-一般/_} {(((S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2})\NP[case=nc,mod=nm]{I2}){I3}_none に/に/助詞-格助詞-一般/_}} {S[mod=adv,form=cont]{I1}_I1(unk,_,_,_) あたり/あたる/動詞-自立/連用形}}} {> S[mod=adv,form=cont]{I1} {< (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 首相/首相/名詞-一般/_} {NP[case=nc,mod=nm]{I1}_none 官邸/官邸/名詞-一般/_}} {(((S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2})\NP[case=nc,mod=nm]{I2}){I3}_none で/で/助詞-格助詞-一般/_}} {> S[mod=adv,form=cont]{I1} {< (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 内閣/内閣/名詞-一般/_} {< NP[case=nc,mod=nm]{I1} {NP[case=nc,mod=nm]{I1}_none 記者/記者/名詞-一般/_} {(NP[case=nc,mod=nm]{I1}\NP[case=nc,mod=nm]{I2}){I1}_none 会/会/名詞-接尾-一般/_}}} {(((S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2})\NP[case=nc,mod=nm]{I3}){I2}_none と/と/助詞-並立助詞/_}} {> S[mod=adv,form=cont]{I1} {ADV (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {< NP[case=nc,mod=adv]{I1} {> NP[case=nc,mod=nm]{I1} {>Bn (NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 二/二/名詞-数/_} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 十/十/名詞-数/_}} {NP[case=nc,mod=nm]{I1}_none 八/八/名詞-数/_}} {(NP[case=nc,mod=adv]{I1}\NP[case=nc,mod=nm]{I2}){I1}_none 日/日/名詞-接尾-助数詞/_}}} {< S[mod=adv,form=cont]{I1} {S[mod=nm,form=stem]{I1}_I1(unk,_,_,_) 会見/会見/名詞-サ変接続/_} {(S[mod=adv,form=cont]{I1}\S[mod=nm,form=stem]{I1}){I2}_none し/する/動詞-自立/連用形}}}}}} {(S[mod=X1,form=X2]{I1}\S[mod=X1,form=X2]{I1}){I2}_none 、/、/記号-読点/_}}} {>Bx (S[mod=nm,form=base]{I1}\NP[case=ga,mod=nm]{I2}){I3} {ADV (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {< S[mod=adv,form=cont]{I1} {> S[mod=adv,form=cont]{I1} {ADV (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {> S[mod=adv,form=cont]{I1} {< (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {> NP[case=nc,mod=nm]{I1} {< (NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2} {> NP[case=nc,mod=nm]{I1} {< (NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2} {NP[case=nc,mod=nm]{I1}_none 社会党/社会党/名詞-固有名詞-組織/_} {(((NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2})\NP[case=nc,mod=nm]{I3}){I2}_none の/の/助詞-連体化/_}} {> NP[case=nc,mod=nm]{I1} {>Bn (NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 新/新/接頭詞-名詞接続/_} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 民主/民主/名詞-一般/_}} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 連合/連合/名詞-サ変接続/_} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 所属/所属/名詞-サ変接続/_} {NP[case=nc,mod=nm]{I1}_none 議員/議員/名詞-一般/_}}}}} {(((NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2})\NP[case=nc,mod=nm]{I3}){I2}_none の/の/助詞-連体化/_}} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 離党/離党/名詞-サ変接続/_} {NP[case=nc,mod=nm]{I1}_none 問題/問題/名詞-ナイ形容詞語幹/_}}} {(((S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2})\NP[case=nc,mod=nm]{I2}){I3}_none に/に/助詞-格助詞-一般/_}} {< S[mod=adv,form=cont]{I1} {S[mod=nm,form=cont]{I1}_I1(unk,_,_,_) つい/つく/動詞-自立/連用タ接続} {(S[mod=adv,form=cont]{I1}\S[mod=nm,form=cont]{I1}){I2}_none て/て/助詞-接続助詞/_}}}} {< S[mod=adv,form=cont]{I1} {< NP[case=to,mod=nm]{I1} {< S[mod=nm,form=base]{I1} {> S[mod=nm,form=base]{I1} {(S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2}_none 「/「/記号-括弧開/_} {SSEQ S[mod=nm,form=base]{I1} {< S[mod=nm,form=base]{I1} {< S[mod=nm,form=base]{I1} {< NP[case=ni,mod=nm]{I1} {< NP[case=ni,mod=nm]{I1} {> NP[case=nc,mod=nm]{I1} {ADN (NP[case=nc,mod=X1]{I1}/NP[case=nc,mod=X1]{I1}){I2} {< S[mod=adn,form=base]{I1} {< NP[case=ni,mod=nm]{I1} {NP[case=nc,mod=nm]{I1}_none 政権/政権/名詞-一般/_} {(NP[case=ni,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none に/に/助詞-格助詞-一般/_}} {< (S[mod=adn,form=base]{I1}\NP[case=ni,mod=nm]{I2}){I1} {< NP[case=o,mod=nm]{I1} {NP[case=nc,mod=nm]{I1}_none 影響/影響/名詞-サ変接続/_} {(NP[case=o,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none を/を/助詞-格助詞-一般/_}} {(((S[mod=adn,form=base]{I1}\NP[case=ni,mod=nm]{I2}){I1})\NP[case=o,mod=nm]{I3}){I1}_I1(unk,I3,I2,_) 及ぼす/及ぼす/動詞-自立/基本形}}}} {NP[case=nc,mod=nm]{I1}_none こと/こと/名詞-非自立-一般/_}} {(NP[case=ni,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none に/に/助詞-格助詞-一般/_}} {(NP[case=ni,mod=nm]{I1}\NP[case=ni,mod=nm]{I1}){I2}_none は/は/助詞-係助詞/_}} {<Bn (S[mod=nm,form=base]{I1}\NP[case=ni,mod=nm]{I2}){I3} {(S[mod=nm,form=neg]{I1}\NP[case=ni,mod=nm]{I2}){I1}_I1(unk,_,I2,_) なら/なる/動詞-非自立/未然形} {(S[mod=nm,form=base]{I1}\S[mod=nm,form=neg]{I1}){I2}_none ない/ない/助動詞/基本形}}} {(S[mod=X1,form=X2]{I1}\S[mod=X1,form=X2]{I1}){I2}_none 。/。/記号-句点/_}} {< S[mod=nm,form=base]{I1} {< NP[case=to,mod=nm]{I1} {> S[mod=nm,form=base]{I1} {< (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {< (S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2} {< S[mod=nm,form=cont]{I1} {< NP[case=ga,mod=nm]{I1} {< NP[case=nc,mod=nm]{I1} {NP[case=nc,mod=nm]{I1}_none 離党/離党/名詞-サ変接続/_} {(NP[case=nc,mod=nm]{I1}\NP[case=nc,mod=nm]{I2}){I1}_none 者/者/名詞-接尾-一般/_}} {(NP[case=ga,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none が/が/助詞-格助詞-一般/_}} {<Bn (S[mod=nm,form=cont]{I1}\NP[case=ga,mod=nm]{I2}){I3} {(S[mod=nm,form=cont]{I1}\NP[case=ga,mod=nm]{I2}){I1}_I1(I2,_,_,_) い/いる/動詞-自立/連用形} {(S[mod=nm,form=cont]{I1}\S[mod=nm,form=cont]{I1}){I2}_none て/て/助詞-接続助詞/_}}} {(((S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2})\S[mod=nm,form=cont]{I2}){I3}_none も/も/助詞-係助詞/_}} {(((S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2})\((S[mod=X1,form=X2]{I1}/S[mod=X1,form=X2]{I1}){I2})){I3}_none 、/、/記号-読点/_}} {< S[mod=nm,form=base]{I1} {< NP[case=ni,mod=nm]{I1} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none その/その/連体詞/_} {NP[case=nc,mod=nm]{I1}_none 範囲/範囲/名詞-一般/_}} {(NP[case=ni,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none に/に/助詞-格助詞-一般/_}} {(S[mod=nm,form=base]{I1}\NP[case=ni,mod=nm]{I2}){I1}_I1(unk,_,I2,_) とどまる/とどまる/動詞-自立/基本形}}} {(NP[case=to,mod=nm]{I1}\S[mod=nm,form=base]{I1}){I2}_none と/と/助詞-格助詞-引用/_}} {(S[mod=nm,form=base]{I1}\NP[case=to,mod=nm]{I2}){I1}_I1(unk,_,_,I2) 思う/思う/動詞-自立/基本形}}}} {(S[mod=X1,form=X2]{I1}\S[mod=X1,form=X2]{I1}){I2}_none 」/」/記号-括弧閉/_}} {(NP[case=to,mod=nm]{I1}\S[mod=nm,form=base]{I1}){I2}_none と/と/助詞-格助詞-引用/_}} {(S[mod=adv,form=cont]{I1}\NP[case=to,mod=nm]{I2}){I1}_I1(unk,_,_,I2) 述べ/述べる/動詞-自立/連用形}}} {(S[mod=X1,form=X2]{I1}\S[mod=X1,form=X2]{I1}){I2}_none 、/、/記号-読点/_}}} {< (S[mod=nm,form=base]{I1}\NP[case=ga,mod=nm]{I2}){I3} {< NP[case=o,mod=nm]{I1} {> NP[case=nc,mod=nm]{I1} {< (NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2} {< NP[case=nc,mod=nm]{I1} {< S[mod=nm,form=base]{I1} {< NP[case=ni,mod=nm]{I1} {< NP[case=ni,mod=nm]{I1} {> NP[case=nc,mod=nm]{I1} {(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none 大量/大量/名詞-形容動詞語幹/_} {NP[case=nc,mod=nm]{I1}_none 離党/離党/名詞-サ変接続/_}} {(NP[case=ni,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none に/に/助詞-格助詞-一般/_}} {(NP[case=ni,mod=nm]{I1}\NP[case=ni,mod=nm]{I1}){I2}_none は/は/助詞-係助詞/_}} {<Bn (S[mod=nm,form=base]{I1}\NP[case=ni,mod=nm]{I2}){I3} {(S[mod=nm,form=neg]{I1}\NP[case=ni,mod=nm]{I2}){I1}_I1(unk,_,I2,_) 至ら/至る/動詞-自立/未然形} {(S[mod=nm,form=base]{I1}\S[mod=nm,form=neg]{I1}){I2}_none ない/ない/助動詞/基本形}}} {(NP[case=nc,mod=nm]{I1}\S[mod=nm,form=base]{I1}){I2}_none と/と/助詞-格助詞-引用/_}} {(((NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2})\NP[case=nc,mod=nm]{I3}){I2}_none の/の/助詞-連体化/_}} {NP[case=nc,mod=nm]{I1}_none 見通し/見通し/名詞-一般/_}} {(NP[case=o,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none を/を/助詞-格助詞-一般/_}} {<Bn (((S[mod=nm,form=base]{I1}\NP[case=ga,mod=nm]{I2}){I3})\NP[case=o,mod=nm]{I4}){I5} {(((S[mod=nm,form=cont]{I1}\NP[case=ga,mod=nm]{I2}){I1})\NP[case=o,mod=nm]{I3}){I1}_I1(I2,I3,_,_) 示し/示す/動詞-自立/連用形} {(S[mod=nm,form=base]{I1}\S[mod=nm,form=cont]{I1}){I2}_none た/た/助動詞/基本形}}}}}} {(S[mod=X1,form=X2]{I1}\S[mod=X1,form=X2]{I1}){I2}_none 。/。/記号-句点/_}}"""

  val simpleLine = """
{> NP[case=nc,mod=nm]{I1}
  {ADN (NP[case=nc,mod=X1]{I1}/NP[case=nc,mod=X1]{I1}){I2}
    {< S[mod=adn,form=base]{I1}
      {< NP[case=ni,mod=nm]{I1}
        {NP[case=nc,mod=nm]{I1}_none 政権/政権/名詞-一般/_}
        {(NP[case=ni,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none に/に/助詞-格助詞-一般/_}
      }
      {< (S[mod=adn,form=base]{I1}\NP[case=ni,mod=nm]{I2}){I1}
        {< NP[case=o,mod=nm]{I1}
          {NP[case=nc,mod=nm]{I1}_none 影響/影響/名詞-サ変接続/_}
          {(NP[case=o,mod=nm]{I1}\NP[case=nc,mod=nm]{I1}){I2}_none を/を/助詞-格助詞-一般/_}
        }
        {(((S[mod=adn,form=base]{I1}\NP[case=ni,mod=nm]{I2}){I1})\NP[case=o,mod=nm]{I3}){I1}_I1(unk,I3,I2,_) 及ぼす/及ぼす/動詞-自立/基本形}
      }
    }
  }
  {NP[case=nc,mod=nm]{I1}_none こと/こと/名詞-非自立-一般/_}
}"""

  val dict = new JapaneseDictionary
  def cat(str:String) = dict.getCategory(str).get

  def converter = new JapaneseParseTreeConverter(dict)

  def getParsedTree(lineStr:String = line): ParseTree[NodeLabel] = {
    val reader = new CCGBankReader(dict)
    val stringTrees = reader.readParseTree(lineStr, true)
    converter.toLabelTree(stringTrees)
  }

  test("read sentence test") {
    val reader = new CCGBankReader(dict)

    val sentence = converter.toSentenceFromStringTree(reader.readParseTree(line, true))
    sentence.word(0) should equal (dict.getWord("村山"))
    sentence.pos(0) should equal (dict.getPoS("名詞-固有名詞-人名-姓/_"))
    sentence.cat(0) should equal (cat("(NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none"))
    sentence.size should equal (76)
  }
  test("read with derivation test") {
    val tree = getParsedTree(line)
    tree.children.size should equal (2)
    tree.label.category should equal (cat("S[mod=nm,form=base]{I1}"))

    tree.children match {
      case BinaryTree(left, right, label) :: LeafTree(leafLabel: TerminalLabel) :: Nil => {
        left.label.category should equal (cat("NP[case=ga,mod=nm]{I1}"))
        right.label.category should equal (cat("(S[mod=nm,form=base]{I1}\\NP[case=ga,mod=nm]{I2}){I3}"))
        label.category should equal (cat("S[mod=nm,form=base]{I1}"))

        leafLabel.word should equal (dict.getWord("。"))
        leafLabel.baseForm should equal (dict.getWord("。"))
        leafLabel.pos should equal (dict.getPoS("記号-句点/_"))
        leafLabel.category should equal (cat("(S[mod=X1,form=X2]{I1}\\S[mod=X1,form=X2]{I1}){I2}_none"))
      }
      case _ => fail
    }
  }
  test("converting parse tree to derivation test") {
    val tree = getParsedTree(simpleLine)
    val deriv = converter.toDerivation(tree)
    val rootCategory = dict.getCategory("NP[case=nc,mod=nm]{I1}").get
    deriv.root should equal (Point(0, 6, rootCategory))
    deriv.get(0, 6, rootCategory) match {
      case Some(AppliedRule(BinaryChildrenPoints(left, right),_)) => {
        left should equal (Point(0, 5, cat("(NP[case=nc,mod=X1]{I1}/NP[case=nc,mod=X1]{I1}){I2}")))
        right should equal (Point(5, 6, cat("NP[case=nc,mod=nm]{I1}_none")))
      }
      case _ => fail
    }
    deriv.get(2, 5, cat("(S[mod=adn,form=base]{I1}\\NP[case=ni,mod=nm]{I2}){I1}")) match {
      case Some(AppliedRule(BinaryChildrenPoints(left, right),_)) => {
        left should equal (Point(2, 4, cat("NP[case=o,mod=nm]{I1}")))
        right should equal (Point(4, 5, cat("(((S[mod=adn,form=base]{I1}\\NP[case=ni,mod=nm]{I2}){I1})\\NP[case=o,mod=nm]{I3}){I1}_I1(unk,I3,I2,_)")))
      }
      case _ => fail
    }
  }
}

class EnglishCCGBankReaderTest extends FunSuite {
  val line = """(<T S[dcl] 0 2> (<T S[dcl] 1 2> (<T NP 0 2> (<T NP 0 2> (<T NP 0 2> (<T NP 0 1> (<T N 1 2> (<L N/N NNP NNP Pierre N_73/N_73>) (<L N NNP NNP Vinken N>) ) ) (<L , , , , ,>) ) (<T NP\NP 0 1> (<T S[adj]\NP 1 2> (<T NP 0 1> (<T N 1 2> (<L N/N CD CD 61 N_93/N_93>) (<L N NNS NNS years N>) ) ) (<L (S[adj]\NP)\NP JJ JJ old (S[adj]\NP_83)\NP_84>) ) ) ) (<L , , , , ,>) ) (<T S[dcl]\NP 0 2> (<L (S[dcl]\NP)/(S[b]\NP) MD MD will (S[dcl]\NP_10)/(S[b]_11\NP_10:B)_11>) (<T S[b]\NP 0 2> (<T S[b]\NP 0 2> (<T (S[b]\NP)/PP 0 2> (<L ((S[b]\NP)/PP)/NP VB VB join ((S[b]\NP_20)/PP_21)/NP_22>) (<T NP 1 2> (<L NP[nb]/N DT DT the NP[nb]_29/N_29>) (<L N NN NN board N>) ) ) (<T PP 0 2> (<L PP/NP IN IN as PP/NP_34>) (<T NP 1 2> (<L NP[nb]/N DT DT a NP[nb]_48/N_48>) (<T N 1 2> (<L N/N JJ JJ nonexecutive N_43/N_43>) (<L N NN NN director N>) ) ) ) ) (<T (S\NP)\(S\NP) 0 2> (<L ((S\NP)\(S\NP))/N[num] NNP NNP Nov. ((S_61\NP_56)_61\(S_61\NP_56)_61)/N[num]_62>) (<L N[num] CD CD 29 N[num]>) ) ) ) ) (<L . . . . .>) )"""

  val dict = new SimpleDictionary
  def cat(str:String) = dict.getCategory(str).get

  def converter = new EnglishParseTreeConverter(dict)

  def getParsedTree(lineStr:String = line): ParseTree[NodeLabel] = {
    val reader = new EnglishCCGBankReader(dict)
    val stringTrees = reader.readParseTree(lineStr, true)
    converter.toLabelTree(stringTrees)
  }

  test("read sentence test") {
    val reader = new EnglishCCGBankReader(dict)

    val sentence = converter.toSentenceFromStringTree(reader.readParseTree(line, true))
    sentence.word(0) should equal (dict.getWord("Pierre"))
    sentence.pos(0) should equal (dict.getPoS("NNP"))
    sentence.cat(0) should equal (cat("N/N"))
  }
  test("read with derivation test") {
    val tree = getParsedTree(line)
    tree.children.size should equal (2)
    tree.label.category should equal (cat("S[dcl]"))

    tree.children match {
      case BinaryTree(left, right, label) :: LeafTree(leafLabel: TerminalLabel) :: Nil => {
        left.label.category should equal (cat("NP"))
        right.label.category should equal (cat("S[dcl]\\NP"))
        label.category should equal (cat("S[dcl]"))

        leafLabel.word should equal (dict.getWord("."))
        leafLabel.baseForm should equal (dict.getWord("."))
        leafLabel.pos should equal (dict.getPoS("."))
        leafLabel.category should equal (cat("."))
      }
      case _ => fail
    }
  }
}
