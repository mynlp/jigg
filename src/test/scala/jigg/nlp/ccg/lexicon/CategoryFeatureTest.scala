package jigg.nlp.ccg.lexicon
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import scala.collection.mutable.HashSet

class JPCategoryFeatureTest extends FunSuite {
  test("equal test") {
    val feat1 = JPCategoryFeature.createFromValues(List("adn","attr","ga"))
    val feat2 = JPCategoryFeature.createFromValues(List("nm","attr","ga"))
    val feat3 = JPCategoryFeature.createFromValues(List("adn","attr"))
    val feat4 = JPCategoryFeature.createFromValues(List("adn","attr","ga"))

    feat1.kvs should equal (feat4.kvs)
    feat1.kvs should not equal (feat2.kvs)
    feat1.kvs should not equal (feat3.kvs)
  }
}
