package jigg.pipeline

/*
 Copyright 2013-2015 Hiroshi Noji

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
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import scala.xml._

class JumanAnnotatorTest extends FunSuite {
  def newJuman(p: Properties = new Properties) = try Some(new JumanAnnotator("juman", new Properties))
  catch { case e: Throwable => None }

  test("makeTokenAltChild 1") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <tokenAlt/>))
    val expected = NodeSeq.fromSeq(Seq(<token><tokenAlt/></token>))

    newJuman() foreach { juman =>
      juman.makeTokenAltChild(input) should be(expected)
    }
  }

  test("makeTokenAltChild 2") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <tokenAlt/>, <token/>))
    val expected = NodeSeq.fromSeq(Seq(<token><tokenAlt/></token>, <token/>))

    newJuman() foreach { juman =>
      juman.makeTokenAltChild(input) should be(expected)
    }
  }

  test("makeTokenAltChild 3") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <tokenAlt/>, <tokenAlt/>, <token/>, <tokenAlt/>))
    val expected = NodeSeq.fromSeq(Seq(<token><tokenAlt/><tokenAlt/></token>, <token><tokenAlt/></token>))

    newJuman() foreach { juman =>
      juman.makeTokenAltChild(input) should be(expected)
    }
  }
}
