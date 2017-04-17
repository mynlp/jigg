package jigg.pipeline

/*
 Copyright 2013-2015 Hiroshi Noji
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licencses/LICENSE-2.0
     
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitation under the License.
*/

import java.util.Properties

import org.scalatest.FunSuite
import org.scalatest.Matchers._

import scala.xml.{Elem, NodeSeq}

class SsplitKerasAnnotatorTest extends FunSuite {

  def rootNode(text: String): Elem = <root><document>{ text }</document></root>

  def segment(text: String, properties: Properties): NodeSeq = {
    val ssplit = new SsplitKerasAnnotator("ssplitKeras", properties)
    val root = rootNode(text)
    ssplit.annotate(root) \ "document" \ "sentences" \ "sentence"
  }

  def findPath(localPath: String): String = getClass.getClassLoader.getResource(localPath).getPath

  val properties = new Properties
  properties.setProperty("ssplitKeras.model", findPath("./data/keras/ssplit_model.h5"))
  properties.setProperty("ssplitKeras.table", findPath("data/keras/jpnLookupCharacter.json"))

  test("split sentence by new line") {
    val sentences = segment("梅が咲いた。\n桜も咲いた。", properties)

    sentences.length should be (2)
    sentences(0).text should be ("梅が咲いた。")
    sentences(1).text should be ("桜も咲いた。")
  }

  test("split text containing multiple new lines") {
    val sentences = segment("梅が咲いた。\n\n\n桜も咲いた。\n\n", properties)

    sentences.length should be (2)
    sentences(0).text should be ("梅が咲いた。")
    sentences(1).text should be ("桜も咲いた。")
  }

  test("split sentence by bracket") {
    val sentences = segment("「梅が咲いた。」「桜も咲いた。」「ウグイスが鳴いた。」", properties)

    sentences.length should be (3)
    sentences(0).text should be ("「梅が咲いた。」")
    sentences(1).text should be ("「桜も咲いた。」")
    sentences(2).text should be ("「ウグイスが鳴いた。」")
  }

  test("split sentence by point") {
    val sentences = segment("梅が咲いた。桜も咲いた。ウグイスが鳴いた。", properties)

    sentences.length should be (3)
    sentences(0).text should be ("梅が咲いた。")
    sentences(1).text should be ("桜も咲いた。")
    sentences(2).text should be ("ウグイスが鳴いた。")
  }

  test("split text containing multiple new lines and space") {
    val sentences = segment("\n\n  梅が咲いた。 \n\n \n  桜も咲いた。ウグイスが鳴いた。\n\n  ", properties)

    sentences.length should be (3)
    sentences(0).text should be ("梅が咲いた。")
    sentences(1).text should be ("桜も咲いた。")
    sentences(2).text should be ("ウグイスが鳴いた。")
  }

  test("character offset value can recover the original text"){
    val text = "\n\n  梅が咲いた。 \n\n \n  桜も咲いた。ウグイスが鳴いた。\n\n  "
    val sentences = segment(text, properties)

    for (s <- sentences){
      s.text should be (
        text.substring(
          (s \@ "characterOffsetBegin").toInt,
          (s \@ "characterOffsetEnd").toInt)
      )
    }
  }

  test("split text containing `Hankaku` and `Zenkaku` character"){
    val text =
      "半角文字abcdを含んでいる。" +
      "半角文字1234を含んでいる。" +
      "全角文字１２３４を含んでいる。"
    val sentences = segment(text, properties)

    sentences.length should be (3)
    sentences(0).text should be ("半角文字abcdを含んでいる。")
    sentences(1).text should be ("半角文字1234を含んでいる。")
    sentences(2).text should be ("全角文字１２３４を含んでいる。")
  }

  test("split text containing unknown character"){
    val text =
      "αを含んでいる。" +
      "βを含んでいる。"
    val sentences = segment(text, properties)

    sentences.length should be (2)
    sentences(0).text should be ("αを含んでいる。")
    sentences(1).text should be ("βを含んでいる。")
  }
}
