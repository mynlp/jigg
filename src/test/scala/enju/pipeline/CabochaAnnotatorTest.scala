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

  test("extract token") {
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

}
