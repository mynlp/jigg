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
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class SimpleKNPAnnotatorTest extends FunSuite {

  def newKNP(p: Properties = new Properties) = try Some(new SimpleKNPAnnotator("knp", p))
  catch { case e: Throwable => None }

  test("getTokens") {
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-0.93093
                   |* -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><判定詞><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><用言代表表記:太郎/たろう><時制-無時制><照応詞候補:太郎><格解析結果:太郎/たろう:判0:ガ/U/-/-/-/-;ニ/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;ガ２/U/-/-/-/-;ニトル/U/-/-/-/-><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><文末><表現文末><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expectedTokens = <tokens><token surf="太郎" reading="たろう" base="太郎" pos="名詞" posId="6" pos1="人名" pos1Id="5" inflectionType="*" inflectionTypeId="0" inflectionForm="*" inflectionFormId="0" features="&quot;人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう&quot; &lt;人名:日本:名:45:0.00106&gt;&lt;疑似代表表記&gt;&lt;代表表記:太郎/たろう&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;文頭&gt;&lt;文末&gt;&lt;表現文末&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;" id="s0_tok0"/></tokens>

    newKNP() foreach { _.getTokens(input, "s0") should be(expectedTokens) }
  }

  test("getBasicPhrases") {
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-0.93093
                   |* -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><判定詞><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><用言代表表記:太郎/たろう><時制-無時制><照応詞候補:太郎><格解析結果:太郎/たろう:判0:ガ/U/-/-/-/-;ニ/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;ガ２/U/-/-/-/-;ニトル/U/-/-/-/-><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><文末><表現文末><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞>
                   |EOS
""".stripMargin.split("\n").toSeq

    val feature = "<文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><判定詞><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><用言代表表記:太郎/たろう><時制-無時制><照応詞候補:太郎><格解析結果:太郎/たろう:判0:ガ/U/-/-/-/-;ニ/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;ガ２/U/-/-/-/-;ニトル/U/-/-/-/-><EID:0>"
    val expected = <basicPhrases><basicPhrase id="s0_bp0" tokens="s0_tok0" features={feature} /></basicPhrases>

    newKNP() foreach { _.getBasicPhrases(input, "s0") should be(expected) }
  }

  test("isChunk") {
    val input = "* -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>"

    newKNP() foreach { _.isChunk(input) should be(true) }
  }

  test("isBasicPhrase") {
    val input = "+ -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><判定詞><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><用言代表表記:太郎/たろう><時制-無時制><照応詞候補:太郎><格解析結果:太郎/たろう:判0:ガ/U/-/-/-/-;ニ/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;ガ２/U/-/-/-/-;ニトル/U/-/-/-/-><EID:0>"

    newKNP() foreach { _.isBasicPhrase(input) should be(true) }
  }

  test("isDocInfo") {
    val input = "# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-0.93093"

    newKNP() foreach { _.isDocInfo(input) should be(true) }
  }

  test("isToken") {
    val input = "太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 \"人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう\" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><文末><表現文末><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞>"

    newKNP() foreach { _.isToken(input) should be(true) }
  }

  test("isEOS") {
    val input = "EOS"

    newKNP() foreach { _.isEOS(input) should be(true) }
  }

  test("getBasicPhrase 2") {
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-7.16850
                   |* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>
                   |走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val feature1 = "<文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>"
    val feature2 = "<文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>"
    val expected = <basicPhrases><basicPhrase id="s0_bp0" tokens="s0_tok0 s0_tok1" features={feature1} /><basicPhrase id="s0_bp1" tokens="s0_tok2 s0_tok3" features={feature2} /></basicPhrases>

    newKNP() foreach { _.getBasicPhrases(input, "s0") should be(expected) }
  }

  test("getChunks") {
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-7.16850
                   |* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>
                   |走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val feature1 = "<文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>"
    val feature2 = "<文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>"

    val expected = <chunks><chunk id="s0_chu0" tokens="s0_tok0 s0_tok1" features={feature1} /><chunk id="s0_chu1" tokens="s0_tok2 s0_tok3" features={feature2}/></chunks>

    newKNP() foreach { _.getChunks(input, "s0") should be(expected) }
  }

  test("getBasicPhraseDependencies") {
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-7.16850
                   |* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>
                   |走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expected = <basicPhraseDependencies root="s0_bp1"><basicPhraseDependency id="s0_bpdep0" head="s0_bp1" dependent="s0_bp0" label="D"/></basicPhraseDependencies>

    newKNP() foreach { _.getBasicPhraseDependencies(input, "s0") should be(expected) }
  }

  test("getDependencies"){
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-7.16850
                   |* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>
                   |走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expected = <dependencies root="s0_chu1"><dependency id="s0_dep0" head="s0_chu1" dependent="s0_chu0" label="D"/></dependencies>

    newKNP() foreach { _.getDependencies(input, "s0") should be(expected) }
  }

  test("getCaseRelations"){
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-7.16850
                   |* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>
                   |走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expected = <caseRelations><caseRelation id="s0_cr0" head="s0_bp1" depend="s0_tok0" label="ガ" flag="C" /><caseRelation id="s0_cr1" head="s0_bp1" depend="unk" label="ヲ" flag="U" /><caseRelation id="s0_cr2" head="s0_bp1" depend="unk" label="ニ" flag="U" /><caseRelation id="s0_cr3" head="s0_bp1" depend="unk" label="ト" flag="U" /><caseRelation id="s0_cr4" head="s0_bp1" depend="unk" label="デ" flag="U" /><caseRelation id="s0_cr5" head="s0_bp1" depend="unk" label="カラ" flag="U" /><caseRelation id="s0_cr6" head="s0_bp1" depend="unk" label="ヨリ" flag="U" /><caseRelation id="s0_cr7" head="s0_bp1" depend="unk" label="マデ" flag="U" /><caseRelation id="s0_cr8" head="s0_bp1" depend="unk" label="時間" flag="U" /><caseRelation id="s0_cr9" head="s0_bp1" depend="unk" label="外の関係" flag="U" /><caseRelation id="s0_cr10" head="s0_bp1" depend="unk" label="ノ" flag="U" /><caseRelation id="s0_cr11" head="s0_bp1" depend="unk" label="修飾" flag="U" /><caseRelation id="s0_cr12" head="s0_bp1" depend="unk" label="トスル" flag="U" /><caseRelation id="s0_cr13" head="s0_bp1" depend="unk" label="ニオク" flag="U" /><caseRelation id="s0_cr14" head="s0_bp1" depend="unk" label="ニカンスル" flag="U" /><caseRelation id="s0_cr15" head="s0_bp1" depend="unk" label="ニヨル" flag="U" /><caseRelation id="s0_cr16" head="s0_bp1" depend="unk" label="ヲフクメル" flag="U" /><caseRelation id="s0_cr17" head="s0_bp1" depend="unk" label="ヲハジメル" flag="U" /><caseRelation id="s0_cr18" head="s0_bp1" depend="unk" label="ヲノゾク" flag="U" /><caseRelation id="s0_cr19" head="s0_bp1" depend="unk" label="ヲツウジル" flag="U" /></caseRelations>

    newKNP() foreach { knp =>
      val tokens = knp.getTokens(input, "s0")
      val bps = knp.getBasicPhrases(input, "s0")
      knp.getCaseRelations(input, tokens, bps, "s0") should be (expected)
    }
  }

  test("getCaseRelations 2"){
    val input = """|# S-ID:1 KNP:4.12-CF1.1 DATE:2015/01/18 SCORE:-22.40233
                   |* 1D <文頭><ガ><助詞><体言><一文字漢字><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:背/せ><主辞代表表記:背/せ>
                   |+ 1D <文頭><ガ><助詞><体言><一文字漢字><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><正規化代表表記:背/せ><照応詞候補:背><解析格:ガ><EID:0><述語項構造:背/せ:名1>
                   |背 せ 背 名詞 6 普通名詞 1 * 0 * 0 "代表表記:背/せ 漢字読み:訓 〜を〜に構成語 カテゴリ:動物-部位;場所-機能" <代表表記:背/せ><漢字読み:訓><〜を〜に構成語><カテゴリ:動物-部位;場所-機能><正規化代表表記:背/せ><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><文節主辞><係:ガ格>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* 2D <連体修飾><用言:形><係:連格><レベル:B-><区切:0-5><ID:（形判連体）><連体節><状態述語><正規化代表表記:高い/たかい><主辞代表表記:高い/たかい>
                   |+ 2D <連体修飾><用言:形><係:連格><レベル:B-><区切:0-5><ID:（形判連体）><連体節><状態述語><正規化代表表記:高い/たかい><用言代表表記:高い/たかい><時制-現在><時制-無時制><格関係0:ガ:背><格関係2:ガ２:人><格解析結果:高い/たかい:形10:ガ/C/背/0/0/1;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ガ２/N/人/2/0/1;ノ/U/-/-/-/-;ニクラベル/U/-/-/-/-;トスル/U/-/-/-/-;トイウ/U/-/-/-/-;ニアワセル/U/-/-/-/-;ニトル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ニカギル/U/-/-/-/-><EID:1><述語項構造:高い/たかい:形10:ガ２/N/人/2;ガ/C/背/0>
                   |高い たかい 高い 形容詞 3 * 0 イ形容詞アウオ段 18 基本形 2 "代表表記:高い/たかい 反義:形容詞:安い/やすい;形容詞:低い/ひくい" <代表表記:高い/たかい><反義:形容詞:安い/やすい;形容詞:低い/ひくい><正規化代表表記:高い/たかい><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞><係:連格>
                   |* 3D <SM-主体><SM-人><ガ><助詞><体言><一文字漢字><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:人/じん?人/ひと><主辞代表表記:人/じん?人/ひと>
                   |+ 3D <SM-主体><SM-人><ガ><助詞><体言><一文字漢字><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><正規化代表表記:人/じん?人/ひと><照応詞候補:人><解析連格:ガ２><解析格:ガ><EID:2><述語項構造:人/じん?人/ひと:名1>
                   |人 じん 人 名詞 6 普通名詞 1 * 0 * 0 "代表表記:人/じん 漢字読み:音 カテゴリ:人" <代表表記:人/じん><漢字読み:音><カテゴリ:人><正規化代表表記:人/じん?人/ひと><品曖><ALT-人-ひと-人-6-1-0-0-"代表表記:人/ひと 漢字読み:訓 カテゴリ:人"><品曖-普通名詞><原形曖昧><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><文節主辞><名詞曖昧性解消><係:ガ格>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><用言:動><レベル:C><区切:5-5><ID:（文末）><提題受:30><主節><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><用言:動><レベル:C><区切:5-5><ID:（文末）><提題受:30><主節><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><時制-未来><主題格:一人称優位><格関係2:ガ:人><格解析結果:走る/はしる:動13:ガ/C/人/2/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:3><述語項構造:走る/はしる:動13:ガ/C/人/2>
                   |走って はしって 走る 動詞 2 * 0 子音動詞ラ行 10 タ系連用テ形 14 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |いる いる いる 接尾辞 14 動詞性接尾辞 7 母音動詞 1 基本形 2 "代表表記:いる/いる" <代表表記:いる/いる><正規化代表表記:いる/いる><文末><表現文末><かな漢字><ひらがな><活用語><付属>
                   |EOS
""".stripMargin.split("\n").toSeq


    val expected = <caseRelations><caseRelation flag="C" label="ガ" depend="s0_tok0" head="s0_bp1" id="s0_cr0"/><caseRelation flag="U" label="ニ" depend="unk" head="s0_bp1" id="s0_cr1"/><caseRelation flag="U" label="ト" depend="unk" head="s0_bp1" id="s0_cr2"/><caseRelation flag="U" label="デ" depend="unk" head="s0_bp1" id="s0_cr3"/><caseRelation flag="U" label="カラ" depend="unk" head="s0_bp1" id="s0_cr4"/><caseRelation flag="U" label="ヨリ" depend="unk" head="s0_bp1" id="s0_cr5"/><caseRelation flag="U" label="マデ" depend="unk" head="s0_bp1" id="s0_cr6"/><caseRelation flag="U" label="ヘ" depend="unk" head="s0_bp1" id="s0_cr7"/><caseRelation flag="U" label="時間" depend="unk" head="s0_bp1" id="s0_cr8"/><caseRelation flag="U" label="外の関係" depend="unk" head="s0_bp1" id="s0_cr9"/><caseRelation flag="U" label="修飾" depend="unk" head="s0_bp1" id="s0_cr10"/><caseRelation flag="N" label="ガ２" depend="s0_tok3" head="s0_bp1" id="s0_cr11"/><caseRelation flag="U" label="ノ" depend="unk" head="s0_bp1" id="s0_cr12"/><caseRelation flag="U" label="ニクラベル" depend="unk" head="s0_bp1" id="s0_cr13"/><caseRelation flag="U" label="トスル" depend="unk" head="s0_bp1" id="s0_cr14"/><caseRelation flag="U" label="トイウ" depend="unk" head="s0_bp1" id="s0_cr15"/><caseRelation flag="U" label="ニアワセル" depend="unk" head="s0_bp1" id="s0_cr16"/><caseRelation flag="U" label="ニトル" depend="unk" head="s0_bp1" id="s0_cr17"/><caseRelation flag="U" label="ヲハジメル" depend="unk" head="s0_bp1" id="s0_cr18"/><caseRelation flag="U" label="ニカギル" depend="unk" head="s0_bp1" id="s0_cr19"/><caseRelation flag="C" label="ガ" depend="s0_tok3" head="s0_bp3" id="s0_cr20"/><caseRelation flag="U" label="ヲ" depend="unk" head="s0_bp3" id="s0_cr21"/><caseRelation flag="U" label="ニ" depend="unk" head="s0_bp3" id="s0_cr22"/><caseRelation flag="U" label="ト" depend="unk" head="s0_bp3" id="s0_cr23"/><caseRelation flag="U" label="デ" depend="unk" head="s0_bp3" id="s0_cr24"/><caseRelation flag="U" label="カラ" depend="unk" head="s0_bp3" id="s0_cr25"/><caseRelation flag="U" label="ヨリ" depend="unk" head="s0_bp3" id="s0_cr26"/><caseRelation flag="U" label="マデ" depend="unk" head="s0_bp3" id="s0_cr27"/><caseRelation flag="U" label="時間" depend="unk" head="s0_bp3" id="s0_cr28"/><caseRelation flag="U" label="外の関係" depend="unk" head="s0_bp3" id="s0_cr29"/><caseRelation flag="U" label="ノ" depend="unk" head="s0_bp3" id="s0_cr30"/><caseRelation flag="U" label="修飾" depend="unk" head="s0_bp3" id="s0_cr31"/><caseRelation flag="U" label="トスル" depend="unk" head="s0_bp3" id="s0_cr32"/><caseRelation flag="U" label="ニオク" depend="unk" head="s0_bp3" id="s0_cr33"/><caseRelation flag="U" label="ニカンスル" depend="unk" head="s0_bp3" id="s0_cr34"/><caseRelation flag="U" label="ニヨル" depend="unk" head="s0_bp3" id="s0_cr35"/><caseRelation flag="U" label="ヲフクメル" depend="unk" head="s0_bp3" id="s0_cr36"/><caseRelation flag="U" label="ヲハジメル" depend="unk" head="s0_bp3" id="s0_cr37"/><caseRelation flag="U" label="ヲノゾク" depend="unk" head="s0_bp3" id="s0_cr38"/><caseRelation flag="U" label="ヲツウジル" depend="unk" head="s0_bp3" id="s0_cr39"/></caseRelations>

    newKNP() foreach { knp =>
      val tokens = knp.getTokens(input, "s0")
      val bps = knp.getBasicPhrases(input, "s0")
      knp.getCaseRelations(input, tokens, bps, "s0") should be (expected)
    }
  }

  //S-ID:d1-s0 (not S-ID:1) is fine
  test("getCaseRelations 3; S-ID:d1-s0"){
    val input = """|# S-ID:d1-s0 JUMAN:7.01 KNP:4.12-CF1.1 DATE:2015/05/12 SCORE:-7.16850
                   |* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><解析格:ガ>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><時制-未来><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/d1-s0;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/->
                   |走る はしる 走る 動詞 2 * 0 子音動詞ラ行 10 基本形 2 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expected = <caseRelations><caseRelation flag="C" label="ガ" depend="s0_tok0" head="s0_bp1" id="s0_cr0"/><caseRelation flag="U" label="ヲ" depend="unk" head="s0_bp1" id="s0_cr1"/><caseRelation flag="U" label="ニ" depend="unk" head="s0_bp1" id="s0_cr2"/><caseRelation flag="U" label="ト" depend="unk" head="s0_bp1" id="s0_cr3"/><caseRelation flag="U" label="デ" depend="unk" head="s0_bp1" id="s0_cr4"/><caseRelation flag="U" label="カラ" depend="unk" head="s0_bp1" id="s0_cr5"/><caseRelation flag="U" label="ヨリ" depend="unk" head="s0_bp1" id="s0_cr6"/><caseRelation flag="U" label="マデ" depend="unk" head="s0_bp1" id="s0_cr7"/><caseRelation flag="U" label="時間" depend="unk" head="s0_bp1" id="s0_cr8"/><caseRelation flag="U" label="外の関係" depend="unk" head="s0_bp1" id="s0_cr9"/><caseRelation flag="U" label="ノ" depend="unk" head="s0_bp1" id="s0_cr10"/><caseRelation flag="U" label="修飾" depend="unk" head="s0_bp1" id="s0_cr11"/><caseRelation flag="U" label="トスル" depend="unk" head="s0_bp1" id="s0_cr12"/><caseRelation flag="U" label="ニオク" depend="unk" head="s0_bp1" id="s0_cr13"/><caseRelation flag="U" label="ニカンスル" depend="unk" head="s0_bp1" id="s0_cr14"/><caseRelation flag="U" label="ニヨル" depend="unk" head="s0_bp1" id="s0_cr15"/><caseRelation flag="U" label="ヲフクメル" depend="unk" head="s0_bp1" id="s0_cr16"/><caseRelation flag="U" label="ヲハジメル" depend="unk" head="s0_bp1" id="s0_cr17"/><caseRelation flag="U" label="ヲノゾク" depend="unk" head="s0_bp1" id="s0_cr18"/><caseRelation flag="U" label="ヲツウジル" depend="unk" head="s0_bp1" id="s0_cr19"/></caseRelations>

    newKNP() foreach { knp =>
      val tokens = knp.getTokens(input, "s0")
      val bps = knp.getBasicPhrases(input, "s0")
      knp.getCaseRelations(input, tokens, bps, "s0") should be (expected)
    }
  }

  test("getNamedEntities"){
    val input = """|# S-ID:1 KNP:4.12-CF1.1 DATE:2015/01/17 SCORE:-22.46564
                   |* 1D <文頭><サ変><組織名疑><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:国際/こくさい+連合/れんごう><主辞代表表記:連合/れんごう>
                   |+ 1D <文節内><係:文節内><文頭><体言><名詞項候補><先行詞候補><正規化代表表記:国際/こくさい><NE内:ORGANIZATION><EID:0>
                   |国際 こくさい 国際 名詞 6 普通名詞 1 * 0 * 0 "代表表記:国際/こくさい カテゴリ:抽象物" <代表表記:国際/こくさい><カテゴリ:抽象物><正規化代表表記:国際/こくさい><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><NE:ORGANIZATION:B>
                   |+ 2D <組織名疑><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><サ変><SM-主体><SM-組織><名詞項候補><先行詞候補><非用言格解析:動><照応ヒント:係><態:未定><正規化代表表記:連合/れんごう><NE:ORGANIZATION:国際連合><Wikipedia上位語:国際組織><Wikipediaエントリ:国際聯合><Wikipediaリダイレクト:国際連合><照応詞候補:国際連合><格解析結果:連合/れんごう:動0:ガ/U/-/-/-/-;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;時間/U/-/-/-/-;ニクワエル/U/-/-/-/-;ノ/U/-/-/-/-;ニヨル/U/-/-/-/-;修飾/U/-/-/-/-;ニオク/U/-/-/-/-;ニモトヅク/U/-/-/-/-;ガ２/U/-/-/-/-;外の関係/U/-/-/-/-><解析格:ガ><EID:1>
                   |連合 れんごう 連合 名詞 6 サ変名詞 2 * 0 * 0 "代表表記:連合/れんごう 組織名末尾 カテゴリ:組織・団体;抽象物" <代表表記:連合/れんごう><組織名末尾><カテゴリ:組織・団体;抽象物><正規化代表表記:連合/れんごう><Wikipedia上位語:国際組織:0-1><Wikipediaエントリ:国際聯合:0-1><Wikipediaリダイレクト:国際連合:0-1><漢字><かな漢字><名詞相当語><サ変><自立><複合←><内容語><タグ単位始><文節主辞><係:ガ格><NE:ORGANIZATION:E>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><サ変><サ変動詞><時制-過去><態:受動><〜られる><用言:動><レベル:C><区切:5-5><ID:（文末）><提題受:30><主節><動態述語><正規化代表表記:設立/せつりつ><主辞代表表記:設立/せつりつ>
                   |+ -1D <文末><サ変動詞><時制-過去><態:受動><〜られる><用言:動><レベル:C><区切:5-5><ID:（文末）><提題受:30><主節><動態述語><サ変><正規化代表表記:設立/せつりつ><用言代表表記:設立/せつりつ+する/する+れる/れる><主題格:一人称優位><格関係1:ガ:連合><格解析結果:設立/せつりつ+する/する+れる/れる:動1:ガ/C/連合/1/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;トスル/U/-/-/-/-;ニヨル/U/-/-/-/-;修飾/U/-/-/-/-;ニモトヅク/U/-/-/-/-;ニオク/U/-/-/-/-;ニトモナウ/U/-/-/-/-;ニツク/U/-/-/-/-;ニムケル/U/-/-/-/-;ニツヅク/U/-/-/-/-><正規化格解析結果-0:設立/せつりつ:動1:ヲ/C/連合/1/0/1><EID:2><述語項構造:設立/せつりつ+する/する+れる/れる:動1:ガ/C/国際連合/1>
                   |設立 せつりつ 設立 名詞 6 サ変名詞 2 * 0 * 0 "代表表記:設立/せつりつ カテゴリ:抽象物" <代表表記:設立/せつりつ><カテゴリ:抽象物><正規化代表表記:設立/せつりつ><漢字><かな漢字><名詞相当語><サ変><サ変動詞><自立><内容語><タグ単位始><文節始><文節主辞>
                   |さ さ する 動詞 2 * 0 サ変動詞 16 未然形 3 "代表表記:する/する 付属動詞候補（基本） 自他動詞:自:成る/なる" <代表表記:する/する><付属動詞候補（基本）><自他動詞:自:成る/なる><正規化代表表記:する/する><とタ系連用テ形複合辞><かな漢字><ひらがな><活用語><付属>
                   |れた れた れる 接尾辞 14 動詞性接尾辞 7 母音動詞 1 タ形 10 "代表表記:れる/れる" <代表表記:れる/れる><正規化代表表記:れる/れる><文末><表現文末><かな漢字><ひらがな><活用語><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expected = <namedEntities><namedEntity id="s0_ne0" tokens="s0_tok0 s0_tok1" label="ORGANIZATION" /></namedEntities>

    newKNP() foreach { knp =>
      knp.getNamedEntities(input, "s0") should be (expected)
    }
  }

  test("getNamedEntities 2"){
    val input = """|# S-ID:1 KNP:4.12-CF1.1 DATE:2015/01/20 SCORE:-7.16850
                   |* 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ 1D <文頭><人名><ガ><助詞><体言><係:ガ格><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><NE:PERSON:太郎><照応詞候補:太郎><解析格:ガ><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞><係:ガ格><NE:PERSON:S>
                   |が が が 助詞 9 格助詞 1 * 0 * 0 NIL <かな漢字><ひらがな><付属>
                   |* -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><主辞代表表記:走る/はしる>
                   |+ -1D <文末><時制-過去><句点><用言:動><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><動態述語><正規化代表表記:走る/はしる><用言代表表記:走る/はしる><主題格:一人称優位><格関係0:ガ:太郎><格解析結果:走る/はしる:動13:ガ/C/太郎/0/0/1;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;ト/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;ノ/U/-/-/-/-;修飾/U/-/-/-/-;トスル/U/-/-/-/-;ニオク/U/-/-/-/-;ニカンスル/U/-/-/-/-;ニヨル/U/-/-/-/-;ヲフクメル/U/-/-/-/-;ヲハジメル/U/-/-/-/-;ヲノゾク/U/-/-/-/-;ヲツウジル/U/-/-/-/-><EID:1><述語項構造:走る/はしる:動13:ガ/C/太郎/0>
                   |走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる" <代表表記:走る/はしる><正規化代表表記:走る/はしる><表現文末><かな漢字><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
                   |。 。 。 特殊 1 句点 1 * 0 * 0 NIL <文末><英記号><記号><付属>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expected = <namedEntities><namedEntity id="s0_ne0" tokens="s0_tok0" label="PERSON" /></namedEntities>

    newKNP() foreach { knp =>
      knp.getNamedEntities(input, "s0") should be (expected)
    }
  }

  test("recovJumanOutput"){
    val jumanTokens = <tokens><token features="&quot;人名:日本:名:45:0.00106&quot;" inflectionFormId="0" inflectionTypeId="0" pos1Id="5" posId="6" reading="たろう" base="太郎" inflectionForm="*" inflectionType="*" pos1="人名" pos="名詞" surf="太郎" id="s0_tok0"/><token features="NIL" inflectionFormId="0" inflectionTypeId="0" pos1Id="1" posId="9" reading="が" base="が" inflectionForm="*" inflectionType="*" pos1="格助詞" pos="助詞" surf="が" id="s0_tok1"/><token features="&quot;代表表記:本/ほん 漢字読み:音 カテゴリ:人工物-その他;抽象物&quot;" inflectionFormId="0" inflectionTypeId="0" pos1Id="1" posId="6" reading="ほん" base="本" inflectionForm="*" inflectionType="*" pos1="普通名詞" pos="名詞" surf="本" id="s0_tok2"/><token features="NIL" inflectionFormId="0" inflectionTypeId="0" pos1Id="1" posId="9" reading="を" base="を" inflectionForm="*" inflectionType="*" pos1="格助詞" pos="助詞" surf="を" id="s0_tok3"/><token features="&quot;代表表記:買う/かう ドメイン:家庭・暮らし;ビジネス 反義:動詞:売る/うる&quot;" inflectionFormId="10" inflectionTypeId="12" pos1Id="0" posId="2" reading="かった" base="買う" inflectionForm="タ形" inflectionType="子音動詞ワ行" pos1="*" pos="動詞" surf="買った" id="s0_tok4"/></tokens>

    val expected = Seq("太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 \"人名:日本:名:45:0.00106\"\n", "が が が 助詞 9 格助詞 1 * 0 * 0 NIL\n", "本 ほん 本 名詞 6 普通名詞 1 * 0 * 0 \"代表表記:本/ほん 漢字読み:音 カテゴリ:人工物-その他;抽象物\"\n", "を を を 助詞 9 格助詞 1 * 0 * 0 NIL\n", "買った かった 買う 動詞 2 * 0 子音動詞ワ行 12 タ形 10 \"代表表記:買う/かう ドメイン:家庭・暮らし;ビジネス 反義:動詞:売る/うる\"\n", "EOS\n")

    newKNP() foreach { knp =>
      knp.recovJumanOutput(jumanTokens) should be (expected)
    }
  }
}
