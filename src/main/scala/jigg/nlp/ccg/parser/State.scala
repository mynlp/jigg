package jigg.nlp.ccg.parser

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

import jigg.nlp.ccg.lexicon.{Category, Point, Derivation}
import jigg.nlp.ccg.lexicon.Direction._

import scala.collection.mutable.{Stack, ListMap}

// the representation of a category with additional inofmration necessary for parsing process
case class WrappedCategory(category:Category, // category of the intermediate node
                           ruleType:String,
                           head:Int, // where the lexical head come from?
                           headCategory:Category, // category of the terminal node of head position
                           headDir:Direction,
                           begin:Int, end:Int) {
  def cat = category.id
  def toDerivationPoint = Point(begin, end, category)

  override def toString = category + "(" + begin + ", " + head + ", " + end + ")"
}

sealed trait State {
  // the terminology is borrowed from the original paper of Zhang and Clark (2011)
  def s0:Option[WrappedCategory]
  def s1:Option[WrappedCategory]
  def s2:Option[WrappedCategory]
  def s3:Option[WrappedCategory]

  def s0l:Option[WrappedCategory]
  def s0r:Option[WrappedCategory]

  def s1l:Option[WrappedCategory]
  def s1r:Option[WrappedCategory]

  def s0h:Option[WrappedCategory]
  def s1h:Option[WrappedCategory]

  def s0u:Option[WrappedCategory]
  def s1u:Option[WrappedCategory]

  def j:Int // the top position of buffer
  def isGold:Boolean // whether this state potentially leads to the gold tree (only used when training)
  def proceed(action:Action, isGold:Boolean):State

  def print = List(s3,s2,s1,s0).collect{case Some(x)=>x}.mkString(" ") + " | " + "j=" + j
  def toDerivation: Derivation
}

// represents a partial derivation tree preserved in a stack during decoding (assumed to not be handled in external resources)
case class StackedNode(item:WrappedCategory,
                       left:Option[StackedNode],
                       right:Option[StackedNode]) {
  def isTerminal = left == None && right == None
  def isUnary = left != None && right == None

  def foreachNode(f: StackedNode => Unit): Unit = {
    f(this)
    left.foreach { _.foreachNode(f) }
    right.foreach { _.foreachNode(f) }
  }

  // def toParseFragment: ParseTree = {
  //   if (isTerminal) {
  //     LeafNode(
  //   } else if (isUnary) {

  //   }
  //   left.toParseFragment
  // }
}

object InitialFullState extends FullState(Array.empty[StackedNode], 0, true)

case class FullState(private val stack:Array[StackedNode],
                     override val j:Int,
                     override val isGold:Boolean = false) extends State {
  override def s0 = if (stack.size > 0) Some(stack(stack.size - 1).item) else None
  override def s1 = if (stack.size > 1) Some(stack(stack.size - 2).item) else None
  override def s2 = if (stack.size > 2) Some(stack(stack.size - 3).item) else None
  override def s3 = if (stack.size > 3) Some(stack(stack.size - 4).item) else None

  override def s0l = if (stack.size > 0) leftCategoryIfHeadIsLeft(stack(stack.size - 1)) else None
  override def s0r = if (stack.size > 0) rightCategoryIfHeadIsRight(stack(stack.size - 1)) else None

  override def s1l = if (stack.size > 1) leftCategoryIfHeadIsLeft(stack(stack.size - 2)) else None
  override def s1r = if (stack.size > 1) rightCategoryIfHeadIsRight(stack(stack.size - 2)) else None

  override def s0h = if (stack.size > 0) headChild(stack(stack.size - 1)) else None
  override def s1h = if (stack.size > 1) headChild(stack(stack.size - 2)) else None

  override def s0u = if (stack.size > 0) childCategoryOfUnary(stack(stack.size - 1)) else None
  override def s1u = if (stack.size > 1) childCategoryOfUnary(stack(stack.size - 2)) else None

  private def leftCategoryIfHeadIsLeft(node:StackedNode):Option[WrappedCategory] = if (node.isUnary) None else node match {
    case StackedNode(WrappedCategory(_,_,_,_,Left,_,_),Some(left),_) => Some(left.item)
    case _ => None
  }
  private def rightCategoryIfHeadIsRight(node:StackedNode):Option[WrappedCategory] = if (node.isUnary) None else node match {
    case StackedNode(WrappedCategory(_,_,_,_,Right,_,_),_,Some(right)) => Some(right.item)
    case _ => None
  }
  private def childCategoryOfUnary(node:StackedNode):Option[WrappedCategory] = if (node.isUnary) {
    node match {
      case StackedNode(WrappedCategory(_,_,_,_,_,_,_),Some(left),_) => Some(left.item)
      case _ => sys.error("never happen.")
    }
  } else None
  private def headChild(node:StackedNode):Option[WrappedCategory] = if (node.isUnary) {
    childCategoryOfUnary(node)
  } else {
    node match {
      case StackedNode(WrappedCategory(_,_,_,_,dir,_,_),left,right) => if (dir == Left) left match {
        case Some(left) => Some(left.item); case _ => None
      } else right match {
        case Some(right) => Some(right.item); case _ => None
      }
    }
  }
  override def proceed(action:Action, actionIsGold:Boolean):FullState = {
    def doShift(category:Category) = {
      val wrappedCategory = WrappedCategory(category, "", j, category, Right, j, j + 1) // head = Right is meaningless
      val shiftingNewNode = StackedNode(wrappedCategory, None, None)
      val newStack = stack :+ shiftingNewNode // this data structure needs to copy the state objects here, which I hope have relatively small overhead (because stacked items are not so much)
      FullState(newStack, j + 1, actionIsGold)
    }
    def doCombine(category:Category, dir:Direction, ruleType:String) = {
      val leftNode = stack(stack.size - 2)
      val rightNode = stack(stack.size - 1)

      val wrappedCategory = WrappedCategory(
        category,
        ruleType,
        dir match { case Left => leftNode.item.head; case Right => rightNode.item.head },
        dir match { case Left => leftNode.item.headCategory
                    case Right => rightNode.item.headCategory },
        dir, leftNode.item.begin, rightNode.item.end)
      val combinedNode = StackedNode(wrappedCategory, Some(leftNode), Some(rightNode))
      val newStack = stack.dropRight(1)
      newStack(newStack.size - 1) = combinedNode
      FullState(newStack, j, actionIsGold)
    }
    def doUnary(category:Category, ruleType:String) = {
      val topNode = stack.last
      val wrappedCategory = topNode.item match { case a => WrappedCategory(category, ruleType, a.head, a.headCategory, a.headDir, a.begin, a.end) }
      val raisedNode = StackedNode(wrappedCategory, Some(topNode), None)
      val newStack = stack.clone
      newStack(newStack.size - 1) = raisedNode
      FullState(newStack, j, actionIsGold)
    }
    action match {
      case Shift(category) => doShift(category)
      case Combine(category, dir, ruleType) => doCombine(category, dir, ruleType)
      case Unary(category, ruleType) => doUnary(category, ruleType)
      case Finish() => this // need to do anything?
    }
  }

  override def toDerivation: Derivation = {
    import jigg.nlp.ccg.lexicon.{NoneChildPoint, UnaryChildPoint, BinaryChildrenPoints, Point, ChildPoint, AppliedRule}

    val derivationMap = Array.fill(j + 1)(Array.fill(j+1)(new ListMap[Category, AppliedRule]))
    stack.foreach { _.foreachNode({
      node:StackedNode => node.item match {
        case WrappedCategory(category,ruleType,_,_,_,b,e) =>
          if (node.isTerminal) {
            derivationMap(b)(e) += category -> AppliedRule(NoneChildPoint(), "")
          } else if (node.isUnary) {
            derivationMap(b)(e) += category ->
            AppliedRule(UnaryChildPoint(Point(b, e, node.left.get.item.category)), ruleType)
          } else {
            (node.left, node.right) match {
              case (Some(StackedNode(leftItem,_,_)), Some(StackedNode(rightItem,_,_))) =>
                derivationMap(b)(e) += category -> AppliedRule(BinaryChildrenPoints(
                  Point(leftItem.begin, leftItem.end, leftItem.category),
                  Point(rightItem.begin, rightItem.end, rightItem.category)), ruleType)
              case _ =>
            }
          }
      }})}
    val roots = stack.map { node => Point(node.item.begin, node.item.end, node.item.category) }
    Derivation(derivationMap, roots)
  }
}

// using SST described in Goldberg et al. (2013); might be more efficient ?
// class PartialState {

// }
