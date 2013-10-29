package enju.ccg.lexicon
import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.ArrayBuffer

/**
 * Numbered objects are usually managed by this (and extended) class
 */
@SerialVersionUID(1L)
abstract class NumberedManager[T<:Numbered[_]] extends Serializable {
  val str2v = new LinkedHashMap[String, T]
  val values = new ArrayBuffer[T]
  def getOrCreate(str:String):T = str2v.get(str) match {
    case Some(v) => v
    case None => {
      val v = createInstance(values.size, str)
      str2v += str -> v
      values += v
      v
    }
  }
  def apply(id:Int) = values(id)
  def createInstance(newId:Int, str:String):T

  def transformValues(f:T => T) = {
    str2v.zipWithIndex.foreach {
      case ((k, v), i) => {
        val newV = f(v)
        str2v += k -> newV // this update does not change the order of LinkedHashMap
        values(i) = newV
      }
    }
  }
}
