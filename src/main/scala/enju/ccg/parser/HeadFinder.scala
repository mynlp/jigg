package enju.ccg.parser

import scala.collection.mutable.HashMap
import enju.ccg.lexicon.{PoS, JapanesePoS, Category}
import enju.ccg.lexicon.Direction._

trait HeadFinder extends Serializable {
  type NodeInfo = HeadFinder.NodeInfo
  def get(left:NodeInfo, right:NodeInfo): Direction
}
object HeadFinder {
  case class NodeInfo(pos:PoS, category:Category, headCategory:Category)
}

case class EnglishHeadFinder(children2dir: Map[(Int, Int), Direction]) extends HeadFinder {
  def get(left:NodeInfo, right:NodeInfo) =
    children2dir.get(left.category.id, right.category.id) match {
      case Some(dir) => dir
      case _ => Left
    }
}

object EnglishHeadFinder {
  import enju.ccg.lexicon.{ParseTree, NodeLabel, BinaryTree, NonterminalLabel}
  def createFromParseTrees(trees: Seq[ParseTree[NodeLabel]]): EnglishHeadFinder = {
    val map = new HashMap[(Int, Int), Direction]
    trees.foreach { _.foreachTree { _ match {
      case BinaryTree(left, right, NonterminalLabel(dir, _, _)) =>
        map += (left.label.category.id, right.label.category.id) -> dir
      case _ =>
    }}}
    EnglishHeadFinder(map.toMap)
  }
}

object JapaneseHeadFinder extends HeadFinder {
  val Symbol = "記号"
  def get(left:NodeInfo, right:NodeInfo) = {
    val leftPos = left.pos.first.v
    val rightPos = right.pos.first.v
    if (rightPos == Symbol) Left else Right
  }
}
