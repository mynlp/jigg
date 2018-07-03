package jigg.util

/*
 Copyright 2013-2018 Hiroshi Noji

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

import org.scalatest._
import scala.xml._

import _root_.jigg.pipeline.Annotation
import _root_.jigg.pipeline.BaseAnnotatorSpec

class TreesUtilSpec extends BaseAnnotatorSpec {

  "streeToNode" should "convert a s-tree string to a Node object" in {
    val ex1 = "(NP (NN a))"

    val sent1 = <sentence><tokens>
    <token form="a" pos="NN" id="t1"/>
    </tokens></sentence>

    Annotation.ParseSpan.idGen.reset()

    val expected1 = <parse root={"sp0"} annotators={"x"}>
    <span id={"sp0"} symbol={"NP"} children={"t1"}/>
    </parse>

    TreesUtil.streeToNode(ex1, sent1, "x") should equal(expected1) (decided by sameElem)


    Annotation.ParseSpan.idGen.reset()

    val ex2 = "(S (NP (DT This)) (VP (VBN is) (NP (DT a) (NN cat))))"
    val sent2 = <sentence><tokens>
    <token form="This" pos="NN" id="t1"/>
    <token form="is" pos="VBN" id="t2"/>
    <token form="a" pos="DT" id="t3"/>
    <token form="cat" pos="NN" id="t4"/>
    </tokens></sentence>

    val expected2 = <parse root={"sp3"} annotators={"x"}>
    <span id="sp0" symbol="NP" children="t1"/>
    <span id="sp1" symbol="NP" children="t3 t4"/>
    <span id="sp2" symbol="VP" children="t2 sp1"/>
    <span id="sp3" symbol="S" children="sp0 sp2"/>
    </parse>

    TreesUtil.streeToNode(ex2, sent2, "x") should equal(expected2) (decided by sameElem)
  }

  it should "skip successive parenteses" in {
    val ex1 = "(NP (((NN a))))"

    val sent1 = <sentence><tokens>
    <token form="a" pos="NN" id="t1"/>
    </tokens></sentence>

    Annotation.ParseSpan.idGen.reset()

    val expected1 = <parse root={"sp0"} annotators={"x"}>
    <span id={"sp0"} symbol={"NP"} children={"t1"}/>
    </parse>

    TreesUtil.streeToNode(ex1, sent1, "x") should equal(expected1) (decided by sameElem)
  }

  it should "ignore newlines in a tree" in {
    Annotation.ParseSpan.idGen.reset()

    val ex2 = """(S
  (NP (DT This))
  (VP (VBN is) (NP (DT a) (NN cat))))"""
    val sent2 = <sentence><tokens>
    <token form="This" pos="NN" id="t1"/>
    <token form="is" pos="VBN" id="t2"/>
    <token form="a" pos="DT" id="t3"/>
    <token form="cat" pos="NN" id="t4"/>
    </tokens></sentence>

    val expected2 = <parse root={"sp3"} annotators={"x"}>
    <span id="sp0" symbol="NP" children="t1"/>
    <span id="sp1" symbol="NP" children="t3 t4"/>
    <span id="sp2" symbol="VP" children="t2 sp1"/>
    <span id="sp3" symbol="S" children="sp0 sp2"/>
    </parse>

    TreesUtil.streeToNode(ex2, sent2, "x") should equal(expected2) (decided by sameElem)
  }

  it should "recognize more than two children" in {
    Annotation.ParseSpan.idGen.reset()

    val ex = """(S (NP (PRP He)) (VP (VBD ate) (NN pizza)) (. .))"""

    val sent = <sentence id="s1" characterOffsetBegin="0" characterOffsetEnd="14">
    <tokens annotators="corenlp">
    <token characterOffsetEnd="2" characterOffsetBegin="0" id="t4" form="He" pos="PRP"/>
    <token characterOffsetEnd="6" characterOffsetBegin="3" id="t5" form="ate" pos="VBD"/>
    <token characterOffsetEnd="12" characterOffsetBegin="7" id="t6" form="pizza" pos="NN"/>
    <token characterOffsetEnd="14" characterOffsetBegin="13" id="t7" form="." pos="."/>
    </tokens>
    </sentence>

    val expected = <parse annotators="x" root="sp2">
      <span id="sp0" symbol="NP" children="t4"/>
      <span id="sp1" symbol="VP" children="t5 t6"/>
      <span id="sp2" symbol="S" children="sp0 sp1 t7"/>
    </parse>

    TreesUtil.streeToNode(ex, sent, "x") should equal(expected) (decided by sameElem)
  }
}
