package jigg.ml

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
