package jp.jigg.nlp.ccg.lexicon

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
