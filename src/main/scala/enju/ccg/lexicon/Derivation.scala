package enju.ccg.lexicon

import scala.collection.mutable.ListMap

case class Point(x:Int, y:Int, category:Category)

sealed trait Children
case class UnaryChild(p:Point) extends Children
case class BinaryChildren(left:Point, right:Point) extends Children

/**
 * Internal representation of gold tree, and parsed tree for output (and evaluation)
 */
case class Derivation(map:Array[Array[ListMap[Category, Children]]], root:Point) { // map: span[i,j] -> Map[Category, Children]
  def get(i:Int, j:Int, category:Category):Option[Children] = map(i)(j).get(category)
}
