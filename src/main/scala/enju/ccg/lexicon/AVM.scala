package enju.ccg.lexicon
import scala.collection.immutable.ListMap
//import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

case class AVM(val values:Seq[Option[String]]) {
  require(!AVM.keys.isEmpty, "AVM key/value setting file must be read beforehand.")

  def kvs = AVM.keys zip values

  //this(ListMap(values.map(v => AVM.v2k(v)) zip values:_*))
  override def toString = kvs.collect { case (a, Some(b)) => a+"="+b }.mkString(",")
}

object AVM {
  private val keys = new ArrayBuffer[String]
  private val v2keyIdx = new HashMap[String, Int]
  private def initValues = Array.fill[Option[String]](keys.size)(None)

  def empty = AVM(initValues)
  def createFromValues(values:Seq[String]) = {
    val vs = initValues
    values.foreach { v => AVM.v2keyIdx(v) match { case i => vs(i) = Some(v) } }
    AVM(vs)
  }
  def readK2V(path:String):Unit = {
    keys.clear
    v2keyIdx.clear
    Source.fromFile(path).getLines.zipWithIndex.foreach {
      case (line, i) => line.split("\t") match { 
        case a if a.size == 2 => (a(0), a(1)) match {
          case (k, vs) => {
            keys += k
            vs.trim.split("\\s+").foreach { v2keyIdx += _ -> i }
          }
        }
        case _ => throw new RuntimeException("fail to parse the AVM setting file at line " + i + ": " + line)
      }
    }
  }
}
