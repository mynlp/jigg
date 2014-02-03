package enju.ccg.lexicon

import scala.collection.mutable.ListMap

case class Point(x:Int, y:Int, category:Category)

sealed trait ChildPoint {
  def points:List[Point]
}
case class UnaryChildPoint(p:Point) extends ChildPoint {
  override def points = p :: Nil
}
case class BinaryChildrenPoints(left:Point, right:Point) extends ChildPoint {
  override def points = left :: right :: Nil
}
// TODO: make this case object
case class NoneChildPoint() extends ChildPoint { // leaf node's children
  override def points = Nil
}

case class AppliedRule(childPoint:ChildPoint, ruleSymbol:String = "") {
  childPoint match {
    case UnaryChildPoint(_) => require(ruleSymbol == "ADV" || ruleSymbol == "ADN")
    case BinaryChildrenPoints(_,_) => require(ruleSymbol == "<" || ruleSymbol == ">" || ruleSymbol == "Φ")
    case NoneChildPoint() => require(ruleSymbol == "")
  }
}

/**
 * Internal representation of gold tree, and parsed tree for output (and evaluation)
 */
case class Derivation(map:Array[Array[ListMap[Category, AppliedRule]]], roots:Array[Point]) { // map: span[i,j] -> Map[Category, (Children, ruleType)]
  def root = roots(0)
  def get(i:Int, j:Int, category:Category): Option[AppliedRule] = map(i)(j).get(category) // find a derivation rule from a category that spans [i,j]
  def get(p:Point): Option[AppliedRule] = p match { case Point(x, y, c) => get(x, y, c) }

  def rulesAt(i:Int, j:Int): ListMap[Category, AppliedRule] = map(i)(j)
  def parentCategory(childPoint:ChildPoint): Option[(Category, String)] = {
    def findParentAndRuleSpanning(i:Int, j:Int, childPoint:ChildPoint) = rulesAt(i, j).find {
      case (category, appliedRule) if (appliedRule.childPoint == childPoint) => true
      case _ => false
    }.map { case (category, appliedRule) => (category, appliedRule.ruleSymbol) }

    childPoint match {
      case UnaryChildPoint(child) => findParentAndRuleSpanning(child.x, child.y, childPoint)
      case BinaryChildrenPoints(left, right) => findParentAndRuleSpanning(left.x, right.y, childPoint)
      case _ => None
    }
  }
  def foreachPoint(f:Point=>Unit, point:Point = root):Unit = {
    f(point)
    get(point) map { _.childPoint.points.foreach { foreachPoint(f, _) } }
  }
  def foreachPointBottomup(f: Point => Unit, point: Point = root): Unit = {
    get(point) map { _.childPoint.points.foreach { foreachPointBottomup(f, _) } }
    f(point)
  }
  def categorySeq: Array[Option[Category]] = (0 until map.size - 1).map { i =>
    val terminalRule = rulesAt(i, i+1).collect {
      case (leafCategory, AppliedRule(NoneChildPoint(),_)) =>
        Some(leafCategory)
    }.toList
    assert(terminalRule.size == 1)
    terminalRule(0)
  }.toArray

  def render(sentence:TaggedSentence): String = { // outputs in CCGBank format
    val leftSides = Array.fill(map.size)("")
    val rightSides = Array.fill(map.size)("")

    def addLeftTo(i:Int, str:String):Unit = leftSides(i) = leftSides(i) + str
    def addRightTo(i:Int, str:String):Unit = rightSides(i) = str + rightSides(i)
    roots.sortWith(_.x < _.x).foreach {
      r => {
        foreachPoint( { point:Point =>
          get(point).map {
            _ match {
              case AppliedRule(NoneChildPoint(), ruleSymbol) =>
                addLeftTo(point.x, "{"+point.category+" "+sentence.word(point.x)+"/"+sentence.pos(point.x))
                addRightTo(point.x, "}")
              case AppliedRule(_, ruleSymbol) =>
                addLeftTo(point.x, "{"+ruleSymbol+" "+point.category+" ")
                addRightTo(point.y-1, "}")
              case _ =>
            }
          }
        }, r)
      }
    }
    leftSides.zip(rightSides).map { case(a,b)=> a+b }.mkString(" ").trim
  }
}
