package jigg.pipeline

/*
 Copyright 2013-2015 Takafumi Sakakibara and Hiroshi Noji

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

import java.util.Properties
import org.scalatest._

class SimpleKNPAnnotatorSpec extends BaseAnnotatorSpec {

  def newKNP(output: String, p: Properties = new Properties) =
    new SimpleKNPAnnotator("knp", p) {
      override def nThreads = 1
      override def mkLocalAnnotator = new SimpleKNPLocalAnnotator {
        override def mkCommunicator = new StubExternalCommunicator(output)
      }
  }

  "Annotator" should "add modifed tokens, basicPhrases, chunks, basicPhraseDependencies, dependencies, caseRelations and namedEntities" in {

    val jumanOutputNode =
      <root><document><sentences>
      <sentence id="s0">
        国際連合が設立された
        <tokens annotators="juman">
          <token id="s0_tok0" form="国際" characterOffsetBegin="0" characterOffsetEnd="2" yomi="こくさい" lemma="国際" pos="名詞" posId="6" pos1="普通名詞" pos1Id="1" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="&quot;代表表記:国際/こくさい カテゴリ:抽象物&quot;"/>
          <token id="s0_tok1" form="連合" characterOffsetBegin="2" characterOffsetEnd="4" yomi="れんごう" lemma="連合" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="&quot;代表表記:連合/れんごう 組織名末尾 カテゴリ:組織・団体;抽象物&quot;"/>
          <token id="s0_tok2" form="が" characterOffsetBegin="4" characterOffsetEnd="5" yomi="が" lemma="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="NIL"/>
          <token id="s0_tok3" form="設立" characterOffsetBegin="5" characterOffsetEnd="7" yomi="せつりつ" lemma="設立" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="&quot;代表表記:設立/せつりつ カテゴリ:抽象物&quot;"/>
          <token id="s0_tok4" form="さ" characterOffsetBegin="7" characterOffsetEnd="8" yomi="さ" lemma="する" pos="動詞" posId="2" pos1="*" pos1Id="0" cType="サ変動詞" cTypeId="16" cForm="未然形" cFormId="3" misc="&quot;代表表記:する/する 付属動詞候補（基本） 自他動詞:自:成る/なる&quot;"/>
          <token id="s0_tok5" form="れた" characterOffsetBegin="8" characterOffsetEnd="10" yomi="れた" lemma="れる" pos="接尾辞" posId="14" pos1="動詞性接尾辞" pos1Id="7" cType="母音動詞" cTypeId="1" cForm="タ形" cFormId="10" misc="&quot;代表表記:れる/れる&quot;"/>
        </tokens>
      </sentence>
    </sentences></document></root>

    val knpOutput = """# S-ID:1 KNP:4.12-CF1.1 DATE:2016/02/18 SCORE:-6.95854
* 1D <文頭><サ変><組織名疑><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:国際/こくさい+連合/れんごう><主辞代表表記:連合/れんごう>
+ 1D <文節内><係:文節内><文頭><体言><名詞項候補><先行詞候補><正規化代表表記:国際/こくさい><NE内:ORGANIZATION>
国際 こくさい 国際 名詞 6 普通名詞 1 * 0 * 0 "代表表記:国際/こくさい カテゴリ:抽象物" <代表表記:国際/こくさい><カテゴリ:抽象物><正規化代表表記:国際/こくさい><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><NE:ORGANIZATION:B>
+ 2D <組織名疑><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><サ変><SM-主体><SM-組織><名詞項候補><先行詞候補><非用言格解析:動><照応ヒント:係><態:未定><正規化代表表記:連合/れんごう><NE:ORGANIZATION:国際連合><Wikipedia上位語:国際組織><解析格:ガ>
連合 れんごう 連合 名詞 6 サ変名詞 2 * 0 * 0 "代表表記:連合/れんごう 組織名末尾 カテゴリ:組織・団体;抽象物" <代表表記:連合/れんごう><組織名末尾><カテゴリ:組織・団体;抽象物><正規化代表表記:連合/れんごう><Wikipedia上位語:国際組織:0-1><漢字><かな漢字><名詞相当語><サ変><自立><複合←><内容語><タグ単位始><文節主辞><係:ガ格><NE:ORGANIZATION:E>
が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
* -1D <文末><サ変><サ変動詞><時制-過去><態:受動><〜られる><用言:動><レベル:C><区切:5-5><ID:（文末）><提題受:30><主節><動態述語><正規化代表表記:設立/せつりつ><主辞代表表記:設立/せつりつ>
+ -1D <文末><サ変動詞><時制-過去><態:受動><〜られる><用言:動><レベル:C><区切:5-5><ID:（文末）><提題受:30><主節><動態述語><サ変><正規化代表表記:設立/せつりつ><用言代表表記:設立/せつりつ+する/する+れる/れる><主題格:一人称優位><格関係1:ガ:連合><格解析結果:設立/せつりつ+する/する+れる/れる:動1:ガ/C/連合/1/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;トスル/U/-/-/-/-;ニヨル/U/-/-/-/-;修飾/U/-/-/-/-;ニモトヅク/U/-/-/-/-;ニオク/U/-/-/-/-;ニトモナウ/U/-/-/-/-;ニツク/U/-/-/-/-;ニムケル/U/-/-/-/-;ニツヅク/U/-/-/-/-><正規化格解析結果-0:設立/せつりつ:動1:ヲ/C/連合/1/0/1>
設立 せつりつ 設立 名詞 6 サ変名詞 2 * 0 * 0 "代表表記:設立/せつりつ カテゴリ:抽象物" <代表表記:設立/せつりつ><カテゴリ:抽象物><正規化代表表記:設立/せつりつ><漢字><かな漢字><名詞相当語><サ変><サ変動詞><自立><内容語><タグ単位始><文節始><文節主辞>
さ さ する 動詞 2 * 0 サ変動詞 16 未然形 3 "代表表記:する/する 付属動詞候補（基本） 自他動詞:自:成る/なる" <代表表記:する/する><付属動詞候補（基本）><自他動詞:自:成る/なる><正規化代表表記:する/する><とタ系連用テ形複合辞><かな漢字><ひらがな><活用語><付属>
れた れた れる 接尾辞 14 動詞性接尾辞 7 母音動詞 1 タ形 10 "代表表記:れる/れる" <代表表記:れる/れる><正規化代表表記:れる/れる><文末><表現文末><かな漢字><ひらがな><活用語><付属>
EOS"""

    val result = (newKNP(knpOutput).annotate(jumanOutputNode) \\ "sentence")(0)

    val deps = result \ "dependencies"
    val chunkDeps = deps.filter(d => d \@ "unit" == "chunk")

    chunkDeps.size should be (1)

    chunkDeps.head should equal (
      <dependencies unit="chunk" annotators="knp">
        <dependency id="s0_knpdep0" head="s0_knpc1" dependent="s0_knpc0" deprel="D"/>
        <dependency id="s0_knpdep1" head="root" dependent="s0_knpc1" deprel="D"/>
      </dependencies>
    ) (decided by sameElem)

    val basicPhraseDeps = deps.filter(d => d \@ "unit" == "basicPhrase")

    basicPhraseDeps.size should be (1)

    basicPhraseDeps.head should equal (
      <dependencies unit="basicPhrase" annotators="knp">
        <dependency id="s0_knpbpdep0" head="s0_knpbp1" dependent="s0_knpbp0" deprel="D"/>
        <dependency id="s0_knpbpdep1" head="s0_knpbp2" dependent="s0_knpbp1" deprel="D"/>
        <dependency id="s0_knpbpdep2" head="root" dependent="s0_knpbp2" deprel="D"/>
      </dependencies>
    ) (decided by sameElem)

    (result \ "tokens").head should equal (<tokens annotators="knp">
      <token id="s0_knpt0" form="国際" characterOffsetBegin="0" characterOffsetEnd="2" yomi="こくさい" lemma="国際" pos="名詞" posId="6" pos1="普通名詞" pos1Id="1" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="&quot;代表表記:国際/こくさい カテゴリ:抽象物&quot; &lt;代表表記:国際/こくさい&gt;&lt;カテゴリ:抽象物&gt;&lt;正規化代表表記:国際/こくさい&gt;&lt;文頭&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;NE:ORGANIZATION:B&gt;"/>
      <token id="s0_knpt1" form="連合" characterOffsetBegin="2" characterOffsetEnd="4" yomi="れんごう" lemma="連合" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="&quot;代表表記:連合/れんごう 組織名末尾 カテゴリ:組織・団体;抽象物&quot; &lt;代表表記:連合/れんごう&gt;&lt;組織名末尾&gt;&lt;カテゴリ:組織・団体;抽象物&gt;&lt;正規化代表表記:連合/れんごう&gt;&lt;Wikipedia上位語:国際組織:0-1&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;サ変&gt;&lt;自立&gt;&lt;複合←&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節主辞&gt;&lt;係:ガ格&gt;&lt;NE:ORGANIZATION:E&gt;"/>
      <token id="s0_knpt2" form="が" characterOffsetBegin="4" characterOffsetEnd="5" yomi="が" lemma="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;"/>
      <token id="s0_knpt3" form="設立" characterOffsetBegin="5" characterOffsetEnd="7" yomi="せつりつ" lemma="設立" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" cType="*" cTypeId="0" cForm="*" cFormId="0" misc="&quot;代表表記:設立/せつりつ カテゴリ:抽象物&quot; &lt;代表表記:設立/せつりつ&gt;&lt;カテゴリ:抽象物&gt;&lt;正規化代表表記:設立/せつりつ&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;サ変&gt;&lt;サ変動詞&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;"/>
      <token id="s0_knpt4" form="さ" characterOffsetBegin="7" characterOffsetEnd="8" yomi="さ" lemma="する" pos="動詞" posId="2" pos1="*" pos1Id="0" cType="サ変動詞" cTypeId="16" cForm="未然形" cFormId="3" misc="&quot;代表表記:する/する 付属動詞候補（基本） 自他動詞:自:成る/なる&quot; &lt;代表表記:する/する&gt;&lt;付属動詞候補（基本）&gt;&lt;自他動詞:自:成る/なる&gt;&lt;正規化代表表記:する/する&gt;&lt;とタ系連用テ形複合辞&gt;&lt;かな漢字&gt;&lt;ひらがな&gt;&lt;活用語&gt;&lt;付属&gt;"/>
      <token id="s0_knpt5" form="れた" characterOffsetBegin="8" characterOffsetEnd="10" yomi="れた" lemma="れる" pos="接尾辞" posId="14" pos1="動詞性接尾辞" pos1Id="7" cType="母音動詞" cTypeId="1" cForm="タ形" cFormId="10" misc="&quot;代表表記:れる/れる&quot; &lt;代表表記:れる/れる&gt;&lt;正規化代表表記:れる/れる&gt;&lt;文末&gt;&lt;表現文末&gt;&lt;かな漢字&gt;&lt;ひらがな&gt;&lt;活用語&gt;&lt;付属&gt;"/>
      </tokens>
    ) (decided by sameElem)

    (result \ "basicPhrases").head should equal (
      <basicPhrases annotators="knp">
        <basicPhrase id="s0_knpbp0" tokens="s0_knpt0" misc="&lt;文節内&gt;&lt;係:文節内&gt;&lt;文頭&gt;&lt;体言&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:国際/こくさい&gt;&lt;NE内:ORGANIZATION&gt;"/>
        <basicPhrase id="s0_knpbp1" tokens="s0_knpt1 s0_knpt2" misc="&lt;組織名疑&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;サ変&gt;&lt;SM-主体&gt;&lt;SM-組織&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;非用言格解析:動&gt;&lt;照応ヒント:係&gt;&lt;態:未定&gt;&lt;正規化代表表記:連合/れんごう&gt;&lt;NE:ORGANIZATION:国際連合&gt;&lt;Wikipedia上位語:国際組織&gt;&lt;解析格:ガ&gt;"/>
        <basicPhrase
        id="s0_knpbp2" tokens="s0_knpt3 s0_knpt4 s0_knpt5" misc="&lt;文末&gt;&lt;サ変動詞&gt;&lt;時制-過去&gt;&lt;態:受動&gt;&lt;〜られる&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;動態述語&gt;&lt;サ変&gt;&lt;正規化代表表記:設立/せつりつ&gt;&lt;用言代表表記:設立/せつりつ+する/する+れる/れる&gt;&lt;主題格:一人称優位&gt;&lt;格関係1:ガ:連合&gt;&lt;格解析結果:設立/せつりつ+する/する+れる/れる:動1:ガ/C/連合/1/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;トスル/U/-/-/-/-;ニヨル/U/-/-/-/-;修飾/U/-/-/-/-;ニモトヅク/U/-/-/-/-;ニオク/U/-/-/-/-;ニトモナウ/U/-/-/-/-;ニツク/U/-/-/-/-;ニムケル/U/-/-/-/-;ニツヅク/U/-/-/-/-&gt;&lt;正規化格解析結果-0:設立/せつりつ:動1:ヲ/C/連合/1/0/1&gt;">
        </basicPhrase>
      </basicPhrases>
    ) (decided by sameElem)

    (result \ "chunks").head should equal (
      <chunks annotators="knp">
        <chunk id="s0_knpc0" tokens="s0_knpt0 s0_knpt1 s0_knpt2" misc="&lt;文頭&gt;&lt;サ変&gt;&lt;組織名疑&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:国際/こくさい+連合/れんごう&gt;&lt;主辞代表表記:連合/れんごう&gt;"/>
        <chunk id="s0_knpc1" tokens="s0_knpt3 s0_knpt4 s0_knpt5" misc="&lt;文末&gt;&lt;サ変&gt;&lt;サ変動詞&gt;&lt;時制-過去&gt;&lt;態:受動&gt;&lt;〜られる&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;動態述語&gt;&lt;正規化代表表記:設立/せつりつ&gt;&lt;主辞代表表記:設立/せつりつ&gt;"/>
      </chunks>
    ) (decided by sameElem)

    (result \ "caseRelations").head should equal (
      <caseRelations annotators="knp">
        <caseRelation id="s0_knpcr0" pred="s0_knpbp2" arg="s0_knpt1" deprel="ガ" flag="C"/>
        <caseRelation id="s0_knpcr1" pred="s0_knpbp2" arg="unk" deprel="ヲ" flag="U"/>
        <caseRelation id="s0_knpcr2" pred="s0_knpbp2" arg="unk" deprel="ニ" flag="U"/>
        <caseRelation id="s0_knpcr3" pred="s0_knpbp2" arg="unk" deprel="ト" flag="U"/>
        <caseRelation id="s0_knpcr4" pred="s0_knpbp2" arg="unk" deprel="デ" flag="U"/>
        <caseRelation id="s0_knpcr5" pred="s0_knpbp2" arg="unk" deprel="カラ" flag="U"/>
        <caseRelation id="s0_knpcr6" pred="s0_knpbp2" arg="unk" deprel="ヨリ" flag="U"/>
        <caseRelation id="s0_knpcr7" pred="s0_knpbp2" arg="unk" deprel="マデ" flag="U"/>
        <caseRelation id="s0_knpcr8" pred="s0_knpbp2" arg="unk" deprel="ヘ" flag="U"/>
        <caseRelation id="s0_knpcr9" pred="s0_knpbp2" arg="unk" deprel="時間" flag="U"/>
        <caseRelation id="s0_knpcr10" pred="s0_knpbp2" arg="unk" deprel="外の関係" flag="U"/>
        <caseRelation id="s0_knpcr11" pred="s0_knpbp2" arg="unk" deprel="トスル" flag="U"/>
        <caseRelation id="s0_knpcr12" pred="s0_knpbp2" arg="unk" deprel="ニヨル" flag="U"/>
        <caseRelation id="s0_knpcr13" pred="s0_knpbp2" arg="unk" deprel="修飾" flag="U"/>
        <caseRelation id="s0_knpcr14" pred="s0_knpbp2" arg="unk" deprel="ニモトヅク" flag="U"/>
        <caseRelation id="s0_knpcr15" pred="s0_knpbp2" arg="unk" deprel="ニオク" flag="U"/>
        <caseRelation id="s0_knpcr16" pred="s0_knpbp2" arg="unk" deprel="ニトモナウ" flag="U"/>
        <caseRelation id="s0_knpcr17" pred="s0_knpbp2" arg="unk" deprel="ニツク" flag="U"/>
        <caseRelation id="s0_knpcr18" pred="s0_knpbp2" arg="unk" deprel="ニムケル" flag="U"/>
        <caseRelation id="s0_knpcr19" pred="s0_knpbp2" arg="unk" deprel="ニツヅク" flag="U"/>
      </caseRelations>
    ) (decided by sameElem)

    (result \ "NEs").head should equal (
      <NEs annotators="knp">
        <NE id="s0_knpne0" tokens="s0_knpt0 s0_knpt1" label="ORGANIZATION"/>
      </NEs>
    ) (decided by sameElem)
  }
}
