package enju.ccg.lexicon
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class CategoryManagerTest extends FunSuite with ShouldMatchers {
  AVMInitializer.init

  val parser = new CategoryParser

  test("the same child node should be assiged the same id") {
    val manager = new CategoryManager
    manager.assignID(parser.parse("NP")) // dummy category (to start the test with id > 0)

    val cat = parser.parse("NP[o,nm]／NP[o,nm]")
    manager.assignID(cat) match {
      case ComplexCategory(id, left, right, _) => {
        left.id should equal (1)
        right.id should equal (1)
        id should equal (2)
      }
      case _ => fail() // should not occur
    }
  }
}
