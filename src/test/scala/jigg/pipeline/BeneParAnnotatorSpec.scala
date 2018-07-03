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

class BeneParAnnotatorSpec extends BaseAnnotatorSpec {

  class AnnotatorStub(output: String) extends BeneParAnnotator("benepar", new Properties) {
    override def mkLocalAnnotator = new LocalBeneParAnnotator {
      override def mkCommunicator = new StubExternalCommunicator(output)
    }
    assert(nThreads == 1)
  }

  Annotation.ParseSpan.idGen.reset()

  "BeneParAnnotator" should "convert a s-tree output of benepar into a node" in {
    val doc =
      <document id="d1">
        <sentences>
          <sentence id="s1" characterOffsetBegin="0" characterOffsetEnd="14">
            He ate pizza .
            <tokens annotators="corenlp">
              <token characterOffsetEnd="2" characterOffsetBegin="0" id="t4" form="He" pos="PRP"/>
              <token characterOffsetEnd="6" characterOffsetBegin="3" id="t5" form="ate" pos="VBD"/>
              <token characterOffsetEnd="12" characterOffsetBegin="7" id="t6" form="pizza" pos="NN"/>
              <token characterOffsetEnd="14" characterOffsetBegin="13" id="t7" form="." pos="."/>
            </tokens>
          </sentence>
        </sentences>
      </document>

    val output = """(S (NP (PRP He)) (VP (VBD ate) (NN pizza)) (. .))
END"""

    val ann = new AnnotatorStub(output)
    val annotation = ann.annotate(doc)

    val s = annotation \\ "sentence"

    (s \ "parse").head should equal(<parse annotators="benepar" root="sp2">
      <span id="sp0" symbol="NP" children="t4"/>
      <span id="sp1" symbol="VP" children="t5 t6"/>
      <span id="sp2" symbol="S" children="sp0 sp1 t7"/>
      </parse>) (decided by sameElem)
  }
}
