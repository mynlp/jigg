package jigg.pipeline

/*
 Copyright 2013-2017 Hiroshi Noji

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

class MecabAnnotatorSpec extends BaseAnnotatorSpec {

  def stubCom(output: String) = new StubExternalCommunicator(output)
  def mapCom(responces: Map[String, String]) = new MapStubExternalCommunicator(responces)

  def newIPA(mkCom: ()=>IOCommunicator, threads: Int = 1, p: Properties = new Properties) =
    new IPAMecabAnnotator("mecab", p) {
      override def mkLocalAnnotator = new IPALocalMecabAnnotator {
        override def mkCommunicator = mkCom()
      }
      override def nThreads = threads
    }

  "Annotator with nThreads=1" should "be able to annotate one sentence" in {
    val s = "a"
    val in = <root><document><sentences><sentence id="s0">a</sentence></sentences></document></root>
    val out = """a	名詞,固有名詞,組織,*,*,*,*
EOS"""
    val annotator = newIPA(()=>stubCom(out), threads=1)
    val result = annotator.annotate(in)
    val tokens = result \\ "token"
    tokens.size should be(1)
    (tokens(0) \@ "pos") should be("名詞")

    result \\ "tokens" \@ "annotators" should be("mecab")
  }

  "Annotator with nThreads=2" should "annotate in parallel" in {
    val responces = Map(
      "a" -> """a	名詞,固有名詞,*,*,*,*,*
EOS""",
      "b" -> """b	動詞,*,*,*,*,*,*
EOS""",
      "c" -> """c	形容詞,*,*,*,*,*,*
EOS"""
    )
    val in = <root>
    <document>
    <sentences>
    <sentence id="s0">a</sentence>
    <sentence id="s1">b</sentence>
    <sentence id="s2">c</sentence>
    </sentences>
    </document>
    </root>

    val annotator = newIPA(()=>mapCom(responces), threads=2)
    val result = annotator.annotate(in)

    val sentences = result \\ "sentence"
    sentences.size should be(3)
    ((sentences(0) \\ "token")(0) \@ "form") should be("a")
    ((sentences(1) \\ "token")(0) \@ "form") should be("b")
    ((sentences(2) \\ "token")(0) \@ "form") should be("c")
  }

}
