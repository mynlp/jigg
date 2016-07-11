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

class SyntaxNetAnnotatorSpec extends BaseAnnotatorSpec {

  class POSAnnotatorStub(output: String) extends
      SyntaxNetPOSAnnotator("syntaxnetpos", new Properties) {

    override def run(input: String) = output.split("\n").toStream
  }

  "POSAnnotator" should "annotate all sentences across documents" in {

    val root = <root>
      <document id="0">
        <sentences><sentence><tokens><token id="t0" surf="a"/></tokens></sentence></sentences>
      </document>
      <document id="1">
        <sentences><sentence><tokens>
          <token id="t1" surf="b"/>
          <token id="t2" surf="c"/>
        </tokens></sentence></sentences>
      </document>
      <document id="2">
        <sentences><sentence><tokens><token id="t3" surf="d"/></tokens></sentence></sentences>
      </document>
    </root>

    val output = """1	a	_	A	A	_	0

1	b	_	B	B	_	0
2	c	_	C	C	_	0

1	c	_	D	D	_	0
"""

    val annotator = new POSAnnotatorStub(output)
    val annotated = annotator.annotate(root)

    annotated should equal (<root>
      <document id="0">
        <sentences><sentence><tokens annotators="syntaxnetpos">
          <token id="t0" surf="a" pos="A" cpos="A"/>
        </tokens></sentence></sentences>
      </document>
      <document id="1">
        <sentences><sentence><tokens annotators="syntaxnetpos">
          <token id="t1" surf="b" pos="B" cpos="B"/>
          <token id="t2" surf="c" pos="C" cpos="C"/>
        </tokens></sentence></sentences>
      </document>
      <document id="2">
        <sentences><sentence><tokens annotators="syntaxnetpos">
          <token id="t3" surf="d" pos="D" cpos="D"/>
        </tokens></sentence></sentences>
      </document>
    </root>) (decided by sameElem)
  }

}
