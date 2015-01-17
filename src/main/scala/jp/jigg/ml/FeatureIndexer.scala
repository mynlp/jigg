package jp.jigg.ml

import scala.collection.mutable.{HashMap, ArrayBuffer}

@SerialVersionUID(1L)
trait FeatureIndexer[Feature] extends Serializable {
  def size: Int

  /** Mutable indexing method which may add a new entry into the backbone map
    */
  def getIndex(key: Feature): Int

  /** Immutable indexing, -1 for unknown entry.
    */
  def get(key: Feature) = getIndex(key)
}

@SerialVersionUID(1L)
class ExactFeatureIndexer[Feature](val map: HashMap[Feature, Int]) extends FeatureIndexer[Feature] {

  def size: Int = map.size

  def getIndex(key: Feature) = map.getOrElseUpdate(key, map.size)

  override def get(key: Feature) = map.getOrElse(key, -1)
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

  def getIndex(key: Feature) = (math.abs(hasher(key)) % maxFeatureSize)
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
