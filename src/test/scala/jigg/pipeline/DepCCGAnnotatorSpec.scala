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

class DepCCGAnnotatorSpec extends BaseAnnotatorSpec {

  val dummyP = new Properties
  dummyP.setProperty("depccg.srcdir", "b")
  dummyP.setProperty("depccg.model", "a")

  class AnnotatorStub(output: String)
      extends DepCCGAnnotator("depccg", dummyP) {

    override def checkArgument() = {} // do not check

    override def mkLocalAnnotator = new LocalDepCCGAnnotator {
      override def mkCommunicator = new StubExternalCommunicator(output)
      // override def runDepccg(input: String) = output.split("\n").toStream
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

    val output ="""<candc>
<ccgs>
<ccg>
<rule cat="S[dcl]" type="rp">
<rule cat="S[dcl]" type="ba">
<lf cat="NP" entity="X" chunk="XX" pos="X" lemma="XX" word="He" span="1" start="0"/>
<rule cat="S[dcl]\NP" type="fa">
<lf cat="(S[dcl]\NP)/NP" entity="X" chunk="XX" pos="X" lemma="XX" word="ate" span="1" start="1"/>
<rule cat="NP" type="lex">
<lf cat="N" entity="X" chunk="XX" pos="X" lemma="XX" word="pizza" span="1" start="2"/>
</rule>
</rule>
</rule>
<lf cat="." entity="X" chunk="XX" pos="X" lemma="XX" word="." span="1" start="3"/>
</rule>

</ccg>
</ccgs>
</candc>
END
"""

    val ann = new AnnotatorStub(output)

    val annotation = ann.annotate(doc)

    val s = annotation \\ "sentence"
    (s \ "ccg").head should equal(
    <ccg annotators="depccg" root="ccgsp0" id="ccg0">
      <span id="ccgsp0" begin="0" end="4" symbol="S[dcl]" rule="rp" children="ccgsp1 ccgsp7"/>
      <span id="ccgsp1" begin="0" end="3" symbol="S[dcl]" rule="ba" children="ccgsp2 ccgsp3"/>
      <span id="ccgsp2" begin="0" end="1" symbol="NP" children="t4"/>
      <span id="ccgsp3" begin="1" end="3" symbol="S[dcl]\NP" rule="fa" children="ccgsp4 ccgsp5"/>
      <span id="ccgsp4" begin="1" end="2" symbol="(S[dcl]\NP)/NP" children="t5"/>
      <span id="ccgsp5" begin="2" end="3" symbol="NP" rule="lex" children="ccgsp6"/>
      <span id="ccgsp6" begin="2" end="3" symbol="N" children="t6"/>
      <span id="ccgsp7" begin="3" end="4" symbol="." children="t7"/>
    </ccg>) (decided by sameElem)
  }

  it should "handle two sentences" in {
    val doc =
      <document id="d1">
        <sentences>
          <sentence id="s1" characterOffsetBegin="0" characterOffsetEnd="14">
            A
            <tokens annotators="corenlp">
              <token characterOffsetEnd="1" characterOffsetBegin="0" id="t0" form="A"/>
            </tokens>
          </sentence>
          <sentence id="s1" characterOffsetBegin="0" characterOffsetEnd="14">
            B
            <tokens annotators="corenlp">
              <token characterOffsetEnd="1" characterOffsetBegin="0" id="t1" form="B"/>
            </tokens>
          </sentence>
        </sentences>
      </document>

    val output ="""<candc>
<ccgs><ccg>
<lf start="0" span="1" word="A" lemma="XX" pos="x" chunk="XX" entity="x" cat="NP" />

</ccg></ccgs>
<ccgs><ccg>
<lf start="0" span="1" word="B" lemma="XX" pos="x" chunk="XX" entity="x" cat="N" />

</ccg></ccgs>
</candc>
END
"""

    val ann = new AnnotatorStub(output)

    val annotation = ann.annotate(doc)

    val ccgs = annotation \\ "ccg"
    ccgs.size should equal (2)

    ccgs(0) should equal(
      <ccg annotators="depccg" root="ccgsp8" id="ccg1"><span id="ccgsp8" begin="0" end="1" symbol="NP" children="t0"/></ccg>
    )

    ccgs(1) should equal(
      <ccg annotators="depccg" root="ccgsp9" id="ccg2"><span id="ccgsp9" begin="0" end="1" symbol="N" children="t1"/></ccg>
    )
  }

}
