package enju.pipeline

import java.util.Properties
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class CabochaAnnotatorTest extends FunSuite {
  test("extract tokens and chunks (no dependency)") {
    val input = <sentence><chunk id="0" link="-1" rel="D" score="0.000000" head="0" func="0"><tok id="0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></chunk></sentence>

    // <sentence id="s0">
    //   <tokens>
    //     <tok id="s0_t0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok>
    //   </tokens>
    //   <chunks>
    //     <chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/>
    //   </chunks>
    // </sentence>

    val expected = <sentence id="s0"><tokens><tok id="s0_t0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></tokens><chunks><chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/></chunks></sentence>

    // <sentence id="s0">
    //   <tokens>
    //     <tok id="s0_t0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok>
    //   </tokens>
    //   <chunks>
    //     <chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/>
    //   </chunks>
    // </sentence>


    val cabocha = new CabochaAnnotator("cabocha", new Properties)

    cabocha.transXml(input, "s0") should be(expected)
  }

  test("extract tokens") {
    val input = <sentence><chunk id="0" link="-1" rel="D" score="0.000000" head="0" func="0"><tok id="0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></chunk></sentence>

    // <sentence id="s0">
    //   <tokens>
    //     <tok id="s0_t0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok>
    //   </tokens>
    //   <chunks>
    //     <chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/>
    //   </chunks>
    // </sentence>

    val expected = <tokens><tok id="s0_t0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></tokens>

    val cabocha = new CabochaAnnotator("cabocha", new Properties)
    cabocha.getTokens(input, "s0") should be(expected)
  }

  test("extract chunks") {
    val input = <sentence><chunk id="0" link="-1" rel="D" score="0.000000" head="0" func="0"><tok id="0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok></chunk></sentence>

    // <sentence id="s0">
    //   <tokens>
    //     <tok id="s0_t0" feature="名詞,一般,*,*,*,*,日本語,ニホンゴ,ニホンゴ">日本語</tok>
    //   </tokens>
    //   <chunks>
    //     <chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/>
    //   </chunks>
    // </sentence>

    val expected = <chunks><chunk id="s0_c0" tokens="s0_t0" head="s0_t0" func="s0_t0"/></chunks>



    val cabocha = new CabochaAnnotator("cabocha", new Properties)
    cabocha.getChunks(input, "s0") should be(expected)
  }


  test("extract dependencies") {
    val input = <sentence><chunk id="0" link="1" rel="D" score="0.000000" head="0" func="1"><tok id="0" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok><tok id="1" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok></chunk><chunk id="1" link="-1" rel="D" score="0.000000" head="2" func="2"><tok id="2" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok></chunk></sentence>

    //<sentence>
    //  <chunk id="0" link="1" rel="D" score="0.000000" head="0" func="1">
    //    <tok id="0" feature="名詞,固有名詞,人名,名,*,*,太郎,タロウ,タロー">太郎</tok>
    //    <tok id="1" feature="助詞,係助詞,*,*,*,*,は,ハ,ワ">は</tok>
    //  </chunk>
    //  <chunk id="1" link="-1" rel="D" score="0.000000" head="2" func="2">
    //    <tok id="2" feature="動詞,自立,*,*,五段・カ行イ音便,基本形,歩く,アルク,アルク">歩く</tok>
    //  </chunk>
    //</sentence>


    val expected = Some(<dependencies><dependency id="s0_d0" head="s0_c1" dependent="s0_c0" label="D"/></dependencies>)

    val cabocha = new CabochaAnnotator("cabocha", new Properties)
    cabocha.getDependencies(input, "s0") should be(expected)
  }

}
