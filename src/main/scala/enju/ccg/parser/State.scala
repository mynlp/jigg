package enju.ccg.parser

import enju.ccg.lexicon.{Category, Point, Derivation}
import enju.ccg.lexicon.Direction._

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

object InitialFullState extends FullState(Nil, 0, true)

case class FullState(private val stack:List[StackedNode],
                     override val j:Int,
                     override val isGold:Boolean = false) extends State {
  def n0 = stack match {
    case node :: _ => Some(node)
    case _ => None
  }
  def n1 = stack match {
    case _ :: node :: _ => Some(node)
    case _ => None
  }
  def n2 = stack match {
    case _ :: _ :: node :: _ => Some(node)
    case _ => None
  }
  def n3 = stack match {
    case _ :: _ :: _ :: node :: _ => Some(node)
    case _ => None
  }

  // These accessor cost a little. I don't override these members with vals, because
  // the user of these accessor, i.e., feature extractor, memorize these values
  override def s0 = n0 map { _.item }
  override def s1 = n1 map { _.item }
  override def s2 = n2 map { _.item }
  override def s3 = n3 map { _.item }

  // override def s0 = if (stack.size > 0) Some(stack(stack.size - 1).item) else None
  // override def s1 = if (stack.size > 1) Some(stack(stack.size - 2).item) else None
  // override def s2 = if (stack.size > 2) Some(stack(stack.size - 3).item) else None
  // override def s3 = if (stack.size > 3) Some(stack(stack.size - 4).item) else None

  override def s0l = n0 flatMap { leftCategoryIfHeadIsLeft(_) }
  override def s0r = n0 flatMap { rightCategoryIfHeadIsRight(_) }

  override def s1l = n1 flatMap { leftCategoryIfHeadIsLeft(_) }
  override def s1r = n1 flatMap { rightCategoryIfHeadIsRight(_) }

  override def s0h = n0 flatMap { headChild(_) }
  override def s1h = n1 flatMap { headChild(_) }

  override def s0u = n0 flatMap { childCategoryOfUnary(_) }
  override def s1u = n1 flatMap { childCategoryOfUnary(_) }

  // override def s0l = if (stack.size > 0) leftCategoryIfHeadIsLeft(stack(stack.size - 1)) else None
  // override def s0r = if (stack.size > 0) rightCategoryIfHeadIsRight(stack(stack.size - 1)) else None

  // override def s1l = if (stack.size > 1) leftCategoryIfHeadIsLeft(stack(stack.size - 2)) else None
  // override def s1r = if (stack.size > 1) rightCategoryIfHeadIsRight(stack(stack.size - 2)) else None

  // override def s0h = if (stack.size > 0) headChild(stack(stack.size - 1)) else None
  // override def s1h = if (stack.size > 1) headChild(stack(stack.size - 2)) else None

  // override def s0u = if (stack.size > 0) childCategoryOfUnary(stack(stack.size - 1)) else None
  // override def s1u = if (stack.size > 1) childCategoryOfUnary(stack(stack.size - 2)) else None

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
      val newStack = shiftingNewNode :: stack
      // val newStack = stack :+ shiftingNewNode // this data structure needs to copy the state objects here, which I hope have relatively small overhead (because stacked items are not so much)
      FullState(newStack, j + 1, actionIsGold)
    }
    def doCombine(category:Category, dir:Direction, ruleType:String) = {
      // Direction is not intuiteive; because List is FIFO (first elem = most right)
      val (rightNode, leftNode) = stack match {
        case right :: left :: _ => (right, left)
        case _ => sys.error("Combine action when stack depth = 1")
      }
      val wrappedCategory = WrappedCategory(
        category,
        ruleType,
        dir match { case Left => leftNode.item.head; case Right => rightNode.item.head },
        dir match { case Left => leftNode.item.headCategory
                    case Right => rightNode.item.headCategory },
        dir, leftNode.item.begin, rightNode.item.end)
      val combinedNode = StackedNode(wrappedCategory, Some(leftNode), Some(rightNode))

      val newStack = combinedNode :: stack.tail.tail
      // val newStack = stack.dropRight(1)
      // newStack(newStack.size - 1) = combinedNode
      FullState(newStack, j, actionIsGold)
    }
    def doUnary(category:Category, ruleType:String) = {
      val topNode = n0.get
      val wrappedCategory = topNode.item match { case a => WrappedCategory(category, ruleType, a.head, a.headCategory, a.headDir, a.begin, a.end) }
      val raisedNode = StackedNode(wrappedCategory, Some(topNode), None)

      val newStack = raisedNode :: stack.tail
      // val newStack = stack.clone
      // newStack(newStack.size - 1) = raisedNode
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
    import enju.ccg.lexicon.{NoneChildPoint, UnaryChildPoint, BinaryChildrenPoints, Point, ChildPoint, AppliedRule}

    val derivationMap = Array.fill(j + 1)(Array.fill(j + 1)(new ListMap[Category, AppliedRule]))
    // val derivationMap = new Array[Array[ListMap[Category, AppliedRule]]](j+1)
    // derivationMap.indices.foreach { derivationMap(_) = Array.fill(j + 1)(new ListMap[Category, AppliedRule]) }
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
    val roots = stack.reverse.toArray.map { node => Point(node.item.begin, node.item.end, node.item.category) }
    Derivation(derivationMap, roots)
  }
}

// using SST described in Goldberg et al. (2013); might be more efficient ?
// class PartialState {

// }
