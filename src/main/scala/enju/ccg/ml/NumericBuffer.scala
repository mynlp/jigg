package enju.ccg.ml

import scala.collection.mutable.ArrayBuffer
import gnu.trove.TDoubleArrayList

class NumericBuffer[A](initSize:Int)(implicit numeric: Numeric[A]) extends ArrayBuffer[A](initSize) {
  def this()(implicit numeric: Numeric[A]) = this(16)(numeric)
  override def apply(idx: Int): A = if (idx >= size || idx < 0) numeric.zero else super.apply(idx)
  override def update(idx: Int, elem: A): Unit = {
    if (idx >= size) this ++= List.fill(idx - size + 1)(numeric.zero)
    super.update(idx, elem)
  }
  def removeIndexes(idxs: Seq[Int]): Unit = {
    val newElems = this.clone
    newElems.clear
    (0 to idxs.size) foreach { i =>
      val idx = if (i == idxs.size) size else idxs(i)
      val lastIdx = if (i == 0) -1 else idxs(i - 1)
      (lastIdx + 1 until idx).foreach { newElems += this(_) }
    }
    this.clear
    newElems.foreach { e => this += e }
  }
}
