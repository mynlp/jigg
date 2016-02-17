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

  def newKNP(output: String, p: Properties = new Properties) = new SimpleKNPAnnotator("", p) {
    override def mkCommunicator = new StubExternalCommunicator(output)
  }

  "Annotator" should "add modifed tokens, basicPhrases, chunks, basicPhraseDependencies, dependencies, caseRelations, namedEntities, coreferences, and predArgs" in {

    val jumanOutputNode =
      <sentence id="s0">
        国際連合が設立された
        <tokens>
          <token id="s0_tok0" surf="国際" reading="こくさい" base="国際" pos="名詞" posId="6" pos1="普通名詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;代表表記:国際/こくさい カテゴリ:抽象物&quot;"/>
          <token id="s0_tok1" surf="連合" reading="れんごう" base="連合" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;代表表記:連合/れんごう 組織名末尾 カテゴリ:組織・団体;抽象物&quot;"/>
          <token id="s0_tok2" surf="が" reading="が" base="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL"/>
          <token id="s0_tok3" surf="設立" reading="せつりつ" base="設立" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;代表表記:設立/せつりつ カテゴリ:抽象物&quot;"/>
          <token id="s0_tok4" surf="さ" reading="さ" base="する" pos="動詞" posId="2" pos1="*" pos1Id="0" inflectionType="サ変動詞" inflectionTypeId="16" inflectionForm="未然形" inflectionFormId="3" semantic="&quot;代表表記:する/する 付属動詞候補（基本） 自他動詞:自:成る/なる&quot;"/>
          <token id="s0_tok5" surf="れた" reading="れた" base="れる" pos="接尾辞" posId="14" pos1="動詞性接尾辞" pos1Id="7" inflectionType="母音動詞" inflectionTypeId="1" inflectionForm="タ形" inflectionFormId="10" semantic="&quot;代表表記:れる/れる&quot;"/>
        </tokens>
      </sentence>

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

    val result = newKNP(knpOutput).newSentenceAnnotation(jumanOutputNode)

    result should equal (
      <sentence id="s0">
        国際連合が設立された
        <tokens>
          <token id="s0_tok0" surf="国際" reading="こくさい" base="国際" pos="名詞" posId="6" pos1="普通名詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;代表表記:国際/こくさい カテゴリ:抽象物&quot; &lt;代表表記:国際/こくさい&gt;&lt;カテゴリ:抽象物&gt;&lt;正規化代表表記:国際/こくさい&gt;&lt;文頭&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;NE:ORGANIZATION:B&gt;"/>
          <token id="s0_tok1" surf="連合" reading="れんごう" base="連合" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;代表表記:連合/れんごう 組織名末尾 カテゴリ:組織・団体;抽象物&quot; &lt;代表表記:連合/れんごう&gt;&lt;組織名末尾&gt;&lt;カテゴリ:組織・団体;抽象物&gt;&lt;正規化代表表記:連合/れんごう&gt;&lt;Wikipedia上位語:国際組織:0-1&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;サ変&gt;&lt;自立&gt;&lt;複合←&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節主辞&gt;&lt;係:ガ格&gt;&lt;NE:ORGANIZATION:E&gt;"/>
          <token id="s0_tok2" surf="が" reading="が" base="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;"/>
          <token id="s0_tok3" surf="設立" reading="せつりつ" base="設立" pos="名詞" posId="6" pos1="サ変名詞" pos1Id="2" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;代表表記:設立/せつりつ カテゴリ:抽象物&quot; &lt;代表表記:設立/せつりつ&gt;&lt;カテゴリ:抽象物&gt;&lt;正規化代表表記:設立/せつりつ&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;サ変&gt;&lt;サ変動詞&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;"/>
          <token id="s0_tok4" surf="さ" reading="さ" base="する" pos="動詞" posId="2" pos1="*" pos1Id="0" inflectionType="サ変動詞" inflectionTypeId="16" inflectionForm="未然形" inflectionFormId="3" semantic="&quot;代表表記:する/する 付属動詞候補（基本） 自他動詞:自:成る/なる&quot; &lt;代表表記:する/する&gt;&lt;付属動詞候補（基本）&gt;&lt;自他動詞:自:成る/なる&gt;&lt;正規化代表表記:する/する&gt;&lt;とタ系連用テ形複合辞&gt;&lt;かな漢字&gt;&lt;ひらがな&gt;&lt;活用語&gt;&lt;付属&gt;"/>
          <token id="s0_tok5" surf="れた" reading="れた" base="れる" pos="接尾辞" posId="14" pos1="動詞性接尾辞" pos1Id="7" inflectionType="母音動詞" inflectionTypeId="1" inflectionForm="タ形" inflectionFormId="10" semantic="&quot;代表表記:れる/れる&quot; &lt;代表表記:れる/れる&gt;&lt;正規化代表表記:れる/れる&gt;&lt;文末&gt;&lt;表現文末&gt;&lt;かな漢字&gt;&lt;ひらがな&gt;&lt;活用語&gt;&lt;付属&gt;"/>
        </tokens>
        <basicPhrases>
          <basicPhrase id="s0_bp0" tokens="s0_tok0" features="&lt;文節内&gt;&lt;係:文節内&gt;&lt;文頭&gt;&lt;体言&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;正規化代表表記:国際/こくさい&gt;&lt;NE内:ORGANIZATION&gt;"/>
          <basicPhrase id="s0_bp1" tokens="s0_tok1 s0_tok2" features="&lt;組織名疑&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;サ変&gt;&lt;SM-主体&gt;&lt;SM-組織&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;非用言格解析:動&gt;&lt;照応ヒント:係&gt;&lt;態:未定&gt;&lt;正規化代表表記:連合/れんごう&gt;&lt;NE:ORGANIZATION:国際連合&gt;&lt;Wikipedia上位語:国際組織&gt;&lt;解析格:ガ&gt;"/>
          <basicPhrase
          id="s0_bp2" tokens="s0_tok3 s0_tok4 s0_tok5" features="&lt;文末&gt;&lt;サ変動詞&gt;&lt;時制-過去&gt;&lt;態:受動&gt;&lt;〜られる&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;動態述語&gt;&lt;サ変&gt;&lt;正規化代表表記:設立/せつりつ&gt;&lt;用言代表表記:設立/せつりつ+する/する+れる/れる&gt;&lt;主題格:一人称優位&gt;&lt;格関係1:ガ:連合&gt;&lt;格解析結果:設立/せつりつ+する/する+れる/れる:動1:ガ/C/連合/1/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;トスル/U/-/-/-/-;ニヨル/U/-/-/-/-;修飾/U/-/-/-/-;ニモトヅク/U/-/-/-/-;ニオク/U/-/-/-/-;ニトモナウ/U/-/-/-/-;ニツク/U/-/-/-/-;ニムケル/U/-/-/-/-;ニツヅク/U/-/-/-/-&gt;&lt;正規化格解析結果-0:設立/せつりつ:動1:ヲ/C/連合/1/0/1&gt;">
</basicPhrase>
        </basicPhrases>
        <chunks>
          <chunk id="s0_chu0" tokens="s0_tok0 s0_tok1 s0_tok2" features="&lt;文頭&gt;&lt;サ変&gt;&lt;組織名疑&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:国際/こくさい+連合/れんごう&gt;&lt;主辞代表表記:連合/れんごう&gt;"/>
          <chunk id="s0_chu1" tokens="s0_tok3 s0_tok4 s0_tok5" features="&lt;文末&gt;&lt;サ変&gt;&lt;サ変動詞&gt;&lt;時制-過去&gt;&lt;態:受動&gt;&lt;〜られる&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;動態述語&gt;&lt;正規化代表表記:設立/せつりつ&gt;&lt;主辞代表表記:設立/せつりつ&gt;"/>
        </chunks>
        <basicPhraseDependencies root="s0_bp2">
          <basicPhraseDependency id="s0_bpdep0" head="s0_bp1" dependent="s0_bp0" label="D"/>
          <basicPhraseDependency id="s0_bpdep1" head="s0_bp2" dependent="s0_bp1" label="D"/>
        </basicPhraseDependencies>
        <dependencies root="s0_chu1">
          <dependency id="s0_dep0" head="s0_chu1" dependent="s0_chu0" label="D"/>
        </dependencies>
        <caseRelations>
          <caseRelation id="s0_cr0" head="s0_bp2" depend="s0_tok1" label="ガ" flag="C"/>
          <caseRelation id="s0_cr1" head="s0_bp2" depend="unk" label="ヲ" flag="U"/>
          <caseRelation id="s0_cr2" head="s0_bp2" depend="unk" label="ニ" flag="U"/>
          <caseRelation id="s0_cr3" head="s0_bp2" depend="unk" label="ト" flag="U"/>
          <caseRelation id="s0_cr4" head="s0_bp2" depend="unk" label="デ" flag="U"/>
          <caseRelation id="s0_cr5" head="s0_bp2" depend="unk" label="カラ" flag="U"/>
          <caseRelation id="s0_cr6" head="s0_bp2" depend="unk" label="ヨリ" flag="U"/>
          <caseRelation id="s0_cr7" head="s0_bp2" depend="unk" label="マデ" flag="U"/>
          <caseRelation id="s0_cr8" head="s0_bp2" depend="unk" label="ヘ" flag="U"/>
          <caseRelation id="s0_cr9" head="s0_bp2" depend="unk" label="時間" flag="U"/>
          <caseRelation id="s0_cr10" head="s0_bp2" depend="unk" label="外の関係" flag="U"/>
          <caseRelation id="s0_cr11" head="s0_bp2" depend="unk" label="トスル" flag="U"/>
          <caseRelation id="s0_cr12" head="s0_bp2" depend="unk" label="ニヨル" flag="U"/>
          <caseRelation id="s0_cr13" head="s0_bp2" depend="unk" label="修飾" flag="U"/>
          <caseRelation id="s0_cr14" head="s0_bp2" depend="unk" label="ニモトヅク" flag="U"/>
          <caseRelation id="s0_cr15" head="s0_bp2" depend="unk" label="ニオク" flag="U"/>
          <caseRelation id="s0_cr16" head="s0_bp2" depend="unk" label="ニトモナウ" flag="U"/>
          <caseRelation id="s0_cr17" head="s0_bp2" depend="unk" label="ニツク" flag="U"/>
          <caseRelation id="s0_cr18" head="s0_bp2" depend="unk" label="ニムケル" flag="U"/>
          <caseRelation id="s0_cr19" head="s0_bp2" depend="unk" label="ニツヅク" flag="U"/>
        </caseRelations>
        <namedEntities>
          <namedEntity id="s0_ne0" tokens="s0_tok0 s0_tok1" label="ORGANIZATION"/>
        </namedEntities>
      </sentence>
    ) (decided by sameElem)
  }
}
