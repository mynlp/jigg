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

  def newKNP(output: Seq[String], p: Properties = new Properties) =
    new DocumentKNPAnnotator("knpDoc", p) {
      override def nThreads = 1
      override def mkLocalAnnotator = new DocumentKNPLocalAnnotator {
        override def mkCommunicator = new StubExternalCommunicator(output)
      }
  }

  "Annotator" should "add modifed tokens, basicPhrases, chunks, basicPhraseDependencies, dependencies, caseRelations, namedEntities" in {

    val jumanOutputNode =
      <root><document id="d0">
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
    </document></root>

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

    val result =
      (newKNP(knpOutputs).annotate(jumanOutputNode) \\ "document")(0)

    val coreferences = result \ "coreferences"

    coreferences.head should equal (
      <coreferences annotators="knpDoc">
        <coreference id="d0_knpcr0" mentions="s0_knpbp0 s1_knpbp0"/>
        <coreference id="d0_knpcr1" mentions="s0_knpbp1"/>
        <coreference id="d0_knpcr2" mentions="s1_knpbp1"/>
      </coreferences>
    ) (decided by sameElem)

    val predArgs = result \\ "predArgs"

    predArgs(0) should equal (
      <predArgs annotators="knpDoc">
        <predArg id="s0_knppr0" pred="s0_knpbp1" arg="d0_knpcr0" deprel="ガ" flag="C"/>
      </predArgs>
    ) (decided by sameElem)

    predArgs(1) should equal (
      <predArgs annotators="knpDoc">
        <predArg id="s1_knppr0" pred="s1_knpbp1" arg="d0_knpcr0" deprel="ガ" flag="C"/>
      </predArgs>
    ) (decided by sameElem)
  }
}
