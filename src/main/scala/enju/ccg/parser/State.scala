package enju.ccg.parser

import enju.ccg.lexicon.{Category, Point}
import enju.ccg.lexicon.Direction._

import scala.collection.mutable.Stack

// the representation of a category with additional inofmration necessary for parsing process
case class WrappedCategory(category:Category,
                           head:Int, // where the lexical head come from?
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
  //var superTagSeq:Array[Option[Category]] // assigned super-tags (this is necessary or not ?)
}

// represents a partial derivation tree preserved in a stack during decoding (assumed to not be handled in external resources)
case class StackedNode(item:WrappedCategory,
                       left:Option[StackedNode],
                       right:Option[StackedNode]) {
  def isTerminal = left == None && right == None
  def isUnary = left != None && right == None
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
    case StackedNode(WrappedCategory(_,_,Left,_,_),Some(left),_) => Some(left.item)
    case _ => None
  }
  private def rightCategoryIfHeadIsRight(node:StackedNode):Option[WrappedCategory] = if (node.isUnary) None else node match {
    case StackedNode(WrappedCategory(_,_,Right,_,_),_,Some(right)) => Some(right.item)
    case _ => None
  }
  private def childCategoryOfUnary(node:StackedNode):Option[WrappedCategory] = if (node.isUnary) {
    node match {
      case StackedNode(WrappedCategory(_,_,Left,_,_),Some(left),_) => Some(left.item)
      case _ => None
    }
  } else None
  private def headChild(node:StackedNode):Option[WrappedCategory] = node match { 
    case StackedNode(WrappedCategory(_,_,dir,_,_),left,right) => if (dir == Left) left match {
      case Some(left) => Some(left.item); case _ => None
    } else right match {
      case Some(right) => Some(right.item); case _ => None
    }
  }
  override def proceed(action:Action, actionIsGold:Boolean):FullState = {
    def doShift(category:Category) = {
      val wrappedCategory = WrappedCategory(category, j, Right, j, j + 1) // head = Right is meaningless
      val shiftingNewNode = StackedNode(wrappedCategory, None, None)
      val newStack = stack :+ shiftingNewNode // this data structure needs to copy the state objects here, which I hope have relatively small overhead (because stacked items are not so much)
      FullState(newStack, j + 1, actionIsGold)
    }
    def doCombine(category:Category, dir:Direction) = {
      val leftNode = stack(stack.size - 2)
      val rightNode = stack(stack.size - 1)
      
      val wrappedCategory = WrappedCategory(
        category,
        dir match { case Left => leftNode.item.head; case Right => rightNode.item.head },
        dir, leftNode.item.begin, rightNode.item.end)
      val combinedNode = StackedNode(wrappedCategory, Some(leftNode), Some(rightNode))
      val newStack = stack.dropRight(1)
      newStack(newStack.size - 1) = combinedNode
      FullState(newStack, j, actionIsGold)
    }
    def doUnary(category:Category) = {
      val topNode = stack.last
      val wrappedCategory = topNode.item match { case a => WrappedCategory(category, a.head, a.headDir, a.begin, a.end) }
      val raisedNode = StackedNode(wrappedCategory, Some(topNode), None)
      val newStack = stack.clone
      newStack(newStack.size - 1) = raisedNode
      FullState(newStack, j, actionIsGold)
    }
    action match {
      case Shift(category) => doShift(category)
      case Combine(category, dir) => doCombine(category, dir)
      case Unary(category) => doUnary(category)
      case Finish() => this // need to do anything?
    }
  }
}

// using SST described in Goldberg et al. (2013); might be more efficient ?
// class PartialState {
  
// }
