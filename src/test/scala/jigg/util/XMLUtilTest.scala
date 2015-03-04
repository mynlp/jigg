package jigg.util

package jigg.pipeline

import java.util.Properties

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class XMLUtilTest extends FunSuite {
  import XMLUtil._

  test("replaceAll visits all elements") {
    val xml =
      <root>
        <document>
          <sentence id={"s1"}>{ "hoge" }</sentence>
          <sentence id={"s2"}>{ "huga" }</sentence>
        </document>
      </root>

    val newXml = replaceAll(xml, "sentence") { sentence =>
      addChild(sentence, <child>{ "child" }</child>)
    }
    val sentence = newXml \ "document" \ "sentence"

    sentence.size should be (2)
    (sentence(0) \ "child").text should be ("child")
    (sentence(1) \ "child").text should be ("child")
  }
}
