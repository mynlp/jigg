package jigg.pipeline

/*
 Copyright 2013-2016 Takafumi Sakakibara and Hiroshi Noji

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

class DocumentKNPAnnotatorTest extends BaseAnnotatorSpec {

  def newKNP(output: Seq[String], p: Properties = new Properties) = new DocumentKNPAnnotator("", p) {
    override def mkCommunicator = new StubExternalCommunicator(output)
  }

  "Annotator" should "add modifed tokens, basicPhrases, chunks, basicPhraseDependencies, dependencies, caseRelations, namedEntities" in {

    val jumanOutputNode =
      <document id="d0">
        <sentences>
          <sentence id="s0">
          太郎が走る。
          <tokens>
            <token id="s0_tok0" surf="太郎" reading="たろう" base="太郎" pos="名詞" posId="6" pos1="人名" pos1Id="5" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;人名:日本:名:45:0.00106&quot;"/>
            <token id="s0_tok1" surf="が" reading="が" base="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL"/>
            <token id="s0_tok2" surf="走る" reading="はしる" base="走る" pos="動詞" posId="2" pos1="*" pos1Id="0" inflectionType="子音動詞ラ行" inflectionTypeId="10" inflectionForm="基本形" inflectionFormId="2" semantic="&quot;代表表記:走る/はしる&quot;"/>
            <token id="s0_tok3" surf="。" reading="。" base="。" pos="特殊" posId="1" pos1="句点" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL"/>
          </tokens>
        </sentence>
        <sentence id="s1">
          太郎が歩く。
          <tokens>
            <token id="s1_tok0" surf="太郎" reading="たろう" base="太郎" pos="名詞" posId="6" pos1="人名" pos1Id="5" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;人名:日本:名:45:0.00106&quot;"/>
            <token id="s1_tok1" surf="が" reading="が" base="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL"/>
            <token id="s1_tok2" surf="歩く" reading="あるく" base="歩く" pos="動詞" posId="2" pos1="*" pos1Id="0" inflectionType="子音動詞カ行" inflectionTypeId="2" inflectionForm="基本形" inflectionFormId="2" semantic="&quot;代表表記:歩く/あるく&quot;"/>
            <token id="s1_tok3" surf="。" reading="。" base="。" pos="特殊" posId="1" pos1="句点" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL"/>
          </tokens>
        </sentence>
      </sentences>
    </document>

    val knpOutputs = Seq("""# S-ID:d0-s0 JUMAN:7.01 KNP:4.12-CF1.1 DATE:2016/02/18 SCORE:-7.16850
* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>
太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
* -1D <文末><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
+ -1D <文末><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><時制-未来><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/d0-s0;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>
走る はしる 走る 動詞 2 * 0 子音動詞ラ行 10 基本形 2 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
EOS""",
      """# S-ID:d0-s1 JUMAN:7.01 KNP:4.12-CF1.1 DATE:2016/02/18 SCORE:-6.35862
* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><C用;【太郎】;=;1;0;9.99:d0-s0(1文前):0文節><共参照><COREFER_ID:1><REFERRED:1-0><EID:0>
太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
* -1D <文末><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:歩く/あるく><主辞代表表記:歩く/あるく>
+ -1D <文末><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:歩く/あるく><用言代表表記:歩く/あるく><時制-未来><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:歩く/あるく:動12:ガ/C/太郎/0/0/d0-s1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;トスル/U/-/-/-/-;ニツク/U/-/-/-/-;ニムケル/U/-/-/-/-;ニソウ/U/-/-/-/-;トイウ/U/-/-/-/-><EID:2><述語項構造:歩く/あるく:動12:ガ/C/太郎/0>
歩く あるく 歩く 動詞 2 * 0 子音動詞カ行 2 基本形 2 "代表表記:歩く/あるく" <代表表記:歩く/あるく><正規化代表表記:歩く/あるく><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
EOS""")

    val result = newKNP(knpOutputs).newDocumentAnnotation(jumanOutputNode)

    result should equal (
      <document id="d0">
        <sentences>
          <sentence id="s0">
            太郎が走る。
            <tokens>
              <token id="s0_tok0" surf="太郎" reading="たろう" base="太郎" pos="名詞" posId="6" pos1="人名" pos1Id="5" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう&quot; &lt;人名:日本:名:45:0.00106&gt;&lt;疑似代表表記&gt;&lt;代表表記:太郎/たろう&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;文頭&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;&lt;係:ガ格&gt;&lt;NE:PERSON:S&gt;"/>
              <token id="s0_tok1" surf="が" reading="が" base="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;"/>
              <token id="s0_tok2" surf="走る" reading="はしる" base="走る" pos="動詞" posId="2" pos1="*" pos1Id="0" inflectionType="子音動詞ラ行" inflectionTypeId="10" inflectionForm="基本形" inflectionFormId="2" semantic="&quot;代表表記:走る/はしる&quot; &lt;代表表記:走る/はしる&gt;&lt;正規化代表表記:走る/はしる&gt;&lt;表現文末&gt;&lt;かな漢字&gt;&lt;活用語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;"/>
              <token id="s0_tok3" surf="。" reading="。" base="。" pos="特殊" posId="1" pos1="句点" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL &lt;文末&gt;&lt;英記号&gt;&lt;記号&gt;&lt;付属&gt;"/>
            </tokens>
            <basicPhrases>
              <basicPhrase id="s0_bp0" tokens="s0_tok0 s0_tok1" features="&lt;文頭&gt;&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;照応詞候補:太郎&gt;&lt;解析格:ガ&gt;&lt;EID:0&gt;"/>
              <basicPhrase
          id="s0_bp1" tokens="s0_tok2 s0_tok3" features="&lt;文末&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:走る/はしる&gt;&lt;用言代表表記:走る/はしる&gt;&lt;時制-未来&gt;&lt;主題格:一人称優位&gt;&lt;格関係0:ガ:太郎&gt;&lt;格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/d0-s0;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-&gt;&lt;EID:1&gt;&lt;述語項構造:走る/はしる:動13:ガ/C/太郎/0&gt;">
</basicPhrase>
            </basicPhrases>
            <chunks>
              <chunk id="s0_chu0" tokens="s0_tok0 s0_tok1" features="&lt;文頭&gt;&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;主辞代表表記:太郎/たろう&gt;"/>
              <chunk id="s0_chu1" tokens="s0_tok2 s0_tok3" features="&lt;文末&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:走る/はしる&gt;&lt;主辞代表表記:走る/はしる&gt;"/>
            </chunks>
            <basicPhraseDependencies root="s0_bp1">
              <basicPhraseDependency id="s0_bpdep0" head="s0_bp1" dependent="s0_bp0" label="D"/>
            </basicPhraseDependencies>
            <dependencies root="s0_chu1">
              <dependency id="s0_dep0" head="s0_chu1" dependent="s0_chu0" label="D"/>
            </dependencies>
            <caseRelations>
              <caseRelation id="s0_cr0" head="s0_bp1" depend="s0_tok0" label="ガ" flag="C"/>
              <caseRelation id="s0_cr1" head="s0_bp1" depend="unk" label="ヲ" flag="U"/>
               <caseRelation id="s0_cr2" head="s0_bp1" depend="unk" label="ニ" flag="U"/>
               <caseRelation id="s0_cr3" head="s0_bp1" depend="unk" label="ト" flag="U"/>
               <caseRelation id="s0_cr4" head="s0_bp1" depend="unk" label="デ" flag="U"/>
               <caseRelation id="s0_cr5" head="s0_bp1" depend="unk" label="カラ" flag="U"/>
               <caseRelation id="s0_cr6" head="s0_bp1" depend="unk" label="ヨリ" flag="U"/>
               <caseRelation id="s0_cr7" head="s0_bp1" depend="unk" label="マデ" flag="U"/>
               <caseRelation id="s0_cr8" head="s0_bp1" depend="unk" label="時間" flag="U"/>
               <caseRelation id="s0_cr9" head="s0_bp1" depend="unk" label="外の関係" flag="U"/>
               <caseRelation id="s0_cr10" head="s0_bp1" depend="unk" label="ノ" flag="U"/>
               <caseRelation id="s0_cr11" head="s0_bp1" depend="unk" label="修飾" flag="U"/>
               <caseRelation id="s0_cr12" head="s0_bp1" depend="unk" label="トスル" flag="U"/>
               <caseRelation id="s0_cr13" head="s0_bp1" depend="unk" label="ニオク" flag="U"/>
               <caseRelation id="s0_cr14" head="s0_bp1" depend="unk" label="ニカンスル" flag="U"/>
               <caseRelation id="s0_cr15" head="s0_bp1" depend="unk" label="ニヨル" flag="U"/>
               <caseRelation id="s0_cr16" head="s0_bp1" depend="unk" label="ヲフクメル" flag="U"/>
               <caseRelation id="s0_cr17" head="s0_bp1" depend="unk" label="ヲハジメル" flag="U"/>
               <caseRelation id="s0_cr18" head="s0_bp1" depend="unk" label="ヲノゾク" flag="U"/>
               <caseRelation id="s0_cr19" head="s0_bp1" depend="unk" label="ヲツウジル" flag="U"/>
            </caseRelations>
            <namedEntities>
              <namedEntity id="s0_ne0" tokens="s0_tok0" label="PERSON"/>
            </namedEntities>
            <predicateArgumentRelations>
              <predicateArgumentRelation id="s0_par0" predicate="s0_bp1" argument="d0_coref0" label="ガ" flag="C"/>
            </predicateArgumentRelations>
          </sentence>
          <sentence id="s1">
            太郎が歩く。
            <tokens>
              <token id="s1_tok0" surf="太郎" reading="たろう" base="太郎" pos="名詞" posId="6" pos1="人名" pos1Id="5" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="&quot;人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう&quot; &lt;人名:日本:名:45:0.00106&gt;&lt;疑似代表表記&gt;&lt;代表表記:太郎/たろう&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;文頭&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;&lt;係:ガ格&gt;&lt;NE:PERSON:S&gt;"/>
              <token id="s1_tok1" surf="が" reading="が" base="が" pos="助詞" posId="9" pos1="格助詞" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL &lt;かな漢字&gt;&lt;ひらがな&gt;&lt;付属&gt;"/>
              <token id="s1_tok2" surf="歩く" reading="あるく" base="歩く" pos="動詞" posId="2" pos1="*" pos1Id="0" inflectionType="子音動詞カ行" inflectionTypeId="2" inflectionForm="基本形" inflectionFormId="2" semantic="&quot;代表表記:歩く/あるく&quot; &lt;代表表記:歩く/あるく&gt;&lt;正規化代表表記:歩く/あるく&gt;&lt;表現文末&gt;&lt;かな漢字&gt;&lt;活用語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;文節主辞&gt;"/>
              <token id="s1_tok3" surf="。" reading="。" base="。" pos="特殊" posId="1" pos1="句点" pos1Id="1" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" semantic="NIL &lt;文末&gt;&lt;英記号&gt;&lt;記号&gt;&lt;付属&gt;"/>
            </tokens>
            <basicPhrases>
              <basicPhrase id="s1_bp0" tokens="s1_tok0 s1_tok1" features="&lt;文頭&gt;&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;名詞項候補&gt;&lt;先行詞候補&gt;&lt;SM-人&gt;&lt;SM-主体&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;NE:PERSON:太郎&gt;&lt;照応詞候補:太郎&gt;&lt;解析格:ガ&gt;&lt;C用;【太郎】;=;1;0;9.99:d0-s0(1文前):0文節&gt;&lt;共参照&gt;&lt;COREFER_ID:1&gt;&lt;REFERRED:1-0&gt;&lt;EID:0&gt;"/>
              <basicPhrase
          id="s1_bp1" tokens="s1_tok2 s1_tok3" features="&lt;文末&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:歩く/あるく&gt;&lt;用言代表表記:歩く/あるく&gt;&lt;時制-未来&gt;&lt;主題格:一人称優位&gt;&lt;格関係0:ガ:太郎&gt;&lt;格解析結果:歩く/あるく:動12:ガ/C/太郎/0/0/d0-s1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;トスル/U/-/-/-/-;ニツク/U/-/-/-/-;ニムケル/U/-/-/-/-;ニソウ/U/-/-/-/-;トイウ/U/-/-/-/-&gt;&lt;EID:2&gt;&lt;述語項構造:歩く/あるく:動12:ガ/C/太郎/0&gt;">
              </basicPhrase>
            </basicPhrases>
            <chunks>
              <chunk id="s1_chu0" tokens="s1_tok0 s1_tok1" features="&lt;文頭&gt;&lt;人名&gt;&lt;ガ&gt;&lt;助詞&gt;&lt;体言&gt;&lt;係:ガ格&gt;&lt;区切:0-0&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;主辞代表表記:太郎/たろう&gt;"/>
              <chunk id="s1_chu1" tokens="s1_tok2 s1_tok3" features="&lt;文末&gt;&lt;句点&gt;&lt;用言:動&gt;&lt;レベル:C&gt;&lt;区切:5-5&gt;&lt;ID:（文末）&gt;&lt;係:文末&gt;&lt;提題受:30&gt;&lt;主節&gt;&lt;格要素&gt;&lt;連用要素&gt;&lt;動態述語&gt;&lt;正規化代表表記:歩く/あるく&gt;&lt;主辞代表表記:歩く/あるく&gt;"/>
            </chunks>
            <basicPhraseDependencies root="s1_bp1">
              <basicPhraseDependency id="s1_bpdep0" head="s1_bp1" dependent="s1_bp0" label="D"/>
            </basicPhraseDependencies>
            <dependencies root="s1_chu1">
              <dependency id="s1_dep0" head="s1_chu1" dependent="s1_chu0" label="D"/>
            </dependencies>
            <caseRelations>
              <caseRelation id="s1_cr0" head="s1_bp1" depend="s1_tok0" label="ガ" flag="C"/>
              <caseRelation id="s1_cr1" head="s1_bp1" depend="unk" label="ヲ" flag="U"/>
              <caseRelation id="s1_cr2" head="s1_bp1" depend="unk" label="ニ" flag="U"/>
              <caseRelation id="s1_cr3" head="s1_bp1" depend="unk" label="ト" flag="U"/>
              <caseRelation id="s1_cr4" head="s1_bp1" depend="unk" label="デ" flag="U"/>
              <caseRelation id="s1_cr5" head="s1_bp1" depend="unk" label="カラ" flag="U"/>
              <caseRelation id="s1_cr6" head="s1_bp1" depend="unk" label="ヨリ" flag="U"/>
              <caseRelation id="s1_cr7" head="s1_bp1" depend="unk" label="ヘ" flag="U"/>
              <caseRelation id="s1_cr8" head="s1_bp1" depend="unk" label="時間" flag="U"/>
              <caseRelation id="s1_cr9" head="s1_bp1" depend="unk" label="外の関係" flag="U"/>
              <caseRelation id="s1_cr10" head="s1_bp1" depend="unk" label="修飾" flag="U"/>
              <caseRelation id="s1_cr11" head="s1_bp1" depend="unk" label="ノ" flag="U"/>
              <caseRelation id="s1_cr12" head="s1_bp1" depend="unk" label="トスル" flag="U"/>
              <caseRelation id="s1_cr13" head="s1_bp1" depend="unk" label="ニツク" flag="U"/>
              <caseRelation id="s1_cr14" head="s1_bp1" depend="unk" label="ニムケル" flag="U"/>
              <caseRelation id="s1_cr15" head="s1_bp1" depend="unk" label="ニソウ" flag="U"/>
               <caseRelation id="s1_cr16" head="s1_bp1" depend="unk" label="トイウ" flag="U"/>
             </caseRelations>
             <namedEntities>
               <namedEntity id="s1_ne0" tokens="s1_tok0" label="PERSON"/>
             </namedEntities>
             <predicateArgumentRelations>
               <predicateArgumentRelation id="s1_par0" predicate="s1_bp1" argument="d0_coref0" label="ガ" flag="C"/>
             </predicateArgumentRelations>
           </sentence>
         </sentences>
         <coreferences>
           <coreference id="d0_coref0" basicPhrases="s0_bp0 s1_bp0"/>
           <coreference id="d0_coref1" basicPhrases="s0_bp1"/>
           <coreference id="d0_coref2" basicPhrases="s1_bp1"/>
         </coreferences>
       </document>
    ) (decided by sameElem)
  }
}
