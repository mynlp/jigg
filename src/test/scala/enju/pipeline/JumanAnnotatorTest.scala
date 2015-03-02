package enju.pipeline

import java.util.Properties
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import scala.xml._

class JumanAnnotatorTest extends FunSuite {
  test("makeTokenAltChild 1") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <tokenAlt/>))
    val expected = NodeSeq.fromSeq(Seq(<token><tokenAlt/></token>))

    val juman = new JumanAnnotator("juman", new Properties)

    juman.makeTokenAltChild(input) should be(expected)
  }

  test("makeTokenAltChild 2") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <tokenAlt/>, <token/>))
    val expected = NodeSeq.fromSeq(Seq(<token><tokenAlt/></token>, <token/>))

    val juman = new JumanAnnotator("juman", new Properties)

    juman.makeTokenAltChild(input) should be(expected)
  }

  test("makeTokenAltChild 3") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <tokenAlt/>, <tokenAlt/>, <token/>, <tokenAlt/>))
    val expected = NodeSeq.fromSeq(Seq(<token><tokenAlt/><tokenAlt/></token>, <token><tokenAlt/></token>))

    val juman = new JumanAnnotator("juman", new Properties)

    juman.makeTokenAltChild(input) should be(expected)
  }
}
