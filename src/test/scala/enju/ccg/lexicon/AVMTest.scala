package enju.ccg.lexicon
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import scala.collection.mutable.HashSet

class AVMTest extends FunSuite {
  AVMInitializer.init

  // the test below is no longer satisfiable
  // test("features are correctly read, resulting in sorted v2k values in insert order") {
  //   var keysAppearedSoFar = new HashSet[Int]
  //   var prev = 0
  //   print(AVM.v2keyIdx)
  //   AVM.v2keyIdx.foreach { case (v, i) => { if (keysAppearedSoFar.contains(i)) {
  //     assert(prev == i)
  //     prev = i
  //   }}}
  // }
  
  test("equal test") {
    val avm1 = AVM.createFromValues(List("adn","attr","ga"))
    val avm2 = AVM.createFromValues(List("nm","attr","ga"))
    val avm3 = AVM.createFromValues(List("adn","attr"))
    val avm4 = AVM.createFromValues(List("adn","attr","ga"))
    
    avm1 should equal (avm4)
    avm1 should not equal (avm2)
    avm1 should not equal (avm3)
    avm1.values should equal (avm4.values)
  }
}
