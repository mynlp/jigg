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
import scala.xml._

class JumanAnnotatorSpec extends BaseAnnotatorSpec {

  def newJuman(output: String, p: Properties = new Properties) = new JumanAnnotator("", p) {
    override def mkCommunicator = new StubExternalCommunicator(output)
  }

  "Annotator" should "add token elements" in {

    val sample = """太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106"
は は は 助詞 9 副助詞 2 * 0 * 0 NIL
走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる"
EOS"""

    val input = <sentence id="s0">太郎は走った</sentence>

    val result = newJuman(sample).newSentenceAnnotation(input)

    result should equal (
      <sentence id="s0">太郎は走った
        <tokens>
          <token id="s0_tok0" surf="太郎" pos="名詞" pos1="人名" inflectionType="*" inflectionForm="*" base="太郎" reading="たろう" posId="6" pos1Id="5" inflectionTypeId="0" inflectionFormId="0" semantic="&quot;人名:日本:名:45:0.00106&quot;"/>
          <token id="s0_tok1" surf="は" pos="助詞" pos1="副助詞" inflectionType="*" inflectionForm="*" base="は" reading="は" posId="9" pos1Id="2" inflectionTypeId="0" inflectionFormId="0" semantic="NIL"/>
          <token id="s0_tok2" surf="走った" pos="動詞" pos1="*" inflectionType="子音動詞ラ行" inflectionForm="タ形" base="走る" reading="はしった" posId="2" pos1Id="0" inflectionTypeId="10" inflectionFormId="10" semantic="&quot;代表表記:走る/はしる&quot;"/>
        </tokens>
      </sentence>
    ) (decided by sameElem)
  }

  it should "append @ token as tokenAlt" in {

    val sample = """太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106"
は は は 助詞 9 副助詞 2 * 0 * 0 NIL
京都 きょうと 京都 名詞 6 地名 4 * 0 * 0 "代表表記:京都/きょうと 地名:日本:府"
@ 京都 きょうと 京都 名詞 6 地名 4 * 0 * 0 "代表表記:京都/きょうと 地名:日本:京都府:市"
に に に 助詞 9 格助詞 1 * 0 * 0 NIL
行った いった 行く 動詞 2 * 0 子音動詞カ行促音便形 3 タ形 10 "代表表記:行く/いく 付属動詞候補（タ系） ドメイン:交通 反義:動詞:帰る/かえる"
@ 行った おこなった 行う 動詞 2 * 0 子音動詞ワ行 12 タ形 10 "代表表記:行う/おこなう"
EOS"""

    val input = <sentence id="s0">太郎は京都に行った</sentence>

    val result = newJuman(sample).newSentenceAnnotation(input)

    result should equal (
      <sentence id="s0">太郎は京都に行った
        <tokens>
          <token id="s0_tok0" surf="太郎" pos="名詞" pos1="人名" inflectionType="*" inflectionForm="*" base="太郎" reading="たろう" posId="6" pos1Id="5" inflectionTypeId="0" inflectionFormId="0" semantic="&quot;人名:日本:名:45:0.00106&quot;"/>
          <token id="s0_tok1" surf="は" pos="助詞" pos1="副助詞" inflectionType="*" inflectionForm="*" base="は" reading="は" posId="9" pos1Id="2" inflectionTypeId="0" inflectionFormId="0" semantic="NIL"/>
          <token id="s0_tok2" surf="京都" pos="名詞" pos1="地名" inflectionType="*" inflectionForm="*" base="京都" reading="きょうと" posId="6" pos1Id="4" inflectionTypeId="0" inflectionFormId="0" semantic="&quot;代表表記:京都/きょうと 地名:日本:府&quot;">
            <tokenAlt id="s0_tok2_alt0" surf="京都" pos="名詞" pos1="地名" inflectionType="*" inflectionForm="*" base="京都" reading="きょうと" posId="6" pos1Id="4" inflectionTypeId="0" inflectionFormId="0" semantic="&quot;代表表記:京都/きょうと 地名:日本:京都府:市&quot;"/>
          </token>
          <token id="s0_tok3" surf="に" pos="助詞" pos1="格助詞" inflectionType="*" inflectionForm="*" base="に" reading="に" posId="9" pos1Id="1" inflectionTypeId="0" inflectionFormId="0" semantic="NIL"/>
          <token id="s0_tok4" surf="行った" pos="動詞" pos1="*" inflectionType="子音動詞カ行促音便形" inflectionForm="タ形" base="行く" reading="いった" posId="2" pos1Id="0" inflectionTypeId="3" inflectionFormId="10" semantic="&quot;代表表記:行く/いく 付属動詞候補（タ系） ドメイン:交通 反義:動詞:帰る/かえる&quot;">
            <tokenAlt id="s0_tok4_alt0" surf="行った" pos="動詞" pos1="*" inflectionType="子音動詞ワ行" inflectionForm="タ形" base="行う" reading="おこなった" posId="2" pos1Id="0" inflectionTypeId="12" inflectionFormId="10" semantic="&quot;代表表記:行う/おこなう&quot;"/>
          </token>
        </tokens>
      </sentence>
    ) (decided by sameElem)
  }

  it should "handle half space input correctly" in {

    val sample = """あ あ あ 感動詞 12 * 0 * 0 * 0 "代表表記:あ/あ"
  \  \  特殊 1 空白 6 * 0 * 0 NIL
い い いい 形容詞 3 * 0 イ形容詞イ段 19 文語基本形 18 "代表表記:良い/よい 反義:形容詞:悪い/わるい"
EOS"""

    val input = <sentence id="s0">あ い</sentence>

    val result = newJuman(sample).newSentenceAnnotation(input)

    result should equal(
      <sentence id="s0">あ い
        <tokens>
          <token id="s0_tok0" surf="あ" pos="感動詞" pos1="*" inflectionType="*" inflectionForm="*" base="あ" reading="あ" posId="12" pos1Id="0" inflectionTypeId="0" inflectionFormId="0" semantic="&quot;代表表記:あ/あ&quot;"/>
          <token id="s0_tok1" surf=" " pos="特殊" pos1="空白" inflectionType="*" inflectionForm="*" base="\ " reading="\ " posId="1" pos1Id="6" inflectionTypeId="0" inflectionFormId="0" semantic="NIL"/>
          <token id="s0_tok2" surf="い" pos="形容詞" pos1="*" inflectionType="イ形容詞イ段" inflectionForm="文語基本形" base="いい" reading="い" posId="3" pos1Id="0" inflectionTypeId="19" inflectionFormId="18" semantic="&quot;代表表記:良い/よい 反義:形容詞:悪い/わるい&quot;"/>
        </tokens>
      </sentence>
    ) (decided by sameElem)
  }
}
