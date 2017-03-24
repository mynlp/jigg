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

  def newJuman(output: String, p: Properties = new Properties) =
    new JumanAnnotator("juman", p) {
      override def nThreads = 1
      override def mkLocalAnnotator = new LocalJumanAnnotator {
        override def mkCommunicator = new StubExternalCommunicator(output)
      }
    }

  "Annotator" should "add token elements" in {

    val sample = """太郎 たろう 太郎 名詞 6 人名 5 * 0 * 0 "人名:日本:名:45:0.00106"
は は は 助詞 9 副助詞 2 * 0 * 0 NIL
走った はしった 走る 動詞 2 * 0 子音動詞ラ行 10 タ形 10 "代表表記:走る/はしる"
EOS"""

    val input =
      <root><document><sentences>
        <sentence id="s0">太郎は走った</sentence>
      </sentences></document></root>

    val result = newJuman(sample).annotate(input)

    result should equal (
      <root><document><sentences>
      <sentence id="s0">太郎は走った
        <tokens annotators="juman" normalized="true">
          <token id="s0_tok0" form="太郎" characterOffsetBegin="0" characterOffsetEnd="2" pos="名詞" pos1="人名" cType="*" cForm="*" lemma="太郎" yomi="たろう" posId="6" pos1Id="5" cTypeId="0" cFormId="0" misc="&quot;人名:日本:名:45:0.00106&quot;"/>
          <token id="s0_tok1" form="は"  characterOffsetBegin="2" characterOffsetEnd="3" pos="助詞" pos1="副助詞" cType="*" cForm="*" lemma="は" yomi="は" posId="9" pos1Id="2" cTypeId="0" cFormId="0" misc="NIL"/>
          <token id="s0_tok2" form="走った" characterOffsetBegin="3" characterOffsetEnd="6" pos="動詞" pos1="*" cType="子音動詞ラ行" cForm="タ形" lemma="走る" yomi="はしった" posId="2" pos1Id="0" cTypeId="10" cFormId="10" misc="&quot;代表表記:走る/はしる&quot;"/>
        </tokens>
      </sentence>
      </sentences></document></root>
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

    val input =
      <root><document><sentences>
        <sentence id="s0">太郎は京都に行った</sentence>
      </sentences></document></root>

    val result = newJuman(sample).annotate(input)

    result should equal (
      <root><document><sentences>
      <sentence id="s0">太郎は京都に行った
        <tokens annotators="juman" normalized="true">
          <token id="s0_tok0" form="太郎" characterOffsetBegin="0" characterOffsetEnd="2" pos="名詞" pos1="人名" cType="*" cForm="*" lemma="太郎" yomi="たろう" posId="6" pos1Id="5" cTypeId="0" cFormId="0" misc="&quot;人名:日本:名:45:0.00106&quot;"/>
          <token id="s0_tok1" form="は" characterOffsetBegin="2" characterOffsetEnd="3" pos="助詞" pos1="副助詞" cType="*" cForm="*" lemma="は" yomi="は" posId="9" pos1Id="2" cTypeId="0" cFormId="0" misc="NIL"/>
          <token id="s0_tok2" form="京都" characterOffsetBegin="3" characterOffsetEnd="5" pos="名詞" pos1="地名" cType="*" cForm="*" lemma="京都" yomi="きょうと" posId="6" pos1Id="4" cTypeId="0" cFormId="0" misc="&quot;代表表記:京都/きょうと 地名:日本:府&quot;">
            <tokenAlt id="s0_tok2_alt0" form="京都" characterOffsetBegin="3" characterOffsetEnd="5" pos="名詞" pos1="地名" cType="*" cForm="*" lemma="京都" yomi="きょうと" posId="6" pos1Id="4" cTypeId="0" cFormId="0" misc="&quot;代表表記:京都/きょうと 地名:日本:京都府:市&quot;"/>
          </token>
          <token id="s0_tok3" form="に" characterOffsetBegin="5" characterOffsetEnd="6" pos="助詞" pos1="格助詞" cType="*" cForm="*" lemma="に" yomi="に" posId="9" pos1Id="1" cTypeId="0" cFormId="0" misc="NIL"/>
          <token id="s0_tok4" form="行った" characterOffsetBegin="6" characterOffsetEnd="9" pos="動詞" pos1="*" cType="子音動詞カ行促音便形" cForm="タ形" lemma="行く" yomi="いった" posId="2" pos1Id="0" cTypeId="3" cFormId="10" misc="&quot;代表表記:行く/いく 付属動詞候補（タ系） ドメイン:交通 反義:動詞:帰る/かえる&quot;">
            <tokenAlt id="s0_tok4_alt0" form="行った" characterOffsetBegin="6" characterOffsetEnd="9" pos="動詞" pos1="*" cType="子音動詞ワ行" cForm="タ形" lemma="行う" yomi="おこなった" posId="2" pos1Id="0" cTypeId="12" cFormId="10" misc="&quot;代表表記:行う/おこなう&quot;"/>
          </token>
        </tokens>
      </sentence>
      </sentences></document></root>
    ) (decided by sameElem)
  }

  it should "handle half space input correctly when normalize=false" in {

    val sample = """あ あ あ 感動詞 12 * 0 * 0 * 0 "代表表記:あ/あ"
  \  \  特殊 1 空白 6 * 0 * 0 NIL
い い いい 形容詞 3 * 0 イ形容詞イ段 19 文語基本形 18 "代表表記:良い/よい 反義:形容詞:悪い/わるい"
EOS"""

//     val sample = """あ あ あ 感動詞 12 * 0 * 0 * 0 "代表表記:あ/あ"
// 　 　 　 特殊 1 空白 6 * 0 * 0 NIL
// い い いい 形容詞 3 * 0 イ形容詞イ段 19 文語基本形 18 "代表表記:良い/よい 反義:形容詞:悪い/わるい"
// EOS"""
    val input =
      <root><document><sentences>
        <sentence id="s0">あ い</sentence>
      </sentences></document></root>

    val juman = newJuman(sample)
    juman.normalize = false

    val result = juman.annotate(input)

    result should equal(
      <root><document><sentences>
      <sentence id="s0">あ い
        <tokens annotators="juman" normalized="false">
          <token id="s0_tok0" form="あ" characterOffsetBegin="0" characterOffsetEnd="1" pos="感動詞" pos1="*" cType="*" cForm="*" lemma="あ" yomi="あ" posId="12" pos1Id="0" cTypeId="0" cFormId="0" misc="&quot;代表表記:あ/あ&quot;"/>
          <token id="s0_tok1" form=" " characterOffsetBegin="1" characterOffsetEnd="2" pos="特殊" pos1="空白" cType="*" cForm="*" lemma="\ " yomi="\ " posId="1" pos1Id="6" cTypeId="0" cFormId="0" misc="NIL"/>
          <token id="s0_tok2" form="い" characterOffsetBegin="2" characterOffsetEnd="3" pos="形容詞" pos1="*" cType="イ形容詞イ段" cForm="文語基本形" lemma="いい" yomi="い" posId="3" pos1Id="0" cTypeId="19" cFormId="18" misc="&quot;代表表記:良い/よい 反義:形容詞:悪い/わるい&quot;"/>
        </tokens>
      </sentence>
      </sentences></document></root>
    ) (decided by sameElem)
  }
}
