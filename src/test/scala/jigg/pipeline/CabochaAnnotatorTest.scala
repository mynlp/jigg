package jigg.pipeline

/*
 Copyright 2013-2015 Hiroshi Noji

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

class CabochaAnnotatorTest extends FunSuite {

  def newCabocha(p: Properties = new Properties) = try Some(new IPACabochaAnnotator("cabocha -P IPA", new Properties))
  catch { case e: Throwable => None }

  test("extract tokens and chunks (no dependency)") {
    val input = <sentence><chunk id="0" link="-1" rel="D" score="0.000000" head="0" func="0"><tok id="0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></chunk></sentence>

    /*
     <sentence id="s0">
     <tokens>
     <tok id="s0_t0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok>
     </tokens>
     <chunks>
     <chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/>
     </chunks>
     </sentence>
     */

    val expected_tokens = <tokens><tok id="s0_tok0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></tokens>
    val expected_chunks = <chunks><chunk id="s0_chu0" tokens="s0_tok0" head="s0_tok0" func="s0_tok0"/></chunks>

    newCabocha() foreach { cabocha =>
      cabocha.getTokens(input, "s0") should be(expected_tokens)
      cabocha.getChunks(input, "s0") should be(expected_chunks)
      cabocha.getDependencies(input, "s0") should be(None)
    }
  }


  test("extract tokens, chunks, and dependencies") {
    val input = <sentence><chunk id="0" link="1" rel="D" score="0.000000" head="0" func="1"><tok id="0" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok><tok id="1" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok></chunk><chunk id="1" link="-1" rel="D" score="0.000000" head="2" func="2"><tok id="2" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok></chunk></sentence>

    /*
     <sentence>
     <chunk id="0" link="1" rel="D" score="0.000000" head="0" func="1">
     <tok id="0" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok>
     <tok id="1" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok>
     </chunk>
     <chunk id="1" link="-1" rel="D" score="0.000000" head="2" func="2">
     <tok id="2" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok>
     </chunk>
     </sentence>
     */

    val expected_tokens = <tokens><tok id="s0_tok0" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok><tok id="s0_tok1" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok><tok id="s0_tok2" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok></tokens>
    val expected_chunks = <chunks><chunk id="s0_chu0" tokens="s0_tok0 s0_tok1" head="s0_tok0" func="s0_tok1"/><chunk id="s0_chu1" tokens="s0_tok2" head="s0_tok2" func="s0_tok2"/></chunks>
    val expected_dependencies = Some(<dependencies><dependency id="s0_dep0" head="s0_chu1" dependent="s0_chu0" label="D"/></dependencies>)

    newCabocha() foreach { cabocha =>
      cabocha.getTokens(input, "s0") should be(expected_tokens)
      cabocha.getChunks(input, "s0") should be(expected_chunks)
      cabocha.getDependencies(input, "s0") should be(expected_dependencies)
    }

  }


  test("complex sentence") {
    val input = <sentence><chunk id="0" link="1" rel="D" score="1.693012" head="0" func="1"><tok id="0" feature="名詞,形容動詞語幹,*,*,*,*,健康,ケンコウ,ケンコー">健康</tok><tok id="1" feature="助動詞,*,*,*,特殊・ダ,体言接続,だ,ナ,ナ">な</tok></chunk><chunk id="1" link="3" rel="D" score="-2.521630" head="2" func="3"><tok id="2" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok><tok id="3" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok></chunk><chunk id="2" link="3" rel="D" score="-2.521630" head="4" func="4"><tok id="4" feature="名詞,副詞可能,*,*,*,*,毎日,マイニチ,マイニチ">毎日</tok></chunk><chunk id="3" link="-1" rel="D" score="0.000000" head="5" func="5"><tok id="5" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok></chunk></sentence>

    /*
     <sentence>
     <chunk id="0" link="1" rel="D" score="1.693012" head="0" func="1">
     <tok id="0" feature="名詞,形容動詞語幹,*,*,*,*,健康,ケンコウ,ケンコー">健康</tok>
     <tok id="1" feature="助動詞,*,*,*,特殊・ダ,体言接続,だ,ナ,ナ">な</tok>
     </chunk>
     <chunk id="1" link="3" rel="D" score="-2.521630" head="2" func="3">
     <tok id="2" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok>
     <tok id="3" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok>
     </chunk>
     <chunk id="2" link="3" rel="D" score="-2.521630" head="4" func="4">
     <tok id="4" feature="名詞,副詞可能,*,*,*,*,毎日,マイニチ,マイニチ">毎日</tok>
     </chunk>
     <chunk id="3" link="-1" rel="D" score="0.000000" head="5" func="5">
     <tok id="5" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok>
     </chunk>
     </sentence>
     */

    val expected_tokens = <tokens><tok id="s0_tok0" feature="名詞,形容動詞語幹,*,*,*,*,健康,ケンコウ,ケンコー">健康</tok><tok id="s0_tok1" feature="助動詞,*,*,*,特殊・ダ,体言接続,だ,ナ,ナ">な</tok><tok id="s0_tok2" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok><tok id="s0_tok3" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok><tok id="s0_tok4" feature="名詞,副詞可能,*,*,*,*,毎日,マイニチ,マイニチ">毎日</tok><tok id="s0_tok5" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok></tokens>
    val expected_chunks = <chunks><chunk id="s0_chu0" tokens="s0_tok0 s0_tok1" head="s0_tok0" func="s0_tok1" /><chunk id="s0_chu1" tokens="s0_tok2 s0_tok3" head="s0_tok2" func="s0_tok3"/><chunk id="s0_chu2" tokens="s0_tok4" head="s0_tok4" func="s0_tok4"/><chunk id="s0_chu3" tokens="s0_tok5" head="s0_tok5" func="s0_tok5"/></chunks>
    val expected_dependencies = Some(<dependencies><dependency id="s0_dep0" head="s0_chu1" dependent="s0_chu0" label="D" /><dependency id="s0_dep1" head="s0_chu3" dependent="s0_chu1" label="D" /><dependency id="s0_dep2" head="s0_chu3" dependent="s0_chu2" label="D" /></dependencies>)

    newCabocha() foreach { cabocha =>
      cabocha.getTokens(input, "s0") should be(expected_tokens)
      cabocha.getChunks(input, "s0") should be(expected_chunks)
      cabocha.getDependencies(input, "s0") should be(expected_dependencies)
    }
  }



}
