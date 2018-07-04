import sys
sys.path.append(".checker/tests")

from basetest import BaseTest


class TestKNP(BaseTest):
    '''
    '''
    def setUp(self):
        self.input_text = "太郎はこの本を二郎を見た女性に渡した。"

        self.expected_text = """<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document id="d0">
    <sentences>
      <sentence characterOffsetEnd="19" characterOffsetBegin="0" id="s0">
        太郎はこの本を二郎を見た女性に渡した。
        <tokens annotators="knp">
          <token misc="&quot;人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう&quot; &lt;人名:日本:名:45:0.00106&gt;&lt;疑似代表表記&gt;&lt;代表表記:太郎/たろう&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;文頭&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;&lt;NE:PERSON:S&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="5" pos1="人名" posId="6" pos="名詞" lemma="太郎" yomi="たろう" characterOffsetEnd="2" characterOffsetBegin="0" form="太郎" id="s0_knpt0"/>
          <token misc="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="2" pos1="副助詞" posId="9" pos="助詞" lemma="は" yomi="は" characterOffsetEnd="3" characterOffsetBegin="2" form="は" id="s0_knpt1"/>
          <token misc="&quot;疑似代表表記 代表表記:この/この&quot; &lt;疑似代表表記&gt;&lt;代表表記:この/この&gt;&lt;正規化代表表記:この/この&gt;&lt;かな漢字&gt;&lt;ひらがな&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="2" pos1="連体詞形態指示詞" posId="7" pos="指示詞" lemma="この" yomi="この" characterOffsetEnd="5" characterOffsetBegin="3" form="この" id="s0_knpt2"/>
          <token misc="&quot;代表表記:本/ほん 漢字読み:音 カテゴリ:人工物-その他;抽象物&quot; &lt;代表表記:本/ほん&gt;&lt;漢字読み:音&gt;&lt;カテゴリ:人工物-その他;抽象物&gt;&lt;正規化代表表記:本/ほん&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="普通名詞" posId="6" pos="名詞" lemma="本" yomi="ほん" characterOffsetEnd="6" characterOffsetBegin="5" form="本" id="s0_knpt3"/>
          <token misc="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="格助詞" posId="9" pos="助詞" lemma="を" yomi="を" characterOffsetEnd="7" characterOffsetBegin="6" form="を" id="s0_knpt4"/>
          <token misc="&quot;人名:日本:名:150:0.00065 疑似代表表記 代表表記:二郎/じろう&quot; &lt;人名:日本:名:150:0.00065&gt;&lt;疑似代表表記&gt;&lt;代表表記:二郎/じろう&gt;&lt;正規化代表表記:二郎/じろう&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="5" pos1="人名" posId="6" pos="名詞" lemma="二郎" yomi="じろう" characterOffsetEnd="9" characterOffsetBegin="7" form="二郎" id="s0_knpt5"/>
          <token misc="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="格助詞" posId="9" pos="助詞" lemma="を" yomi="を" characterOffsetEnd="10" characterOffsetBegin="9" form="を" id="s0_knpt6"/>
          <token misc="&quot;代表表記:見る/みる 補文ト 自他動詞:自:見える/みえる&quot; &lt;代表表記:見る/みる&gt;&lt;補文ト&gt;&lt;自他動詞:自:見える/みえる&gt;&lt;正規化代表表記:見る/みる&gt;&lt;かな漢字&gt;&lt;活用語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;" cFormId="10" cForm="タ形" cTypeId="1" cType="母音動詞" pos1Id="0" pos1="*" posId="2" pos="動詞" lemma="見る" yomi="みた" characterOffsetEnd="12" characterOffsetBegin="10" form="見た" id="s0_knpt7"/>
          <token misc="&quot;代表表記:女性/じょせい カテゴリ:人&quot; &lt;代表表記:女性/じょせい&gt;&lt;カテゴリ:人&gt;&lt;正規化代表表記:女性/じょせい&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;肩書同格&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="普通名詞" posId="6" pos="名詞" lemma="女性" yomi="じょせい" characterOffsetEnd="14" characterOffsetBegin="12" form="女性" id="s0_knpt8"/>
          <token misc="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="格助詞" posId="9" pos="助詞" lemma="に" yomi="に" characterOffsetEnd="15" characterOffsetBegin="14" form="に" id="s0_knpt9"/>
          <token misc="&quot;代表表記:渡す/わたす 付属動詞候補（基本） 自他動詞:自:渡る/わたる&quot; &lt;代表表記:渡す/わたす&gt;&lt;付属動詞候補（基本）&gt;&lt;自他動詞:自:渡る/わたる&gt;&lt;正規化代表表記:渡す/わたす&gt;&lt;表現文末&gt;&lt;かな漢字&gt;&lt;活用語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;" cFormId="10" cForm="タ形" cTypeId="5" cType="子音動詞サ行" pos1Id="0" pos1="*" posId="2" pos="動詞" lemma="渡す" yomi="わたした" characterOffsetEnd="18" characterOffsetBegin="15" form="渡した" id="s0_knpt10"/>
          <token misc="NIL &lt;文末&gt;&lt;英記号&gt;&lt;記号&gt;&lt;付属&gt;" cFormId="0" cForm="*" cTypeId="0" cType="*" pos1Id="1" pos1="句点" posId="1" pos="特殊" lemma="。" yomi="。" characterOffsetEnd="19" characterOffsetBegin="18" form="。" id="s0_knpt11"/>
        </tokens>
        <basicPhrases annotators="knp">
          <basicPhrase misc="&lt;文頭&gt;&lt;人名&gt;&lt;ハ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:未格&gt;&lt;提題&gt;&lt;区切:3-5&gt;&lt;主題表現&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;解析格:ガ&gt;" tokens="s0_knpt0 s0_knpt1" id="s0_knpbp0"/>
          <basicPhrase misc="&lt;連体修飾&gt;&lt;連体詞形態指示詞&gt;&lt;係:連体&gt;&lt;区切:0-4&gt;&lt;正規化代表表記:この/この&gt;" tokens="s0_knpt2" id="s0_knpbp1"/>
          <basicPhrase misc="&lt;ヲ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;一文字漢字&gt;&lt;係:ヲ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:本/ほん&gt;&lt;解析格:ヲ&gt;" tokens="s0_knpt3 s0_knpt4" id="s0_knpbp2"/>
          <basicPhrase misc="&lt;人名&gt;&lt;ヲ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ヲ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:二郎/じろう&gt;&lt;解析格:ヲ&gt;" tokens="s0_knpt5 s0_knpt6" id="s0_knpbp3"/>
          <basicPhrase misc="&lt;補文ト&gt;&lt;時制-過去&gt;&lt;連体修飾&gt;&lt;用言:動&gt;&lt;係:連格&gt;&lt;レベル:B&gt;&lt;区切:0-5&gt;&lt;ID:（動詞連体）&gt;&lt;連体節&gt;&lt;動態述語&gt;&lt;正規化代表表記:見る/みる&gt;&lt;用言代表表記:見る/みる&gt;&lt;格関係3:ヲ:二郎&gt;&lt;格関係5:ガ:女性&gt;&lt;格解析結果:見る/みる:動5:ガ/N/女性/5/0/2;ヲ/C/二郎/3/0/2;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;ヲツウジル/U/-/-/-/-;トスル/U/-/-/-/-;ニムケル/U/-/-/-/-&gt;" tokens="s0_knpt7" id="s0_knpbp4"/>
          <basicPhrase misc="&lt;SM-主体&gt;&lt;SM-人&gt;&lt;ニ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ニ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:女性/じょせい&gt;&lt;解析連格:ガ&gt;&lt;解析格:ニ&gt;" tokens="s0_knpt8 s0_knpt9" id="s0_knpbp5"/>
          <basicPhrase misc="&lt;文末&gt;&lt;時制-過去&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:渡す/わたす&gt;&lt;用言代表表記:渡す/わたす&gt;&lt;主題格:一人称優位&gt;&lt;格関係0:ガ:太郎&gt;&lt;格関係2:ヲ:本&gt;&lt;格関係5:ニ:女性&gt;&lt;格解析結果:渡す/わたす:動2:ガ/N/太郎/0/0/2;ヲ/C/本/2/0/2;ニ/C/女性/5/0/2;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニツク/U/-/-/-/-;ヲツウジル/U/-/-/-/-;ニヨル/U/-/-/-/-;ニトル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ニアワセル/U/-/-/-/-&gt;" tokens="s0_knpt10 s0_knpt11" id="s0_knpbp6">
</basicPhrase>
        </basicPhrases>
        <chunks annotators="knp">
          <chunk misc="&lt;文頭&gt;&lt;人名&gt;&lt;ハ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:未格&gt;&lt;提題&gt;&lt;区切:3-5&gt;&lt;主題表現&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;主辞代表表記:太郎/たろう&gt;" tokens="s0_knpt0 s0_knpt1" id="s0_knpc0"/>
          <chunk misc="&lt;連体修飾&gt;&lt;連体詞形態指示詞&gt;&lt;係:連体&gt;&lt;区切:0-4&gt;&lt;正規化代表表記:この/この&gt;&lt;主辞代表表記:この/この&gt;" tokens="s0_knpt2" id="s0_knpc1"/>
          <chunk misc="&lt;ヲ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;一文字漢字&gt;&lt;係:ヲ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:本/ほん&gt;&lt;主辞代表表記:本/ほん&gt;" tokens="s0_knpt3 s0_knpt4" id="s0_knpc2"/>
          <chunk misc="&lt;人名&gt;&lt;ヲ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ヲ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:二郎/じろう&gt;&lt;主辞代表表記:二郎/じろう&gt;" tokens="s0_knpt5 s0_knpt6" id="s0_knpc3"/>
          <chunk misc="&lt;補文ト&gt;&lt;時制-過去&gt;&lt;連体修飾&gt;&lt;用言:動&gt;&lt;係:連格&gt;&lt;レベル:B&gt;&lt;区切:0-5&gt;&lt;ID:（動詞連体）&gt;&lt;連体節&gt;&lt;動態述語&gt;&lt;正規化代表表記:見る/みる&gt;&lt;主辞代表表記:見る/みる&gt;" tokens="s0_knpt7" id="s0_knpc4"/>
          <chunk misc="&lt;SM-主体&gt;&lt;SM-人&gt;&lt;ニ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ニ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:女性/じょせい&gt;&lt;主辞代表表記:女性/じょせい&gt;" tokens="s0_knpt8 s0_knpt9" id="s0_knpc5"/>
          <chunk misc="&lt;文末&gt;&lt;時制-過去&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:渡す/わたす&gt;&lt;主辞代表表記:渡す/わたす&gt;" tokens="s0_knpt10 s0_knpt11" id="s0_knpc6"/>
        </chunks>
        <dependencies annotators="knp" unit="basicPhrase">
          <dependency deprel="D" dependent="s0_knpbp0" head="s0_knpbp6" id="s0_knpbpdep0"/>
          <dependency deprel="D" dependent="s0_knpbp1" head="s0_knpbp2" id="s0_knpbpdep1"/>
          <dependency deprel="D" dependent="s0_knpbp2" head="s0_knpbp6" id="s0_knpbpdep2"/>
          <dependency deprel="D" dependent="s0_knpbp3" head="s0_knpbp4" id="s0_knpbpdep3"/>
          <dependency deprel="D" dependent="s0_knpbp4" head="s0_knpbp5" id="s0_knpbpdep4"/>
          <dependency deprel="D" dependent="s0_knpbp5" head="s0_knpbp6" id="s0_knpbpdep5"/>
          <dependency deprel="D" dependent="s0_knpbp6" head="root" id="s0_knpbpdep6"/>
        </dependencies>
        <dependencies annotators="knp" unit="chunk">
          <dependency deprel="D" dependent="s0_knpc0" head="s0_knpc6" id="s0_knpdep0"/>
          <dependency deprel="D" dependent="s0_knpc1" head="s0_knpc2" id="s0_knpdep1"/>
          <dependency deprel="D" dependent="s0_knpc2" head="s0_knpc6" id="s0_knpdep2"/>
          <dependency deprel="D" dependent="s0_knpc3" head="s0_knpc4" id="s0_knpdep3"/>
          <dependency deprel="D" dependent="s0_knpc4" head="s0_knpc5" id="s0_knpdep4"/>
          <dependency deprel="D" dependent="s0_knpc5" head="s0_knpc6" id="s0_knpdep5"/>
          <dependency deprel="D" dependent="s0_knpc6" head="root" id="s0_knpdep6"/>
        </dependencies>
        <caseRelations annotators="knp">
          <caseRelation flag="N" deprel="ガ" arg="s0_knpt8" pred="s0_knpbp4" id="s0_knpcr0"/>
          <caseRelation flag="C" deprel="ヲ" arg="s0_knpt5" pred="s0_knpbp4" id="s0_knpcr1"/>
          <caseRelation flag="U" deprel="ニ" arg="unk" pred="s0_knpbp4" id="s0_knpcr2"/>
          <caseRelation flag="U" deprel="ト" arg="unk" pred="s0_knpbp4" id="s0_knpcr3"/>
          <caseRelation flag="U" deprel="デ" arg="unk" pred="s0_knpbp4" id="s0_knpcr4"/>
          <caseRelation flag="U" deprel="カラ" arg="unk" pred="s0_knpbp4" id="s0_knpcr5"/>
          <caseRelation flag="U" deprel="ヨリ" arg="unk" pred="s0_knpbp4" id="s0_knpcr6"/>
          <caseRelation flag="U" deprel="マデ" arg="unk" pred="s0_knpbp4" id="s0_knpcr7"/>
          <caseRelation flag="U" deprel="時間" arg="unk" pred="s0_knpbp4" id="s0_knpcr8"/>
          <caseRelation flag="U" deprel="外の関係" arg="unk" pred="s0_knpbp4" id="s0_knpcr9"/>
          <caseRelation flag="U" deprel="修飾" arg="unk" pred="s0_knpbp4" id="s0_knpcr10"/>
          <caseRelation flag="U" deprel="ノ" arg="unk" pred="s0_knpbp4" id="s0_knpcr11"/>
          <caseRelation flag="U" deprel="ヲツウジル" arg="unk" pred="s0_knpbp4" id="s0_knpcr12"/>
          <caseRelation flag="U" deprel="トスル" arg="unk" pred="s0_knpbp4" id="s0_knpcr13"/>
          <caseRelation flag="U" deprel="ニムケル" arg="unk" pred="s0_knpbp4" id="s0_knpcr14"/>
          <caseRelation flag="N" deprel="ガ" arg="s0_knpt0" pred="s0_knpbp6" id="s0_knpcr15"/>
          <caseRelation flag="C" deprel="ヲ" arg="s0_knpt3" pred="s0_knpbp6" id="s0_knpcr16"/>
          <caseRelation flag="C" deprel="ニ" arg="s0_knpt8" pred="s0_knpbp6" id="s0_knpcr17"/>
          <caseRelation flag="U" deprel="ト" arg="unk" pred="s0_knpbp6" id="s0_knpcr18"/>
          <caseRelation flag="U" deprel="デ" arg="unk" pred="s0_knpbp6" id="s0_knpcr19"/>
          <caseRelation flag="U" deprel="カラ" arg="unk" pred="s0_knpbp6" id="s0_knpcr20"/>
          <caseRelation flag="U" deprel="ヨリ" arg="unk" pred="s0_knpbp6" id="s0_knpcr21"/>
          <caseRelation flag="U" deprel="マデ" arg="unk" pred="s0_knpbp6" id="s0_knpcr22"/>
          <caseRelation flag="U" deprel="ヘ" arg="unk" pred="s0_knpbp6" id="s0_knpcr23"/>
          <caseRelation flag="U" deprel="時間" arg="unk" pred="s0_knpbp6" id="s0_knpcr24"/>
          <caseRelation flag="U" deprel="外の関係" arg="unk" pred="s0_knpbp6" id="s0_knpcr25"/>
          <caseRelation flag="U" deprel="ノ" arg="unk" pred="s0_knpbp6" id="s0_knpcr26"/>
          <caseRelation flag="U" deprel="修飾" arg="unk" pred="s0_knpbp6" id="s0_knpcr27"/>
          <caseRelation flag="U" deprel="トスル" arg="unk" pred="s0_knpbp6" id="s0_knpcr28"/>
          <caseRelation flag="U" deprel="ニツク" arg="unk" pred="s0_knpbp6" id="s0_knpcr29"/>
          <caseRelation flag="U" deprel="ヲツウジル" arg="unk" pred="s0_knpbp6" id="s0_knpcr30"/>
          <caseRelation flag="U" deprel="ニヨル" arg="unk" pred="s0_knpbp6" id="s0_knpcr31"/>
          <caseRelation flag="U" deprel="ニトル" arg="unk" pred="s0_knpbp6" id="s0_knpcr32"/>
          <caseRelation flag="U" deprel="ヲノゾク" arg="unk" pred="s0_knpbp6" id="s0_knpcr33"/>
          <caseRelation flag="U" deprel="ニアワセル" arg="unk" pred="s0_knpbp6" id="s0_knpcr34"/>
        </caseRelations>
        <NEs annotators="knp">
          <NE label="PERSON" tokens="s0_knpt0" id="s0_knpne0"/>
        </NEs>
      </sentence>
    </sentences>
  </document>
</root>"""

        self.exe = 'runMain jigg.pipeline.Pipeline -annotators ssplit,juman,knp'

    def test_knp(self):
        self.check_equal(self.exe, self.input_text, self.expected_text)
