package enju.ccg.lexicon

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

class CategoryManager extends Serializable {
  val canonicalCategoryMap = new HashMap[Category, Category] // map the category with id 0 (canonical form) to the category with the category-specific id
  private val categories = new ArrayBuffer[Category]
  
  private def newId = categories.size
  private def addEntry(original:Category, withId:Category):Unit= {
    canonicalCategoryMap += original -> withId
    categories += withId
  }
  def apply(i:Int) = categories(i)

  // WARNING: this method return the different instance of Category than the argument.
  def assignID(original:Category):Category = {
    require(original.id == 0, "given category to assignID should not be given ID previously.")
    original match {
      case AtomicCategory(id, base, avm) => canonicalCategoryMap.get(original) match {
        case Some(withId) => withId
        case None => {
          val withId = AtomicCategory(newId, base, avm)
          addEntry(original, withId)
          withId
        }
      }
      case ComplexCategory(id, left, right, slash) => canonicalCategoryMap.get(original) match {
        // be careful to the order; all keys of canonicalCategoryMap have id 0 including child categories
        case Some(withId) => withId
        case None => {
          val leftWithId = assignID(left)
          val rightWithId = assignID(right)
          val withId = ComplexCategory(newId, leftWithId, rightWithId, slash)
          addEntry(original, withId)
          withId
        }
      }
    }
  }
}
