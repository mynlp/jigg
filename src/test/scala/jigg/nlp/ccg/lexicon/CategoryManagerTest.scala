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

class CategoryManagerTest extends FunSuite {
  test("the same child node should be assiged the same id") {
    val manager = new CategoryManager // Constructor automatically creates unknown category which is assigned id 0

    val cat = JapaneseCategoryParser.parse("NP[case=o,mod=nm]/NP[case=o,mod=nm]")
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
