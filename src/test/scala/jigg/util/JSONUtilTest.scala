package jigg.util

package jigg.pipeline

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class JSONUtiltest extends FunSuite{
  import JSONUtil._
  import scala.xml._
  import org.json4s._

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

  val textNode = 
    <sentence id="s0" characterOffsetBegin="0" characterOffsetEnd="11">
      Hello Jigg!
      <tokens annotators="corenlp">
        <token characterOffsetEnd="5" characterOffsetBegin="0" id="t0" form="Hello"/>
        <token characterOffsetEnd="10" characterOffsetBegin="6" id="t1" form="Jigg"/>
        <token characterOffsetEnd="11" characterOffsetBegin="10" id="t2" form="!"/>
      </tokens>
    </sentence>

  val textNodeWithNewLine = 
    <sentence id="s0" characterOffsetBegin="0" characterOffsetEnd="11">
      {"Hello Jigg!\n"}
      <tokens annotators="corenlp">
        <token characterOffsetEnd="5" characterOffsetBegin="0" id="t0" form="Hello"/>
        <token characterOffsetEnd="10" characterOffsetBegin="6" id="t1" form="Jigg"/>
        <token characterOffsetEnd="11" characterOffsetBegin="10" id="t2" form="!"/>
      </tokens>
    </sentence>

  /**
   * Unit testing toJSON
   */
  test("toJSON should generate formatted String object from scala.xml.Node"){
    JSONUtil.toJSON(node).getClass should be (classOf[String])
  }

  /**
   * Unit testing XMLParser 
   */
  test("isTextNode should be return true when a text node is input"){
    JSONUtil.XMLParser.isTextNode(textNode) should be (true)
  }

  test("isTextNode should be return true when a text node with newline character is input"){
    JSONUtil.XMLParser.isTextNode(textNodeWithNewLine) should be (true)
  }

  /**
   * Unit testing JSON to XML
   */
  test("toXML should generate xml.Node"){
    val jsonString = JSONUtil.toJSON(node)
    val jsonValue = JSONUtil.parseJSON(jsonString)
    val xmlNode = JSONUtil.toXML(jsonValue)

    jsonString.getClass should be (classOf[String])
    jsonValue.isInstanceOf[JValue] should be (true)
    xmlNode.isInstanceOf[Node] should be (true)
  }
}
