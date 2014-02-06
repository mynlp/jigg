package enju.ccg.ml

import scala.collection.mutable.HashMap

class FeatureIndexer[Feature] extends HashMap[Feature, Int] {
  def getIndex(key: Feature) = getOrElseUpdate(key, size)

  def removeIndexes(idxs: Seq[Int]): Unit = {
    val features = this.toSeq.sortWith(_._2 < _._2).map(_._1)
    val originalSize = size
    (0 to idxs.size) foreach { i =>
      val idx = if (i == idxs.size) originalSize else idxs(i)
      val lastIdx = if (i == 0) -1 else idxs(i - 1)
      (lastIdx + 1 until idx).foreach { f => this(features(f)) -= i }
      if (i != idxs.size) this -= features(idx)
    }
  }
  def removeElemsOver(lastIdx: Int): Unit = this.toSeq.foreach { case (feature, idx) =>
    if (idx >= lastIdx) this -= feature
  }
}
