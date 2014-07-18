package enju.ccg.lexicon

import scala.collection.immutable.ListMap
//import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/** Features assigned to a category.
  *
  * To assign hash value and equality, each subclass should be defined by case class.
  * The argument `values` represent all information of a category feature, but directly
  * calculating this from raw string might be difficult; To remedy this, current subclasses
  * have companion object with a constructor receiving information, which is easy to get
  * from a category string.
  */
trait CategoryFeature {
  def kvs: Seq[(String, String)]
  def unify(lhs: CategoryFeature): Boolean = false // TODO: implement
}

@SerialVersionUID(-8236395926230742650L)
case class JPCategoryFeature(values: Seq[String]) extends CategoryFeature {
  import JPCategoryFeature._

  override def kvs = keys zip values
  override def toString = kvs.filter(_._2 != "").map { case (k, v) => k + "=" + v }.mkString(",")
}

object JPCategoryFeature {
  // This is a hard-coded mapping of feature structure of Japanese category.
  private val k2vals = Map(
    "mod" -> Array("adv", "adn", "nm"),
    "form" -> Array("attr", "base", "cont", "hyp", "imp",
      "beg", "stem", "ta", "te", "pre", "r", "neg", "s", "da"),
    "case" -> Array("ga", "o", "ni", "to", "nc"))

  private val keys = k2vals.keys.toSeq
  private val v2keyIdx = {
    val key2idx = keys.zipWithIndex.toMap
    k2vals.flatMap { case (key, vals) =>
      vals.map { v => v -> key2idx(key) }
    }
  }
  val kvpair = """\w+=(\w+)""".r

  def createFromValues(values: Seq[String]) = values match {
    case Seq() => emptyFeature
    case _ =>
      val sortedValues = Array.fill(keys.size)("")
      values.filter(_!="").foreach { value =>
        val v = value match { case kvpair(v) => v; case v => v }
        v2keyIdx(v) match { case i => sortedValues(i) = v }
      }
      JPCategoryFeature(sortedValues)
  }
  // We cache this because mostly categories don't have a feature
  private val emptyFeature = JPCategoryFeature(Array.fill(keys.size)(""))
}

case class EnCategoryFeature(values: Seq[String]) extends CategoryFeature {
  override def kvs = values.zipWithIndex.map { case (v, k) => (k.toString, v) }
  override def toString = values.mkString(",")
}

object EnCategoryFeature {
  def createFromValues(values: Seq[String]) = EnCategoryFeature(values.sortWith(_ < _))
}
