package jigg.nlp.ccg.lexicon

/*
 Copyright 2013-2015 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
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
