package jigg.util

package jigg.pipeline

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class JSONUtiltest extends FunSuite{
  import JSONUtil._
  import scala.xml._
  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  val node = 
    <root>
      <document id={"d0"}>
        <sentences>
          <sentence characterOffsetEnd={"10"} characterOffsetBegin={"0"} id={"s0"}>
            Test Node
            <tokens annotators={"corenlp"}>
              <token form={"Test"} id={"t0"} characterOffsetBegin={"0"} characterOffsetEnd={"5"}/>
              <token form={"Node"} id={"t1"} characterOffsetBegin={"5"} characterOffsetEnd={"10"}/>
            </tokens>
          </sentence>
        </sentences>
      </document>
    </root>

  /**
   * Unit testing toJSON
   */
  test("toJSON should generate formatted String object from scala.xml.Node"){
    JSONUtil.toJSON(node).getClass should be (classOf[String])
  }

  /**
   * Unit testing JSON to XML
   */
  test("toXML should generate xml.Node"){
    val jsonString = JSONUtil.toJSON(node)
    val jsonValue = parse(jsonString)
    val xmlNode = JSONUtil.toXML(jsonValue)

    jsonString.getClass should be (classOf[String])
    jsonValue.isInstanceOf[JValue] should be (true)
    xmlNode.isInstanceOf[Node] should be (true)
  }
}
