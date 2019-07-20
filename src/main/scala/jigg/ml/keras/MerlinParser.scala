package jigg.ml.keras.Merlin

import breeze.linalg.argmax
import jigg.ml.keras._
import jigg.util.HDF5Object

import scala.xml.Node
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class MerlinParser(modelPath: String) {

  private val model = MerlinModel(modelPath)
}

object MerlinParser{
  def main(args:Array[String]): Unit = {
    var modelPatht = args(0)
    var r = new MerlinParser(modelPatht)
  }
}
