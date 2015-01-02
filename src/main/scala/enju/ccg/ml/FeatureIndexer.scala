package enju.ccg.ml

import scala.collection.mutable.HashMap

@SerialVersionUID(1L)
trait FeatureIndexer[Feature] extends Serializable {
  def size: Int

  /** Mutable indexing method which may add a new entry into the backbone map
    */
  def getIndex(key: Feature): Int

  /** Immutable indexing, -1 for unknown entry.
    */
  def get(key: Feature) = getIndex(key)

  def removeZeroWeightFeatures(weightsList: NumericBuffer[Float]*): Unit = {}
}

@SerialVersionUID(-3058955006170004987L)
class ExactFeatureIndexer[Feature] extends FeatureIndexer[Feature] {
  val map = new HashMap[Feature, Int]

  def size: Int = map.size

  def getIndex(key: Feature) = map.getOrElseUpdate(key, map.size)

  override def get(key: Feature) = map.getOrElse(key, -1)

  override def removeZeroWeightFeatures(weightsList: NumericBuffer[Float]*): Unit = {
    val baseWeights = weightsList(0)
    if (size > baseWeights.size) {
      removeElemsOver(baseWeights.size)
    }
    val oldSize = size
    val removingIdxs = baseWeights.zipWithIndex.filter(_._1 == 0).map(_._2)
    removeIndexes(removingIdxs)

    weightsList.foreach { weights =>
      weights.removeIndexes(removingIdxs)
      assert(weights.size == size)
    }
    println("feature size is reduced from " + oldSize + " -> " + size)
  }

  private def removeIndexes(idxs: Seq[Int]): Unit = {
    val features = map.toSeq.sortWith(_._2 < _._2).map(_._1)
    val originalSize = size
    (0 to idxs.size) foreach { i =>
      val idx = if (i == idxs.size) originalSize else idxs(i)
      val lastIdx = if (i == 0) -1 else idxs(i - 1)
      (lastIdx + 1 until idx).foreach { f => map(features(f)) -= i }
      if (i != idxs.size) map -= features(idx)
    }
  }
  private def removeElemsOver(lastIdx: Int): Unit = map.toSeq.foreach { case (feature, idx) =>
    if (idx >= lastIdx) map -= feature
  }
}

/** FeatureIndexer with hash trick. Hash value is calculated with MurmurHash3.
  *
  * Pros of this approach are:
  *  1) Very memory efficient; we don't have to hold a hashmap for millions of feature objects;
  *  2) Small loading time of model.
  *
  * The expense is a small loss of accuracy but usually this is really small...
  */
@SerialVersionUID(1L)
class HashedFeatureIndexer[Feature] private(
  val maxFeatureSize: Int,
  val hasher: (Feature => Int)) extends FeatureIndexer[Feature] {

  def size = maxFeatureSize

  def getIndex(key: Feature) = 1 + (math.abs(hasher(key)) % maxFeatureSize)
}

object HashedFeatureIndexer {
  def apply[Feature](
    maxFeatureSize: Int = (2 << 23),
    hasher: (Feature => Int) = {f: Feature => f.hashCode()}) = {

    val biggestPrimeBelow = primes.takeWhile(maxFeatureSize > _).last
    new HashedFeatureIndexer[Feature](biggestPrimeBelow, hasher)
  }

  private lazy val primes = 2 #:: sieve(3)

  private def sieve(n: Int): Stream[Int] =
    if (primes.takeWhile(p => p*p <= n).exists(n % _ == 0)) sieve(n + 2)
    else n #:: sieve(n + 2)
}
