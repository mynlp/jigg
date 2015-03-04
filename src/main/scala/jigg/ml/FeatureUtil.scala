// package jigg.ml

// import scala.collection.mutable.{Map => mMap}
// import scala.collection.mutable.AnyRefMap

// trait FeatureUtil[Feature <: AnyRef] {
//   type FeatureIndexer = AnyRefMap[Feature, Int]

//   def getIndex(indexer: FeatureIndexer, key: Feature) = indexer.getOrElseUpdate(key, indexer.size)

//   def removeIndexes(indexer: FeatureIndexer, idxs: Seq[Int]): Unit = {
//     val features = indexer.toSeq.sortWith(_._2 < _._2).map(_._1)
//     val originalSize = indexer.size
//     (0 to idxs.size) foreach { i =>
//       val idx = if (i == idxs.size) originalSize else idxs(i)
//       val lastIdx = if (i == 0) -1 else idxs(i - 1)
//       (lastIdx + 1 until idx) foreach { f => indexer(features(f)) -= i }
//       if (i != idxs.size) indexer -= features(idx)
//     }
//   }
//   def removeElemsOver(indexer: FeatureIndexer, lastIdx: Int) = indexer.toSeq.foreach {
//     case (feature, idx) =>
//       indexer -= feature
//   }
// }

// // example usage:
// object FeatureUtilExample {
//   case class MyFeature(unlabeled: String, label: Int)
//   object FU extends FeatureUtil[MyFeature]

//   def run = {
//     val indexer = new FU.FeatureIndexer
//     FU.getIndex(indexer, MyFeature("hoge", 10))
//   }
// }
