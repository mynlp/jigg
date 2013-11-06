package enju.ccg.lexicon

import scala.collection.mutable.ListMap

case class Point(x:Int, y:Int, category:Category)

sealed trait ChildPoint {
  def childPoints:List[Point]
}
case class UnaryChildPoint(p:Point) extends ChildPoint {
  override def childPoints = p :: Nil
}
case class BinaryChildrenPoints(left:Point, right:Point) extends ChildPoint {
  override def childPoints = left :: right :: Nil
}
case class NoneChildPoint() extends ChildPoint { // leaf node's children
  override def childPoints = Nil
}

/**
 * Internal representation of gold tree, and parsed tree for output (and evaluation)
 */
case class Derivation(map:Array[Array[ListMap[Category, ChildPoint]]], root:Point) { // map: span[i,j] -> Map[Category, Children]
  def get(i:Int, j:Int, category:Category): Option[ChildPoint] = map(i)(j).get(category)
  def get(p:Point): Option[ChildPoint] = p match { case Point(x, y, c) => get(x, y, c) }

  def rulesAt(i:Int, j:Int): ListMap[Category, ChildPoint] = map(i)(j)
  def parentCategory(childPoint:ChildPoint): Option[Category] = childPoint match {
    case UnaryChildPoint(child) =>
      rulesAt(child.x, child.y).find(_._2 == childPoint).map(_._1)
    case BinaryChildrenPoints(left, right) =>
      rulesAt(left.x, right.y).find(_._2 == childPoint).map(_._1)
    case _ => None
  }

  def foreachPoint(f:Point=>Unit, point:Point = root):Unit = {
    f(point)
    get(point) map { _.childPoints.foreach { foreachPoint(f, _) } }
  }
}
