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
import scala.xml.Node
import org.scalatest._

class CabochaAnnotatorSpec extends BaseAnnotatorSpec {

  def newIPA(output: String, p: Properties = new Properties) =
    new IPACabochaAnnotator("cabocha", p) {
      override def nThreads = 1 // prallelism is already tested for mecab.
      override def mkLocalAnnotator = new IPALocalCabochaAnnotator {
        override def mkCommunicator = new StubExternalCommunicator(output)
      }
    }

  "Annotator" should "add root dependency to one word sentence" in {

    val s = "a"
    val annotator = newIPA(Sentences.cabocha(s))
    val result = annotator.annotate(Sentences.xml(s))

    val chunks = result \\ "chunks"
    chunks.size should be (1)
    chunks.head should equal (
      <chunks annotators="cabocha"><chunk id="s0_chu0" tokens="s0_tok0" head="s0_tok0" func="s0_tok0"/></chunks>)

    val deps = result \\ "dependencies"
    deps.size should be (1)
    deps.head should equal (
      <dependencies annotators="cabocha"><dependency unit="chunk" id="s0_dep0" head="root" dependent="s0_chu0" deprel="D"/></dependencies>)

    val sentences = result \\ "sentence"

    sentences.size should be (1)
    (sentences \ "_" \ "_").size should be (3)
  }

  it should "add all chunks and dependencies for a sentence" in {

    val s = "ipa3words"
    val annotator = newIPA(Sentences.cabocha(s))
    val result = annotator.annotate(Sentences.xml(s))

    val chunks = result \\ "chunks"

    chunks.size should be (1)
    chunks.head should equal (
      <chunks annotators="cabocha">
        <chunk id="s0_chu0" tokens="s0_tok0 s0_tok1" head="s0_tok0" func="s0_tok1"/>
        <chunk id="s0_chu1" tokens="s0_tok2 s0_tok3" head="s0_tok2" func="s0_tok3"/>
        <chunk id="s0_chu2" tokens="s0_tok4 s0_tok5" head="s0_tok4" func="s0_tok5"/>
      </chunks>
    ) (decided by sameElem)

    val deps = result \\ "dependencies"
    deps.size should be (1)
    deps.head should equal (
      <dependencies annotators="cabocha">
        <dependency unit="chunk" id="s0_dep0" head="s0_chu2" dependent="s0_chu0" deprel="D"/>
        <dependency unit="chunk" id="s0_dep1" head="s0_chu2" dependent="s0_chu1" deprel="D"/>
        <dependency unit="chunk" id="s0_dep2" head="root" dependent="s0_chu2" deprel="D"/>
      </dependencies>
    ) (decided by sameElem)
  }

  object Sentences {

    val xml = Map(
      "a" -> <root><document><sentences><sentence id="s0">あ
        <tokens>
        <token id="s0_tok0" surf="あ" pos="フィラー" pos1="*" pos2="*" pos3="*" inflectionType="*" inflectionForm="*" base="あ" reading="ア" pronounce="ア"/>
        </tokens>
        </sentence></sentences></document></root>,

      "annotated" -> <root><document><sentences><sentence id="s0">あ
        <tokens>
        <token id="s0_tok0" surf="あ" pos="フィラー" pos1="*" pos2="*" pos3="*" inflectionType="*" inflectionForm="*" base="あ" reading="ア" pronounce="ア"/>
        </tokens>
        <chunks><chunk id="s0_chu0" tokens="s0_tok0" head="" func="s0_tok0"/></chunks>
        <dependencies><dependency unit="chunk" id="s0_dep0" head="" dependent="s0_chu0" deprel="D"/></dependencies>
        </sentence></sentences></document></root>,

      "ipa3words" ->
        <root><document><sentences><sentence id="s0">
          太郎は京都に行った
          <tokens>
            <token id="s0_tok0" surf="太郎" pos="名詞" pos1="固有名詞" pos2="人名" pos3="名" inflectionType="*" inflectionForm="*" base="太郎" reading="タロウ" pronounce="タロー"/>
            <token id="s0_tok1" surf="は" pos="助詞" pos1="係助詞" pos2="*" pos3="*" inflectionType="*" inflectionForm="*" base="は" reading="ハ" pronounce="ワ"/>
            <token id="s0_tok2" surf="京都" pos="名詞" pos1="固有名詞" pos2="地域" pos3="一般" inflectionType="*" inflectionForm="*" base="京都" reading="キョウト" pronounce="キョート"/>
            <token id="s0_tok3" surf="に" pos="助詞" pos1="格助詞" pos2="一般" pos3="*" inflectionType="*" inflectionForm="*" base="に" reading="ニ" pronounce="ニ"/>
            <token id="s0_tok4" surf="行っ" pos="動詞" pos1="自立" pos2="*" pos3="*" inflectionType="五段・カ行促音便" inflectionForm="連用タ接続" base="行く" reading="イッ" pronounce="イッ"/>
            <token id="s0_tok5" surf="た" pos="助動詞" pos1="*" pos2="*" pos3="*" inflectionType="特殊・タ" inflectionForm="基本形" base="た" reading="タ" pronounce="タ"/>
          </tokens>
        </sentence></sentences></document></root>
    )

    val cabocha = Map(
      "a" -> """* 0 -1D 0/0 0.000000
あ	フィラー,*,*,*,*,*,あ,ア,ア
EOS""",

      "annotated" -> """* 0 -1D 0/0 0.000000
あ	フィラー,*,*,*,*,*,あ,ア,ア
EOS""",

      "ipa3words" -> """* 0 2D 0/1 -2.452581
太郎	名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー
は	助詞,係助詞,*,*,*,*,は,ハ,ワ
* 1 2D 0/1 -2.452581
京都	名詞,固有名詞,地域,一般,*,*,京都,キョウト,キョート
に	助詞,格助詞,一般,*,*,*,に,ニ,ニ
* 2 -1D 0/1 0.000000
行っ	動詞,自立,*,*,五段・カ行促音便,連用タ接続,行く,イッ,イッ
た	助動詞,*,*,*,特殊・タ,基本形,た,タ,タ
EOS"""
    )
  }
}
