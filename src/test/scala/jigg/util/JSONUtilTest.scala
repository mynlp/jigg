package jigg.util

package jigg.pipeline

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class JSONUtiltest extends FunSuite{
  import JSONUtil._

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

  test("toJSON generate StringBuilder object from scala.xml.Node"){
    JSONUtil.toJSON(node).getClass should be (classOf[StringBuilder])
  }
}
