package enju.ccg.parser

import enju.ccg.lexicon.{AVMInitializer, JapaneseDictionary, CCGBankReader, ParseTree, GoldSuperTaggedSentence, Derivation}

class ParsedSentences {
  AVMInitializer.init

  val line = "{< S[nm,base] {< S[nm,base] {< NP[ga,nm] {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 村山/村山/名詞-固有名詞-人名-姓/_} {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 富市/富市/名詞-固有名詞-人名-名/_} {NP[nc,nm] 首相/首相/名詞-一般/_}}} {NP[ga,nm]＼NP[nc,nm]sem は/は/助詞-係助詞/_}} {> S[nm,base]＼NP[ga,nm] {ADV S[nm,base]1／S[nm,base]1 {< S[adv,cont] {> S[adv,cont] {ADV S[adv,cont]1／S[adv,cont]1 {> S[adv,cont] {< S[adv,cont]1／S[adv,cont]1 {NP[nc,nm] 年頭/年頭/名詞-一般/_} {(S1／S1)＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}} {S[adv,cont] あたり/あたる/動詞-自立/連用形}}} {> S[adv,cont] {< S[adv,cont]1／S[adv,cont]1 {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 首相/首相/名詞-一般/_} {NP[nc,nm] 官邸/官邸/名詞-一般/_}} {(S1／S1)＼NP[nc,nm]sem で/で/助詞-格助詞-一般/_}} {> S[adv,cont] {< S[adv,cont]1／S[adv,cont]1 {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 内閣/内閣/名詞-一般/_} {< NP[nc,nm] {NP[nc,nm] 記者/記者/名詞-一般/_} {NP[nc,nm]＼NP[nc,nm] 会/会/名詞-接尾-一般/_}}} {(S1／S1)＼NP[nc,nm] と/と/助詞-並立助詞/_}} {> S[adv,cont] {ADV S[adv,cont]1／S[adv,cont]1 {< NP[nc,adv] {> NP[nc,nm] {> NP[nc,nm]1／NP[nc,nm]1 {NP[nc,nm]1／NP[nc,nm]1 二/二/名詞-数/_} {NP[nc,nm]1／NP[nc,nm]1 十/十/名詞-数/_}} {NP[nc,nm] 八/八/名詞-数/_}} {NP[nc,adv]＼NP[nc,nm] 日/日/名詞-接尾-助数詞/_}}} {< S[adv,cont] {S[nm,stem] 会見/会見/名詞-サ変接続/_} {S[adv,cont]＼S[nm,stem]sem し/する/動詞-自立/連用形}}}}}} {S1＼S1 、/、/記号-読点/_}}} {> S[nm,base]＼NP[ga,nm] {ADV S[nm,base]1／S[nm,base]1 {< S[adv,cont] {> S[adv,cont] {ADV S[adv,cont]1／S[adv,cont]1 {> S[adv,cont] {< S[adv,cont]1／S[adv,cont]1 {> NP[nc,nm] {< NP[nc,nm]1／NP[nc,nm]1 {> NP[nc,nm] {< NP[nc,nm]1／NP[nc,nm]1 {NP[nc,nm] 社会党/社会党/名詞-固有名詞-組織/_} {(NP[nc,nm]1／NP[nc,nm]1)＼NP[nc,nm] の/の/助詞-連体化/_}} {> NP[nc,nm] {> NP[nc,nm]1／NP[nc,nm]1 {NP[nc,nm]1／NP[nc,nm]1 新/新/接頭詞-名詞接続/_} {NP[nc,nm]1／NP[nc,nm]1 民主/民主/名詞-一般/_}} {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 連合/連合/名詞-サ変接続/_} {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 所属/所属/名詞-サ変接続/_} {NP[nc,nm] 議員/議員/名詞-一般/_}}}}} {(NP[nc,nm]1／NP[nc,nm]1)＼NP[nc,nm] の/の/助詞-連体化/_}} {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 離党/離党/名詞-サ変接続/_} {NP[nc,nm] 問題/問題/名詞-ナイ形容詞語幹/_}}} {(S1／S1)＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}} {< S[adv,cont] {S[nm,cont] つい/つく/動詞-自立/連用タ接続} {S[adv,cont]＼S[nm,cont]sem て/て/助詞-接続助詞/_}}}} {< S[adv,cont] {< NP[to,nm] {< S[nm,base] {> S[nm,base] {S1／S1 「/「/記号-括弧開/_} {Φ S[nm,base] {< S[nm,base] {< S[nm,base] {< NP[ni,nm] {< NP[ni,nm] {> NP[nc,nm] {ADN NP[nc,nm]1／NP[nc,nm]1 {< S[adn,base] {< NP[ni,nm] {NP[nc,nm] 政権/政権/名詞-一般/_} {NP[ni,nm]＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}} {< S[adn,base]＼NP[ni,nm] {< NP[o,nm] {NP[nc,nm] 影響/影響/名詞-サ変接続/_} {NP[o,nm]＼NP[nc,nm]sem を/を/助詞-格助詞-一般/_}} {(S[adn,base]＼NP[ni,nm,ni])＼NP[o,nm,o] 及ぼす/及ぼす/動詞-自立/基本形}}}} {NP[nc,nm] こと/こと/名詞-非自立-一般/_}} {NP[ni,nm]＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}} {NP[ni,nm]＼NP[ni,nm]sem は/は/助詞-係助詞/_}} {< S[nm,base]＼NP[ni,nm] {S[nm,neg]＼NP[ni,nm,ni] なら/なる/動詞-非自立/未然形} {S[nm,base]＼S[nm,neg]sem ない/ない/助動詞/基本形}}} {S1＼S1 。/。/記号-句点/_}} {< S[nm,base] {< NP[to,nm] {> S[nm,base] {< S[nm,base]1／S[nm,base]1 {< S[nm,base]1／S[nm,base]1 {< S[nm,cont] {< NP[ga,nm] {< NP[nc,nm] {NP[nc,nm] 離党/離党/名詞-サ変接続/_} {NP[nc,nm]＼NP[nc,nm] 者/者/名詞-接尾-一般/_}} {NP[ga,nm]＼NP[nc,nm]sem が/が/助詞-格助詞-一般/_}} {< S[nm,cont]＼NP[ga,nm] {S[nm,cont]＼NP[ga,nm,ga] い/いる/動詞-自立/連用形} {S[nm,cont]＼S[nm,cont]sem て/て/助詞-接続助詞/_}}} {(S1／S1)＼S[nm,cont]sem も/も/助詞-係助詞/_}} {(S2／S2)1＼(S3／S3)1 、/、/記号-読点/_}} {< S[nm,base] {< NP[ni,nm] {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 その/その/連体詞/_} {NP[nc,nm] 範囲/範囲/名詞-一般/_}} {NP[ni,nm]＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}} {S[nm,base]＼NP[ni,nm,ni] とどまる/とどまる/動詞-自立/基本形}}} {NP[to,nm]＼S[nm,base]sem と/と/助詞-格助詞-引用/_}} {S[nm,base]＼NP[to,nm,to] 思う/思う/動詞-自立/基本形}}}} {S1＼S1 」/」/記号-括弧閉/_}} {NP[to,nm]＼S[nm,base]sem と/と/助詞-格助詞-引用/_}} {S[adv,cont]＼NP[to,nm,to] 述べ/述べる/動詞-自立/連用形}}} {S1＼S1 、/、/記号-読点/_}}} {< S[nm,base]＼NP[ga,nm] {< NP[o,nm] {> NP[nc,nm] {< NP[nc,nm]1／NP[nc,nm]1 {< NP[nc,nm] {< S[nm,base] {< NP[ni,nm] {< NP[ni,nm] {> NP[nc,nm] {NP[nc,nm]1／NP[nc,nm]1 大量/大量/名詞-形容動詞語幹/_} {NP[nc,nm] 離党/離党/名詞-サ変接続/_}} {NP[ni,nm]＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}} {NP[ni,nm]＼NP[ni,nm]sem は/は/助詞-係助詞/_}} {< S[nm,base]＼NP[ni,nm] {S[nm,neg]＼NP[ni,nm,ni] 至ら/至る/動詞-自立/未然形} {S[nm,base]＼S[nm,neg]sem ない/ない/助動詞/基本形}}} {NP[nc,nm]＼S[nm,base]sem と/と/助詞-格助詞-引用/_}} {(NP[nc,nm]1／NP[nc,nm]1)＼NP[nc,nm] の/の/助詞-連体化/_}} {NP[nc,nm] 見通し/見通し/名詞-一般/_}} {NP[o,nm]＼NP[nc,nm]sem を/を/助詞-格助詞-一般/_}} {< (S[nm,base]＼NP[ga,nm])＼NP[o,nm] {(S[nm,cont]＼NP[ga,nm,ga])＼NP[o,nm,o] 示し/示す/動詞-自立/連用形} {S[nm,base]＼S[nm,cont]sem た/た/助動詞/基本形}}}}}} {S1＼S1 。/。/記号-句点/_}}"

  val simpleLine = """
{> NP[nc,nm]
  {ADN NP[nc,nm]1／NP[nc,nm]1 
    {< S[adn,base] 
      {< NP[ni,nm] 
        {NP[nc,nm] 政権/政権/名詞-一般/_} 
        {NP[ni,nm]＼NP[nc,nm]sem に/に/助詞-格助詞-一般/_}
      } 
      {< S[adn,base]＼NP[ni,nm] 
        {< NP[o,nm] 
          {NP[nc,nm] 影響/影響/名詞-サ変接続/_} 
          {NP[o,nm]＼NP[nc,nm]sem を/を/助詞-格助詞-一般/_}
        } 
        {(S[adn,base]＼NP[ni,nm,ni])＼NP[o,nm,o] 及ぼす/及ぼす/動詞-自立/基本形}
      }
    }
  } 
  {NP[nc,nm] こと/こと/名詞-非自立-一般/_}
}"""
  val dict = new JapaneseDictionary // NOTE: dictionary is mutable, so this may be unsafe
  def cat(str:String) = dict.getCategory(str).get
  
  def parseTree = getParsedTree(line)
  def simpleParseTree = getParsedTree(simpleLine)

  def sentenceAndDerivation: (GoldSuperTaggedSentence, Derivation) = {
    val reader = new CCGBankReader(dict)
    reader.readSentenceAndDerivation(line, true)
  }
  def simpleSentenceAndDerivation: (GoldSuperTaggedSentence, Derivation) = {
    val reader = new CCGBankReader(dict)
    reader.readSentenceAndDerivation(simpleLine, true)
  }
  def getParsedTree(lineStr:String): ParseTree[CCGBankReader#JapaneseLeafItem] = {
    val reader = new CCGBankReader(dict)
    val parser = reader.japaneseTreeParser(true)
    val parseTrees = parser.parse(lineStr)
    parseTrees(0)
  }  
}
