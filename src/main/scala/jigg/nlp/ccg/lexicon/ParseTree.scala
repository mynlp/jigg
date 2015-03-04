package jigg.nlp.ccg.lexicon

case class Span(begin:Int, end:Int)

sealed trait ParseTree[T] {
  def label: T
  def children: List[ParseTree[T]]
  var span:Option[Span] = None

  def getSequence: Seq[LeafTree[T]] = this match {
    case UnaryTree(child, _) => child.getSequence
    case BinaryTree(left, right, _) => left.getSequence ++ right.getSequence
    case leaf: LeafTree[_] => leaf :: Nil
  }
  def setSpans(i:Int = 0): Int = {
    this match {
      case leaf: LeafTree[_] => span = Some(Span(i, i+1))
      case node =>
        var j = i
        node.children.foreach { subtree => j = subtree.setSpans(j) }
        span = Some(Span(i, j))
    }
    span.get.end
  }

  def foreachTree[U](f:ParseTree[T]=>U): Unit = {
    f(this)
    children.foreach { _.foreachTree(f) }
  }

  // f receives ParseTree, not label, because it might use child node to add information to the new label, e.g., deciding syntactic head child might require to access to its children.
  def mapBottomup[NewLabel](f: ParseTree[T] => NewLabel): ParseTree[NewLabel] = {
    val mappedTree = mapBottomupHelper(f)
    mappedTree.span = span
    mappedTree
  }
  protected def mapBottomupHelper[NewLabel](f: ParseTree[T] => NewLabel): ParseTree[NewLabel]
}

case class UnaryTree[T](child: ParseTree[T], override val label: T) extends ParseTree[T] {
  override def children = child :: Nil
  override def mapBottomupHelper[NewLabel](f: ParseTree[T] => NewLabel): ParseTree[NewLabel] =
    UnaryTree(child.mapBottomup(f), f(this))
}

case class BinaryTree[T](left: ParseTree[T], right: ParseTree[T], override val label: T) extends ParseTree[T] {
  override def children = left :: right :: Nil
  override def mapBottomupHelper[NewLabel](f: ParseTree[T] => NewLabel) =
    BinaryTree(left.mapBottomup(f), right.mapBottomup(f), f(this))
}

case class LeafTree[T](override val label: T) extends ParseTree[T] {
  override def children = Nil
  override def mapBottomupHelper[NewLabel](f: ParseTree[T] => NewLabel) = LeafTree(f(this))
}
