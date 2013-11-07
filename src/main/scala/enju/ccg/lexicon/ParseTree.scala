package enju.ccg.lexicon

import scala.collection.mutable.ListMap

case class Span(begin:Int, end:Int)

// trait LeafItem
// case class JapaneseLeafItem(word:Word, base:Word, pos:PoS) extends LeafItem

sealed trait ParseTree[+T] { // T:LeafInfo (absorb language differences)
  def label: Category
  def children: List[ParseTree[T]]
  var span:Option[Span] = None

  def extractSpan(span: Option[Span]): (Int, Int) = span match {
    case Some(Span(b, e)) => (b, e)
    case _ => sys.error("oops. None span is going to be extracted!")
  }

  def getSequence: Seq[LeafNode[T]] = this match {
    case UnaryTree(child, label, _) => child.getSequence
    case BinaryTree(left, right, _, _) => left.getSequence ++ right.getSequence
    case leaf: LeafNode[_] => leaf :: Nil
  }
  def setSpans(i:Int = 0): Int = {
    this match {
      case leaf: LeafNode[_] => span = Some(Span(i, i+1))
      case node: IntermediateTree[_] =>
        var j = i
        node.children.foreach { subtree => j = subtree.setSpans(j) }
        span = Some(Span(i, j))
    }
    span.get.end
  }
  def toDerivation: Derivation = {
    setSpans(0)
    val length = span match {
      case Some(Span(0, j)) => j
      case _ => sys.error("setSpans error.")
    }
    val derivationMap = new Array[Array[ListMap[Category, AppliedRule]]](length+1)
    derivationMap.indices.foreach { derivationMap(_) = Array.fill(length+1)(new ListMap[Category, AppliedRule]) }

    def setDerivationMap(tree:ParseTree[_]): AppliedRule = tree match {
      case LeafNode(_,l) => AppliedRule(NoneChildPoint(), "")
      case UnaryTree(child, l, symbol) =>
        val (childBegin, childEnd) = extractSpan(child.span)
        derivationMap(childBegin)(childEnd) += child.label -> setDerivationMap(child)
        AppliedRule(UnaryChildPoint(Point(childBegin, childEnd, child.label)), symbol)
      case BinaryTree(left, right, l, symbol) =>
        val (leftBegin, leftEnd) = extractSpan(left.span)
        val (rightBegin, rightEnd) = extractSpan(right.span)
        derivationMap(leftBegin)(leftEnd) += left.label -> setDerivationMap(left)
        derivationMap(rightBegin)(rightEnd) += right.label -> setDerivationMap(right)
        AppliedRule(BinaryChildrenPoints(Point(leftBegin, leftEnd, left.label), Point(rightBegin, rightEnd, right.label)), symbol)
    }
    derivationMap(0)(length) += label -> setDerivationMap(this)

    Derivation(derivationMap, Array(Point(0, length, label)))
  }
  def foreachTree(f:ParseTree[_]=>Unit): Unit = {
    f(this)
    children.foreach { f(_) }
  }
  
  // TODO: conversion to dependency tree
  // def toDepTree(headFinder: HeadFinder) = {
  // }
}

sealed trait IntermediateTree[+T] extends ParseTree[T] {
  def ruleSymbol: String
}
case class UnaryTree[+T](child:ParseTree[T], override val label:Category, override val ruleSymbol:String) extends IntermediateTree[T] {
  def children = child :: Nil
}
case class BinaryTree[+T](left:ParseTree[T], right:ParseTree[T], override val label:Category, override val ruleSymbol:String) extends IntermediateTree[T] {
  def children = left :: right :: Nil
}
case class LeafNode[+T](info:T, override val label:Category) extends ParseTree[T] {
  def children = Nil
}
