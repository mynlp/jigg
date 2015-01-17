package jp.jigg.nlp.pipeline

import java.util.Properties

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class RegexSentenceAnnotatorTest extends FunSuite {
  def rootNode(text: String) = <root><document>{ text }</document></root>

  def segment(text: String, properties: Properties) = {
    val ssplit = new RegexSentenceAnnotator("ssplit", properties)
    val root = rootNode(text)
    ssplit.annotate(root) \ "document" \ "sentences" \ "sentence"
  }

  test("split sentence by new line") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "newLine")
    val sentences = segment("太郎はリンゴを食べる。\n次郎は学校に行く。", properties)

    sentences.length should be(2)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
  }

  test("split text containing multiple new lines") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "newLine")
    val sentences= segment("\n\n  太郎はリンゴを食べる。\n   \n\n    次郎は学校に行く。  \n  \n    ", properties)
    sentences.length should be(2)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
  }

  test("split text containing multiple sentences per line") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "pointAndNewLine")
    val sentences = segment("太郎はリンゴを食べる。次郎は学校に行く。三郎は東京へ帰る。", properties)
    sentences.length should be(3)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
    sentences(2).text should be("三郎は東京へ帰る。")
  }

  test("split text containing multiple sentences by point and new line") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "pointAndNewLine")
    val sentences = segment("太郎はリンゴを食べる。\n\n次郎は学校に行く。三郎は東京へ帰る。\n", properties)
    sentences.length should be(3)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く。")
    sentences(2).text should be("三郎は東京へ帰る。")
  }

  test("split text by point only") {
    val properties = new Properties
    properties.setProperty("ssplit.method", "point")
    val sentences = segment("太郎はリンゴを食べる。\n\n次郎は学校に行く\n三郎は東京へ帰る。\n", properties)
    sentences.length should be(2)
    sentences(0).text should be("太郎はリンゴを食べる。")
    sentences(1).text should be("次郎は学校に行く\n三郎は東京へ帰る。")
  }

  test("split text by custom pattern") {
    val properties = new Properties
    properties.setProperty("ssplit.pattern", """\.\s+""")
    val sentences = segment("Taro eats an apple.  Jiro goes to a school. ", properties)
    sentences.length should be(2)
    sentences(0).text should be("Taro eats an apple.")
    sentences(1).text should be("Jiro goes to a school.")
  }
}
