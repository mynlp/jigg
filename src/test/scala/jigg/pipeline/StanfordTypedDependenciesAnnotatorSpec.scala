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

import org.scalatest._
import scala.xml._

class StanfordTypedDependenciesAnnotatorSpec extends BaseAnnotatorSpec {

  val annotator = new StanfordTypedDependenciesAnnotator("typeddep", new Properties)

  val input = <root>
  <document id="d0">
    <sentences>
      <sentence id="s0" characterOffsetBegin="0" characterOffsetEnd="14">
        This is a cat.
        <tokens annotators="corenlp">
          <token pos="DT" characterOffsetEnd="4" characterOffsetBegin="0" id="t0" form="This"/>
          <token pos="VBZ" characterOffsetEnd="7" characterOffsetBegin="5" id="t1" form="is"/>
          <token pos="DT" characterOffsetEnd="9" characterOffsetBegin="8" id="t2" form="a"/>
          <token pos="NN" characterOffsetEnd="13" characterOffsetBegin="10" id="t3" form="cat"/>
          <token pos="." characterOffsetEnd="14" characterOffsetBegin="13" id="t4" form="."/>
        </tokens>
        <parse root="sp3" annotators="benepar">
          <span id="sp0" symbol="NP" children="t0"/>
          <span id="sp1" symbol="NP" children="t2 t3"/>
          <span id="sp2" symbol="VP" children="t1 sp1"/>
          <span id="sp3" symbol="S" children="sp0 sp2 t4"/>
        </parse>
      </sentence>
    </sentences>
  </document>
</root>

  "annotator" should "add stanford typed dependencies" in {
    val ann = annotator.annotate(input)

    val deps = ann \\ "dependencies"

    deps.filter(_ \@ "type" == "basic").head should equal(
      <dependencies type="basic" annotators="typeddep">
        <dependency id="dep0" head="ROOT" dependent="t3" deprel="root"/>
        <dependency id="dep1" head="t3" dependent="t0" deprel="nsubj"/>
        <dependency id="dep2" head="t3" dependent="t1" deprel="cop"/>
        <dependency id="dep3" head="t3" dependent="t2" deprel="det"/>
        <dependency id="dep4" head="t3" dependent="t4" deprel="punct"/>
      </dependencies>) (decided by sameElem)

    deps.filter(_ \@ "type" == "collapsed").head should equal(
      <dependencies type="collapsed" annotators="typeddep">
        <dependency id="dep5" head="ROOT" dependent="t3" deprel="root"/>
        <dependency id="dep6" head="t3" dependent="t0" deprel="nsubj"/>
        <dependency id="dep7" head="t3" dependent="t1" deprel="cop"/>
        <dependency id="dep8" head="t3" dependent="t2" deprel="det"/>
        <dependency id="dep9" head="t3" dependent="t4" deprel="punct"/>
      </dependencies>) (decided by sameElem)

    deps.filter(_ \@ "type" == "collapsed-ccprocessed").head should equal(
      <dependencies type="collapsed-ccprocessed" annotators="typeddep">
        <dependency id="dep10" head="ROOT" dependent="t3" deprel="root"/>
        <dependency id="dep11" head="t3" dependent="t0" deprel="nsubj"/>
        <dependency id="dep12" head="t3" dependent="t1" deprel="cop"/>
        <dependency id="dep13" head="t3" dependent="t2" deprel="det"/>
        <dependency id="dep14" head="t3" dependent="t4" deprel="punct"/>
      </dependencies>) (decided by sameElem)

    deps.filter(_ \@ "type" == "enhanced").head should equal(
      <dependencies type="enhanced" annotators="typeddep">
        <dependency id="dep15" head="ROOT" dependent="t3" deprel="root"/>
        <dependency id="dep16" head="t3" dependent="t0" deprel="nsubj"/>
        <dependency id="dep17" head="t3" dependent="t1" deprel="cop"/>
        <dependency id="dep18" head="t3" dependent="t2" deprel="det"/>
        <dependency id="dep19" head="t3" dependent="t4" deprel="punct"/>
      </dependencies>) (decided by sameElem)

    deps.filter(_ \@ "type" == "enhanced-plus-plus").head should equal(
      <dependencies type="enhanced-plus-plus" annotators="typeddep">
        <dependency id="dep20" head="ROOT" dependent="t3" deprel="root"/>
        <dependency id="dep21" head="t3" dependent="t0" deprel="nsubj"/>
        <dependency id="dep22" head="t3" dependent="t1" deprel="cop"/>
        <dependency id="dep23" head="t3" dependent="t2" deprel="det"/>
        <dependency id="dep24" head="t3" dependent="t4" deprel="punct"/>
      </dependencies>) (decided by sameElem)
  }
}
