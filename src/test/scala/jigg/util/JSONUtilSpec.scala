package jigg.util

package jigg.pipeline

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class JSONUtilSpec extends FunSuite{
  import JSONUtil._
  import scala.xml._
  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  val testNode =
    <root>
      <document id={"d0"}>
        Test Node
      </document>
    </root>
  val goldJSON =
    parse(
    """
      {
        ".tag" : "root",
        ".child" : [ {
          ".tag" : "document",
          "id" : "d0",
          "text" : "Test Node"
        } ]
      }
    """
    )

  /**
    * For handling a backslash.
    */
  val testNodeForBackslash =
    <root>
      <document id={"d0\\N"}>
        Test Node
      </document>
    </root>

  val goldJSONForBackSlash =
    parse(
    """{".tag":"root",".child":
      [{".tag":"document","id":"d0\\N","text":"Test Node"}
      ]
    }"""
    )

  /**
    * For handling escaped strings.
    */
  val testNodeForEscaping =
  parse(
  """{".tag":"root",".child":
      [{".tag":"document","id":"&lt;d0&gt;","text":"&amp;Test Node&quot;amp;"}
      ]
    }"""
  )

  /**
   * Unit testing toJSON
   */
  test("toJSON should generate formatted String object from scala.xml.Node"){
    parse(JSONUtil.toJSON(testNode)) should be (goldJSON)
    parse(JSONUtil.toJSON(testNodeForBackslash)) should be (goldJSONForBackSlash)
  }
  /**
   * Unit testing JSON to XML
   */
  test("toXML should generate xml.Node"){
    val xmlFromJSON = JSONUtil.toXML(goldJSON)
    val xmlFromJSONWithBackslash = JSONUtil.toXML(goldJSONForBackSlash)
    val xmlFromJSONWithEscapeChar = JSONUtil.toXML(testNodeForEscaping)
    xmlFromJSON should be (<root><document id={"d0"}>{"Test Node"}</document></root>)
    xmlFromJSONWithBackslash should be (<root><document id={"d0\\N"}>{"Test Node"}</document></root>)
    xmlFromJSONWithEscapeChar should be (<root><document id={"<d0>"}>{"&Test Node\"amp;"}</document></root>)
  }
}
