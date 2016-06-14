package jigg.pipeline

/*
 Copyright 2013-2016 Hiroshi Noji

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
import java.util.{List => JList}

import org.scalatest._
import scala.xml._

import edu.berkeley.nlp.syntax.Tree

class BerkeleyParserAnnotatorSpec extends BaseAnnotatorSpec {

  class ConstantParser(output: Tree[String]) {
    def parse(sentence: JList[String], pos: JList[String]) = output
  }

  class FromTokenAnnotatorStub(output: Tree[String]) extends
      BerkeleyParserAnnotatorFromToken("berkeleyparser", new Properties) {
    override lazy val parser = new ConstantParser(output) with Parser
  }

  class FromPOSAnnotatorStub(output: Tree[String]) extends
      BerkeleyParserAnnotatorFromPOS(
    "berkeleyparser", new Properties) {
    override lazy val parser = new ConstantParser(output) with Parser
  }

  def emptyFromTokenAnn = new FromTokenAnnotatorStub(new Tree[String]("(ROOT)"))
  def emptyFromPOSAnn = new FromPOSAnnotatorStub(new Tree[String]("ROOT"))

  "Annotator" should "output the empty parse for the empty sentence" in {
    val fromToken = emptyFromTokenAnn
    val fromPOS = emptyFromPOSAnn

    val sentence =
      <sentence id="s0">
        <tokens>
        </tokens>
      </sentence>

    val fromTokenResult = fromToken.newSentenceAnnotation(sentence)
    val fromPOSResult = fromPOS.newSentenceAnnotation(sentence)

    fromTokenResult should equal (
      <sentence id="s0">
        <tokens annotators="berkeleyparser">
        </tokens>
        <parse annotators="berkeleyparser" root=""/>
      </sentence>
    ) (decided by sameElem)

    fromPOSResult should equal (
      <sentence id="s0">
        <tokens>
        </tokens>
        <parse annotators="berkeleyparser" root=""/>
      </sentence>
    ) (decided by sameElem)
  }

  "FromTokenAnnotator" should "throws AnnotationError if the parse failed" in {
    val ann = emptyFromTokenAnn
    val sentence =
      <sentence id="s0">
        <tokens>
          <token id="t0" from="a" characterOffsetBegin="0" characterOffsetEnd="1"/>
          <token id="t0" from="b" characterOffsetBegin="2" characterOffsetEnd="3"/>
        </tokens>
      </sentence>

    a [AnnotationError] should be thrownBy {
      ann.newSentenceAnnotation(sentence)
    }
  }

  "FromPOSAnnotator" should "throws AnnotationError if the parse failed" in {
    val ann = emptyFromPOSAnn
    val sentence =
      <sentence id="s0">
        <tokens>
          <token id="t0" from="a" pos="NN" characterOffsetBegin="0" characterOffsetEnd="1"/>
          <token id="t0" from="b" pos="NN" characterOffsetBegin="2" characterOffsetEnd="3"/>
        </tokens>
      </sentence>

    a [AnnotationError] should be thrownBy {
      ann.newSentenceAnnotation(sentence)
    }
  }
}
