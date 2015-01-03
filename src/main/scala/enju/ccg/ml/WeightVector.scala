package enju.ccg.ml

import scala.collection.mutable.ArrayBuffer

@SerialVersionUID(1L)
trait WeightVector[@specialized(Int, Double, Float) A] extends Serializable {
  def apply(idx: Int): A
  def update(idx: Int, elem: A): Unit
  def size: Int

  def seq: IndexedSeq[A] // indexed seq from a backbone data structure
}

object WeightVector {
  def growable[A](initialSize: Int = 0)(implicit numeric: Numeric[A]) = new GrowableWeightVector[A](new ArrayBuffer[A](initialSize))(numeric)
}

class FixedWeightVector[@specialized(Int, Double, Float) A](val array: Array[A]) extends WeightVector[A] {
  def apply(idx: Int) = array(idx)
  def update(idx: Int, elem: A) = array(idx) = elem
  def size = array.size

  def seq = array
}

class GrowableWeightVector[@specialized(Int, Double, Float) A](val array: ArrayBuffer[A])(implicit numeric: Numeric[A]) extends WeightVector[A] {
  def apply(idx: Int) = if (idx >= size || idx < 0) numeric.zero else array(idx)
  def update(idx: Int, elem: A) = {
    if (idx >= array.size) array ++= List.fill(idx - array.size + 1)(numeric.zero)
    array(idx) = elem
  }
  def size = array.size

  def seq = array
}
