package enju.pipeline

import java.util.Properties
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class KNPAnnotatorTest extends FunSuite {
  test("getTokens") {
    val input = """|# S-ID:1 KNP:4.11-CF1.1 DATE:2015/01/13 SCORE:-0.93093
                   |* -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><正規化代表表記:太郎/たろう><主辞代表表記:太郎/たろう>
                   |+ -1D <文頭><文末><人名><体言><用言:判><体言止><レベル:C><区切:5-5><ID:（文末）><裸名詞><提題受:30><主節><状態述語><判定詞><名詞項候補><先行詞候補><SM-人><SM-主体><正規化代表表記:太郎/たろう><用言代表表記:太郎/たろう><時制-無時制><照応詞候補:太郎><格解析結果:太郎/たろう:判0:ガ/U/-/-/-/-;ニ/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヨリ/U/-/-/-/-;マデ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-;修飾/U/-/-/-/-;ノ/U/-/-/-/-;ガ２/U/-/-/-/-;ニトル/U/-/-/-/-><EID:0>
                   |太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう" <人名:日本:名:45:0.00106><疑似代表表記><代表表記:太郎/たろう><正規化代表表記:太郎/たろう><文頭><文末><表現文末><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><固有キー><文節主辞>
                   |EOS
""".stripMargin.split("\n").toSeq

    val expected_tokens = <tokens><token surf="太郎" reading="たろう" base="太郎" pos="名詞" pos_id="6" pos1="人名" pos1_id="5" inflectionType="*" inflectionType_id="0" inflectionForm="*" inflectionForm_id="0" features="人名:日本:名:45:0.00106 疑似代表表記 代表表記:太郎/たろう &lt;人名:日本:名:45:0.00106&gt;&lt;疑似代表表記&gt;&lt;代表表記:太郎/たろう&gt;&lt;正規化代表表記:太郎/たろう&gt;&lt;文頭&gt;&lt;文末&gt;&lt;表現文末&gt;&lt;漢字&gt;&lt;かな漢字&gt;&lt;名詞相当語&gt;&lt;自立&gt;&lt;内容語&gt;&lt;タグ単位始&gt;&lt;文節始&gt;&lt;固有キー&gt;&lt;文節主辞&gt;" id="s0_0"/></tokens>

    val knp = new KNPAnnotator("knp", new Properties)

    knp.getTokens(input, "s0") should be(expected_tokens)
  }


}
