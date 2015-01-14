package enju.pipeline

import java.util.Properties
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class CabochaAnnotatorTest extends FunSuite {
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

    val expected_tokens = <tokens><tok id="s0_0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></tokens>
    val expected_chunks = <chunks><chunk id="s0_0" tokens="s0_0" head="s0_0" func="s0_0"/></chunks>

    val cabocha = new CabochaAnnotator("cabocha", new Properties)

    cabocha.getTokens(input, "s0") should be(expected_tokens)
    cabocha.getChunks(input, "s0") should be(expected_chunks)
    cabocha.getDependencies(input, "s0") should be(None)
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

    val expected_tokens = <tokens><tok id="s0_0" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok><tok id="s0_1" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok><tok id="s0_2" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok></tokens>
    val expected_chunks = <chunks><chunk id="s0_0" tokens="s0_0 s0_1" head="s0_0" func="s0_1"/><chunk id="s0_1" tokens="s0_2" head="s0_2" func="s0_2"/></chunks>
    val expected_dependencies = Some(<dependencies><dependency id="s0_0" head="s0_1" dependent="s0_0" label="D"/></dependencies>)

    val cabocha = new CabochaAnnotator("cabocha", new Properties)

    cabocha.getTokens(input, "s0") should be(expected_tokens)
    cabocha.getChunks(input, "s0") should be(expected_chunks)
    cabocha.getDependencies(input, "s0") should be(expected_dependencies)

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

    val expected_tokens = <tokens><tok id="s0_0" feature="名詞,形容動詞語幹,*,*,*,*,健康,ケンコウ,ケンコー">健康</tok><tok id="s0_1" feature="助動詞,*,*,*,特殊・ダ,体言接続,だ,ナ,ナ">な</tok><tok id="s0_2" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok><tok id="s0_3" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok><tok id="s0_4" feature="名詞,副詞可能,*,*,*,*,毎日,マイニチ,マイニチ">毎日</tok><tok id="s0_5" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok></tokens>
    val expected_chunks = <chunks><chunk id="s0_0" tokens="s0_0 s0_1" head="s0_0" func="s0_1" /><chunk id="s0_1" tokens="s0_2 s0_3" head="s0_2" func="s0_3"/><chunk id="s0_2" tokens="s0_4" head="s0_4" func="s0_4"/><chunk id="s0_3" tokens="s0_5" head="s0_5" func="s0_5"/></chunks>
    val expected_dependencies = Some(<dependencies><dependency id="s0_0" head="s0_1" dependent="s0_0" label="D" /><dependency id="s0_1" head="s0_3" dependent="s0_1" label="D" /><dependency id="s0_2" head="s0_3" dependent="s0_2" label="D" /></dependencies>)

    val cabocha = new CabochaAnnotator("cabocha", new Properties)

    cabocha.getTokens(input, "s0") should be(expected_tokens)
    cabocha.getChunks(input, "s0") should be(expected_chunks)
    cabocha.getDependencies(input, "s0") should be(expected_dependencies)
  }



}
