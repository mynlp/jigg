package jigg.pipeline

import java.util.Properties
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class DocumentKNPAnnotatorTest extends FunSuite {
  def newKNP(p: Properties = new Properties) = try Some(new DocumentKNPAnnotator("knp", p))
  catch { case e: Throwable => None }

  test("getCoreferences 1"){
    val docNode = <document id="d0"><sentences><sentence id="s0">太郎が走る。<tokens><token features="&quot;人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう&quot; &lt;人名:日本:名:45:0.00106&gt;&lt;疑似代表表記&gt;&lt;代表表記:太郎/たろう&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;文頭&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;&lt;係:ガ格&gt;&lt;NE:PERSON:S&gt;" inflectionFormId="0" inflectionTypeId="0" pos1Id="5" posId="6" reading="たろう" base="太郎" inflectionForm="*" inflectionType="*" pos1="人名" pos="名詞" surf="太郎" id="s0_tok0"/><token features="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;" inflectionFormId="0" inflectionTypeId="0" pos1Id="1" posId="9" reading="が" base="が" inflectionForm="*" inflectionType="*" pos1="格助詞" pos="助詞" surf="が" id="s0_tok1"/><token features="&quot;代表表記:走る/はしる&quot; &lt;代表表記:走る/はしる&gt;&lt;正規化代表表記:走る/はしる&gt;&lt;表現文末&gt;&lt;かな漢字&gt;&lt;活用語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;" inflectionFormId="2" inflectionTypeId="10" pos1Id="0" posId="2" reading="はしる" base="走る" inflectionForm="基本形" inflectionType="子音動詞ラ行" pos1="*" pos="動詞" surf="走る" id="s0_tok2"/><token features="NIL &lt;文末&gt;&lt;英記号&gt;&lt;記号&gt;&lt;付属&gt;" inflectionFormId="0" inflectionTypeId="0" pos1Id="1" posId="1" reading="。" base="。" inflectionForm="*" inflectionType="*" pos1="句点" pos="特殊" surf="。" id="s0_tok3"/></tokens><basicPhrases><basicPhrase features="&lt;文頭&gt;&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;照応詞候補:太郎&gt;&lt;解析格:ガ&gt;&lt;EID:0&gt;" tokens="s0_tok0 s0_tok1" id="s0_bp0"/><basicPhrase features="&lt;文末&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:走る/はしる&gt;&lt;用言代表表記:走る/はしる&gt;&lt;時制-未来&gt;&lt;主題格:一人称優位&gt;&lt;格関係0:ガ:太郎&gt;&lt;格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/d0-s0;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-&gt;&lt;EID:1&gt;&lt;述語項構造:走る/はしる:動13:ガ/C/太郎/0&gt;" tokens="s0_tok2 s0_tok3" id="s0_bp1"></basicPhrase></basicPhrases></sentence><sentence id="s1">太郎が歩く。<tokens><token features="&quot;人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう&quot; &lt;人名:日本:名:45:0.00106&gt;&lt;疑似代表表記&gt;&lt;代表表記:太郎/たろう&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;文頭&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;&lt;係:ガ格&gt;&lt;NE:PERSON:S&gt;" inflectionFormId="0" inflectionTypeId="0" pos1Id="5" posId="6" reading="たろう" base="太郎" inflectionForm="*" inflectionType="*" pos1="人名" pos="名詞" surf="太郎" id="s1_tok0"/><token features="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;" inflectionFormId="0" inflectionTypeId="0" pos1Id="1" posId="9" reading="が" base="が" inflectionForm="*" inflectionType="*" pos1="格助詞" pos="助詞" surf="が" id="s1_tok1"/><token features="&quot;代表表記:歩く/あるく&quot; &lt;代表表記:歩く/あるく&gt;&lt;正規化代表表記:歩く/あるく&gt;&lt;表現文末&gt;&lt;かな漢字&gt;&lt;活用語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;" inflectionFormId="2" inflectionTypeId="2" pos1Id="0" posId="2" reading="あるく" base="歩く" inflectionForm="基本形" inflectionType="子音動詞カ行" pos1="*" pos="動詞" surf="歩く" id="s1_tok2"/><token features="NIL &lt;文末&gt;&lt;英記号&gt;&lt;記号&gt;&lt;付属&gt;" inflectionFormId="0" inflectionTypeId="0" pos1Id="1" posId="1" reading="。" base="。" inflectionForm="*" inflectionType="*" pos1="句点" pos="特殊" surf="。" id="s1_tok3"/></tokens><basicPhrases><basicPhrase features="&lt;文頭&gt;&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;照応詞候補:太郎&gt;&lt;解析格:ガ&gt;&lt;C用;【太郎】;=;1;0;9.99:d0-s0(1文前):0文節&gt;&lt;共参照&gt;&lt;COREFER_ID:1&gt;&lt;REFERRED:1-0&gt;&lt;EID:0&gt;" tokens="s1_tok0 s1_tok1" id="s1_bp0"/><basicPhrase features="&lt;文末&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:歩く/あるく&gt;&lt;用言代表表記:歩く/あるく&gt;&lt;時制-未来&gt;&lt;主題格:一人称優位&gt;&lt;格関係0:ガ:太郎&gt;&lt;格解析結果:歩く/あるく:動12:ガ/C/太郎/0/0/d0-s1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;トスル/U/-/-/-/-;ニツク/U/-/-/-/-;ニムケル/U/-/-/-/-;ニソウ/U/-/-/-/-;トイウ/U/-/-/-/-&gt;&lt;EID:2&gt;&lt;述語項構造:歩く/あるく:動12:ガ/C/太郎/0&gt;" tokens="s1_tok2 s1_tok3" id="s1_bp1"></basicPhrase></basicPhrases></sentence></sentences></document>

    val expected = <coreferences><coreference id="d0_coref0" basicPhrases="s0_bp0 s1_bp0" /><coreference id="d0_coref1" basicPhrases="s0_bp1" /><coreference id="d0_coref2" basicPhrases="s1_bp1" /></coreferences>

    newKNP() foreach { knp =>
      knp.getCoreferences(docNode) should be (expected)
    }
  }

  test("getCoreferences 2"){
    val docNode = <document id="d0"><sentences><sentence id="s0">太郎は太郎だ<basicPhrases><basicPhrase features="&lt;文頭&gt;&lt;人名&gt;&lt;ハ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:未格&gt;&lt;提題&gt;&lt;区切:3-5&gt;&lt;主題表現&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;照応詞候補:太郎&gt;&lt;解析格:ガ&gt;&lt;COREFER_ID:1&gt;&lt;EID:0&gt;" tokens="s0_tok0 s0_tok1" id="s0_bp0"/><basicPhrase features="&lt;文末&gt;&lt;人名&gt;&lt;体言&gt;&lt;用言:判&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;状態述語&gt;&lt;判定詞&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;用言代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;時制-現在&gt;&lt;時制-無時制&gt;&lt;照応詞候補:太郎&gt;&lt;格関係0:ガ:太郎&gt;&lt;格解析結果:太郎/たろう:判13:ガ/N/太郎/0/0/d0-s0;外の関係/U/-/-/-/-&gt;&lt;C用;【太郎】;=;0;0;9.99:d0-s0(同一文):0文節&gt;&lt;共参照&gt;&lt;COREFER_ID:1&gt;&lt;EID:0&gt;" tokens="s0_tok2 s0_tok3" id="s0_bp1"></basicPhrase></basicPhrases></sentence></sentences></document>
    val expected = <coreferences><coreference id="d0_coref0" basicPhrases="s0_bp0 s0_bp1" /></coreferences>

    newKNP() foreach { knp =>
      knp.getCoreferences(docNode) should be (expected)
    }
  }

  test("getPredicateArgumentRelations 1"){
    val sentenceNode = <sentence id="s0">太郎が走る。<basicPhrases><basicPhrase features="&lt;文頭&gt;&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;照応詞候補:太郎&gt;&lt;解析格:ガ&gt;&lt;EID:0&gt;" tokens="s0_tok0 s0_tok1" id="s0_bp0"/><basicPhrase features="&lt;文末&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:走る/はしる&gt;&lt;用言代表表記:走る/はしる&gt;&lt;時制-未来&gt;&lt;主題格:一人称優位&gt;&lt;格関係0:ガ:太郎&gt;&lt;格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/d0-s0;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-&gt;&lt;EID:1&gt;&lt;述語項構造:走る/はしる:動13:ガ/C/太郎/0&gt;" tokens="s0_tok2 s0_tok3" id="s0_bp1" /></basicPhrases></sentence>

    val expected = <predicateArgumentRelations><predicateArgumentRelation id="s0_par0" predicate="s0_bp1" argument="d0_coref0" label="ガ" flag="C"/></predicateArgumentRelations>

    newKNP() foreach { knp =>
      knp.getPredicateArgumentRelations(sentenceNode, "d0") should be (expected)
    }
  }

  test("getPredicateArgumentRelations 2"){
    val sentenceNode = <sentence id="s0">麻生太郎がコーヒーを飲んだ。<basicPhrases><basicPhrase features="&lt;文節内&gt;&lt;係:文節内&gt;&lt;文頭&gt;&lt;人名疑&gt;&lt;地名疑&gt;&lt;体言&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:麻生/あそう?麻生/あさお&gt;&lt;NE内:PERSON&gt;&lt;EID:0&gt;" tokens="s0_tok0" id="s0_bp0"/><basicPhrase features="&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:麻生太郎&gt;&lt;Wikipedia上位語:政治家&gt;&lt;Wikipediaエントリ:麻生太郎&gt;&lt;照応詞候補:麻生太郎&gt;&lt;解析格:ガ&gt;&lt;EID:1&gt;" tokens="s0_tok1 s0_tok2" id="s0_bp1"/><basicPhrase features="&lt;ヲ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ヲ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:珈琲/こーひー&gt;&lt;照応詞候補:コーヒー&gt;&lt;解析格:ヲ&gt;&lt;EID:2&gt;" tokens="s0_tok3 s0_tok4" id="s0_bp2"/><basicPhrase features="&lt;文末&gt;&lt;時制-過去&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:飲む/のむ&gt;&lt;用言代表表記:飲む/のむ&gt;&lt;主題格:一人称優位&gt;&lt;格関係1:ガ:太郎&gt;&lt;格関係2:ヲ:コーヒー&gt;&lt;格解析結果:飲む/のむ:動1:ガ/C/太郎/1/0/d0-s0;ヲ/C/コーヒー/2/0/d0-s0;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;トスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ニツク/U/-/-/-/-;ニアワセル/U/-/-/-/-;トイウ/U/-/-/-/-;ニクワエル/U/-/-/-/-;ヲツウジル/U/-/-/-/-&gt;&lt;EID:3&gt;&lt;述語項構造:飲む/のむ:動1:ガ/C/麻生太郎/1;ヲ/C/コーヒー/2&gt;" tokens="s0_tok5 s0_tok6" id="s0_bp3"></basicPhrase></basicPhrases></sentence>

    //<述語項構造:飲む/のむ:動1:ガ/C/麻生太郎/1;ヲ/C/コーヒー/2>

    val expected = <predicateArgumentRelations><predicateArgumentRelation id="s0_par0" predicate="s0_bp3" argument="d0_coref1" label="ガ" flag="C"/><predicateArgumentRelation id="s0_par1" predicate="s0_bp3" argument="d0_coref2" label="ヲ" flag="C"/></predicateArgumentRelations>

    newKNP() foreach { knp =>
      knp.getPredicateArgumentRelations(sentenceNode, "d0") should be (expected)
    }
  }

  test("getPredicateArgumentRelations 3"){
    //「りんごはおいしかったけれど、値段が少し高かった」
    val sentenceNode = <sentence id="s0">りんごはおいしかったけれど、値段が少し高かった<basicPhrases><basicPhrase features="&lt;文頭&gt;&lt;ハ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:未格&gt;&lt;提題&gt;&lt;区切:3-5&gt;&lt;主題表現&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:林檎/りんご&gt;&lt;照応詞候補:りんご&gt;&lt;解析格:ガ&gt;&lt;EID:0&gt;" tokens="s0_tok0 s0_tok1" id="s0_bp0"/><basicPhrase features="&lt;時制-過去&gt;&lt;読点&gt;&lt;助詞&gt;&lt;用言:形&gt;&lt;係:連用&gt;&lt;レベル:C&gt;&lt;並キ:述:&amp;ST:3.0&amp;&amp;&amp;レベル:B&gt;&lt;区切:3-5&gt;&lt;ID:〜けれども&gt;&lt;提題受:25&gt;&lt;連用要素&gt;&lt;連用節&gt;&lt;状態述語&gt;&lt;正規化代表表記:美味しい/おいしい&gt;&lt;用言代表表記:美味しい/おいしい&gt;&lt;主題格:一人称優位&gt;&lt;格関係0:ガ:りんご&gt;&lt;格解析結果:美味しい/おいしい:形18:ガ/N/りんご/0/0/d0-s0;ニ/U/-/-/-/-;ヨリ/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-&gt;&lt;EID:1&gt;&lt;述語項構造:美味しい/おいしい:形18:ガ/N/りんご/0&gt;" tokens="s0_tok2 s0_tok3 s0_tok4" id="s0_bp1"/><basicPhrase features="&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:値段/ねだん&gt;&lt;照応詞候補:値段&gt;&lt;解析格:ガ&gt;&lt;EID:2&gt;&lt;述語項構造:値段/ねだん:名1:ノ/O/りんご/0&gt;" tokens="s0_tok5 s0_tok6" id="s0_bp2"/><basicPhrase features="&lt;相対名詞修飾&gt;&lt;用言弱修飾&gt;&lt;数量&gt;&lt;副詞&gt;&lt;修飾&gt;&lt;係:連用&gt;&lt;区切:0-4&gt;&lt;連用要素&gt;&lt;連用節&gt;&lt;省略解析なし&gt;&lt;正規化代表表記:少し/すこし&gt;&lt;解析格:修飾&gt;&lt;EID:3&gt;" tokens="s0_tok7" id="s0_bp3"/><basicPhrase features="&lt;文末&gt;&lt;時制-過去&gt;&lt;用言:形&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;状態述語&gt;&lt;正規化代表表記:高い/たかい&gt;&lt;用言代表表記:高い/たかい&gt;&lt;主題格:一人称優位&gt;&lt;格関係2:ガ:値段&gt;&lt;格関係3:修飾:少し&gt;&lt;格解析結果:高い/たかい:形5:ガ/C/値段/2/0/d0-s0;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/C/少し/3/0/d0-s0;ノ/U/-/-/-/-;ガ２/U/-/-/-/-;ニクラベル/U/-/-/-/-;トスル/U/-/-/-/-;ニタイスル/U/-/-/-/-;ニトル/U/-/-/-/-;トイウ/U/-/-/-/-;ニヨル/U/-/-/-/-;ニツク/U/-/-/-/-;ニオク/U/-/-/-/-&gt;&lt;EID:4&gt;&lt;述語項構造:高い/たかい:形5:ガ/C/値段/2;修飾/C/少し/3&gt;" tokens="s0_tok8" id="s0_bp4"></basicPhrase></basicPhrases></sentence>

    // <述語項構造:美味しい/おいしい:形18:ガ/N/りんご/0>
    // <述語項構造:値段/ねだん:名1:ノ/O/りんご/0>
    // <述語項構造:高い/たかい:形5:ガ/C/値段/2;修飾/C/少し/3>

    val expected = <predicateArgumentRelations><predicateArgumentRelation id="s0_par0" predicate="s0_bp1" argument="d0_coref0" label="ガ" flag="N"/><predicateArgumentRelation id="s0_par1" predicate="s0_bp2" argument="d0_coref0" label="ノ" flag="O"/><predicateArgumentRelation id="s0_par2" predicate="s0_bp4" argument="d0_coref2" label="ガ" flag="C"/><predicateArgumentRelation id="s0_par3" predicate="s0_bp4" argument="d0_coref3" label="修飾" flag="C"/></predicateArgumentRelations>

    newKNP() foreach { knp =>
      knp.getPredicateArgumentRelations(sentenceNode, "d0") should be (expected)
    }
  }

}
