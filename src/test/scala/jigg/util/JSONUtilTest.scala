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
  val goldJSONString =
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
  val goldJSON = parse(goldJSONString.stripMargin)

  /**
    * For handling a backslash.
    */
  val testNodeForBackslash =
    <root>
      <document id={"d0\\N"}>
        Test Node
      </document>
    </root>
  val goldJSONForBackSlashString =
    """{".tag":"root",".child":
      [{".tag":"document","id":"d0\\N","text":"Test Node"}
      ]
    }"""
  val goldJSONForBackSlash = parse(goldJSONForBackSlashString.stripMargin)
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
    xmlFromJSON should be (<root><document id={"d0"}>{"Test Node"}</document></root>)
    xmlFromJSONWithBackslash should be (<root><document id={"d0\\N"}>{"Test Node"}</document></root>)
  }
}
