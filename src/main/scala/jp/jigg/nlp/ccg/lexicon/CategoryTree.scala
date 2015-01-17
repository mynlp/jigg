package jp.jigg.nlp.ccg.lexicon
import Slash._

case class CategoryTree(var surface:String, slash:Slash, left:CategoryTree, right:CategoryTree) {
  def isLeaf = left == null && right == null
  def setSurface:CategoryTree = {
    def childSurface(child:CategoryTree) =
      if (child.isLeaf) child.surface else '(' + child.surface + ')'

    if (isLeaf) assert(surface != null)
    else surface = childSurface(left) + slash + childSurface(right)
    this
  }
  def foreachLeaf(f:CategoryTree=>Any):Unit = {
    if (isLeaf) f(this)
    else List(left,right).foreach(_.foreachLeaf(f))
  }
}

object CategoryTree {
  def createLeaf(surface:String) = CategoryTree(surface, null, null, null)
  def createInternal(slash:Slash, left:CategoryTree , right:CategoryTree) =
    CategoryTree(null, slash, left, right)
}
