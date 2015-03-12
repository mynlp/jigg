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

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

class CategoryManager extends StringBaseNumberedManager[Category] with OptionReturner[Category] {
  override def createWithId(original:Category): Category = original match {
    case AtomicCategory(id, base, avm) => AtomicCategory(newId, base, avm)
    case ComplexCategory(id, left, right, slash) =>
      val leftWithId = assignID(left)
      val rightWithId = assignID(right)
      ComplexCategory(newId, leftWithId, rightWithId, slash)
  }
  override def getOrNone(str:String): Option[Category] = str2objIndex.get(str) match {
    case Some(i) => Some(objects(i))
    case None => canonicalMap.get(createCanonicalInstance(str))
  }

  override def createCanonicalInstance(str:String): Category = JapaneseCategoryParser.parse(str)

  // This is used when candidate shift category is empty
  // It sometimes happen if for example, PoS not registered in the dictionary is detected.
  val unkCategory = getOrCreate("UNK")
}
