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

case class AppliedRule(childPoint:ChildPoint, ruleSymbol:String = "")

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
    def tokenStr(i: Int) = s"${sentence.word(i)}/${sentence.base(i)}/${sentence.pos(i)}"

    def renderSubtree(point: Point): String = get(point) match {
      case Some(AppliedRule(NoneChildPoint(), _)) => // terminal
        s"{${point.category} ${tokenStr(point.x)}}"
      case Some(AppliedRule(UnaryChildPoint(c), ruleSymbol)) =>
        s"{${ruleSymbol} ${point.category} ${renderSubtree(c)}}"
      case Some(AppliedRule(BinaryChildrenPoints(l, r), ruleSymbol)) =>
        s"{${ruleSymbol} ${point.category} ${renderSubtree(l)} ${renderSubtree(r)}}"
      case _ => ""
    }
    roots.sortWith(_.x < _.x).map { renderSubtree(_) }.mkString(" ")
  }

  def renderEnjuXML(sentence:TaggedSentence, id: Int): String = {
    def removeFeatureKeys(category: Category) = {
      val r = """([^\[,]\w+)=""".r
      r.replaceAllIn(category.toString, "")
    }
    def consPre(cat: Category) = s"""<cons id="c{cat.id}" cat="${removeFeatureKeys(cat)}">"""
    def tok(p: Point) = s"""<tok id="t${p.category.id}" surface="${sentence.word(p.x)}" pos="${sentence.pos(p.x)}">${sentence.word(p.x)}</tok>"""

    def renderSubtree(point: Point): String = get(point) match {
      case Some(AppliedRule(NoneChildPoint(), _)) => // terminal
        consPre(point.category) + tok(point) + "</cons>"
      case Some(AppliedRule(UnaryChildPoint(c), ruleSymbol)) =>
        consPre(point.category) + renderSubtree(c) + "</cons>"
      case Some(AppliedRule(BinaryChildrenPoints(l, r), ruleSymbol)) =>
        consPre(point.category) + renderSubtree(l) + renderSubtree(r) + "</cons>"
      case _ => ""
    }
    s"""<sentence id="s$id" parse_status="success">${roots.sortWith(_.x < _.x).map { renderSubtree(_) }.mkString}</sentence>"""
  }

  /** This method combines separated subtrees in the derivation into one tree by force.
    * Currently, this combine process is done in left-to-right order, that is,
    * the most left two subtrees are combined, which head is the right child.
    * This procedure is intended for head-last language, e.g., Japanese, so not generally applicable, but partly solve the problem of separated subtrees on bunsetsu-dependency evaluation.
    */
  def toSingleRoot: Derivation = {
    val newMap = map.map { _.clone }
    (1 until roots.size) foreach { j =>
      val i = j - 1

      newMap(0)(roots(j).y) += roots(j).category -> AppliedRule(BinaryChildrenPoints(
        Point(0, roots(i).y, roots(i).category),
        roots(j)), ">")
    }
    val newRoot = Point(0, roots.last.y, roots.last.category)
    Derivation(newMap, Array(newRoot))
  }
}
