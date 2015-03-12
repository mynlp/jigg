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

import jigg.nlp.ccg.lexicon.{JapaneseDictionary, CCGBankReader, ParseTree, GoldSuperTaggedSentence, Derivation, NodeLabel, JapaneseParseTreeConverter}

class ParsedSentences {
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

  val dict = new JapaneseDictionary // NOTE: dictionary is mutable, so this may be unsafe
  def converter = new JapaneseParseTreeConverter(dict)

  def cat(str:String) = dict.getCategory(str).get

  def parseTree = getParsedTree(line)
  def simpleParseTree = getParsedTree(simpleLine)

  def sentenceAndDerivation: (GoldSuperTaggedSentence, Derivation) = {
    val tree = getParsedTree(line)
    (converter.toSentenceFromLabelTree(tree), converter.toDerivation(tree))
  }
  def simpleSentenceAndDerivation: (GoldSuperTaggedSentence, Derivation) = {
    val tree = getParsedTree(simpleLine)
    (converter.toSentenceFromLabelTree(tree), converter.toDerivation(tree))
  }

  def getParsedTree(lineStr:String): ParseTree[NodeLabel] = {
    val reader = new CCGBankReader(dict)
    val stringTrees = reader.readParseTree(lineStr, true)
    converter.toLabelTree(stringTrees)
  }
}
