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

import org.scalatest._
import scala.xml._

class EasyCCGAnnotatorSpec extends BaseAnnotatorSpec {

  val dummyP = new Properties
  dummyP.setProperty("easyccg.path", "b")
  dummyP.setProperty("easyccg.model", "a")

  class AnnotatorStub(output: String)
      extends EasyCCGAnnotator("easyccg", dummyP) {

    override def mkLocalAnnotator = new LocalEasyCCGAnnotator {
      override def mkCommunicator = new StubExternalCommunicator(output)
    }
    override def nThreads = 1
  }

  Annotation.CCGSpan.idGen.reset()
  Annotation.CCG.idGen.reset()

  "Annotator" should "add a CCG annotation" in {
    val doc =
      <document id="d1">
        <sentences>
          <sentence id="s1" characterOffsetBegin="0" characterOffsetEnd="14">
            He ate pizza .
            <tokens annotators="corenlp">
              <token characterOffsetEnd="2" characterOffsetBegin="0" id="t4" form="He"/>
              <token characterOffsetEnd="6" characterOffsetBegin="3" id="t5" form="ate"/>
              <token characterOffsetEnd="12" characterOffsetBegin="7" id="t6" form="pizza"/>
              <token characterOffsetEnd="14" characterOffsetBegin="13" id="t7" form="."/>
            </tokens>
          </sentence>
        </sentences>
      </document>

    val output ="""ID=1
(<T S[dcl] ba 1 2> (<L NP He He x x O NP>) (<T S[dcl]\NP fa 0 2> (<L (S[dcl]\NP)/NP ate ate x x O (S[dcl]\NP)/NP>) (<T NP rp 0 2> (<T NP lex 0 1> (<L N pizza pizza x x O N>) ) (<L . . . x x O .>) ) ) )"""

    // (<T S[dcl] ba 1 2>
    //   (<L NP He He x x O NP>)
    //   (<T S[dcl]\NP fa 0 2>
    //     (<L (S[dcl]\NP)/NP ate ate x x O (S[dcl]\NP)/NP>)
    //     (<T NP rp 0 2>
    //       (<T NP lex 0 1>
    //         (<L N pizza pizza x x O N>) )
    //       (<L . . . x x O .>) ) ) )
    val ann = new AnnotatorStub(output)

    val annotation = ann.annotate(doc)

    val s = annotation \\ "sentence"
    (s \ "ccg").head should equal(
    <ccg annotators="easyccg" root="ccgsp0" id="ccg0">
      <span id="ccgsp0" begin="0" end="4" symbol="S[dcl]" rule="ba" children="ccgsp1 ccgsp2"/>
      <span id="ccgsp1" begin="0" end="1" symbol="NP" children="t4"/>
      <span id="ccgsp2" begin="1" end="4" symbol="S[dcl]\NP" rule="fa" children="ccgsp3 ccgsp4"/>
      <span id="ccgsp3" begin="1" end="2" symbol="(S[dcl]\NP)/NP" children="t5"/>
      <span id="ccgsp4" begin="2" end="4" symbol="NP" rule="rp" children="ccgsp5 ccgsp7"/>
      <span id="ccgsp5" begin="2" end="3" symbol="NP" rule="lex" children="ccgsp6"/>
      <span id="ccgsp6" begin="2" end="3" symbol="N" children="t6"/>
      <span id="ccgsp7" begin="3" end="4" symbol="." children="t7"/>
    </ccg>) (decided by sameElem)
  }

}
