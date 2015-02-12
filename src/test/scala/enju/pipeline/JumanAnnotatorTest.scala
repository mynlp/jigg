package enju.pipeline

import java.util.Properties
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import scala.xml._

class JumanAnnotatorTest extends FunSuite {
  test("makeTokenAltChild 1") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <token_alt/>))
    val expected = NodeSeq.fromSeq(Seq(<token><token_alt/></token>))

    val juman = new JumanAnnotator("juman", new Properties)

    juman.makeTokenAltChild(input) should be(expected)
  }

  test("makeTokenAltChild 2") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <token_alt/>, <token/>))
    val expected = NodeSeq.fromSeq(Seq(<token><token_alt/></token>, <token/>))

    val juman = new JumanAnnotator("juman", new Properties)

    juman.makeTokenAltChild(input) should be(expected)
  }

  test("makeTokenAltChild 3") {
    val input = NodeSeq.fromSeq(Seq(<token/>, <token_alt/>, <token_alt/>, <token/>, <token_alt/>))
    val expected = NodeSeq.fromSeq(Seq(<token><token_alt/><token_alt/></token>, <token><token_alt/></token>))

    val juman = new JumanAnnotator("juman", new Properties)

    juman.makeTokenAltChild(input) should be(expected)
  }
}
