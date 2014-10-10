package enju.pipeline

import java.util.Properties

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class RegexSentenceAnnotatorTest extends FunSuite {
  test("split sentence by new line") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "newLine")
    val ssplit = new RegexSentenceAnnotator("ssplit", properties)
    val text = Stream("太郎はリンゴを食べる。\n次郎は学校に行く。")
    val sentences = ssplit.annotate(text) \ "sentence"
    sentences.length should be(2)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
  }

  test("split text containing multiple new lines") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "newLine")
    val ssplit = new RegexSentenceAnnotator("ssplit", properties)
    val text = Stream("\n\n  太郎はリンゴを食べる。\n   \n\n    次郎は学校に行く。  \n  \n    ")
    val sentences = ssplit.annotate(text) \ "sentence"
    sentences.length should be(2)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
  }

  test("split text containing multiple sentences per line") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "pointAndNewLine")
    val ssplit = new RegexSentenceAnnotator("ssplit", properties)
    val text = Stream("太郎はリンゴを食べる。次郎は学校に行く。三郎は東京へ帰る。")
    val sentences = ssplit.annotate(text) \ "sentence"
    sentences.length should be(3)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
    sentences(2).text should be("三郎は東京へ帰る。")
  }

  test("split text containing multiple sentences by point and new line") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "pointAndNewLine")
    val ssplit = new RegexSentenceAnnotator("ssplit", properties)
    val text = Stream("太郎はリンゴを食べる。\n\n次郎は学校に行く。三郎は東京へ帰る。\n")
    val sentences = ssplit.annotate(text) \ "sentence"
    sentences.length should be(3)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
    sentences(2).text should be("三郎は東京へ帰る。")
  }

  test("split text by point only") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "point")
    val ssplit = new RegexSentenceAnnotator("ssplit", properties)
    val text = Stream("太郎はリンゴを食べる。\n\n次郎は学校に行く\n三郎は東京へ帰る。\n")
    val sentences = ssplit.annotate(text) \ "sentence"
    sentences.length should be(2)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く\n三郎は東京へ帰る。")
  }

  test("split text by custom pattern") {
    val properties = new Properties
    properties.setProperty("ssplit.pattern", """\.\s+""")
    val ssplit = new RegexSentenceAnnotator("ssplit", properties)
    val text = Stream("Taro eats an apple.  Jiro goes to a school. ")
    val sentences = ssplit.annotate(text) \ "sentence"
    sentences.length should be(2)
    sentences(0).text should be("Taro eats an apple.")
    sentences(1).text should be("Jiro goes to a school.")
  }
}

