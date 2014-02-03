package enju.ccg

import scala.collection.mutable.HashMap
import ml.{NumericBuffer, FeatureIndexer}

trait Problem {
  def train: Unit
  def predict: Unit
  def evaluate: Unit
  def save: Unit

  protected def pathWithBankDirPathAsDefault(fullPath: String, nameInBankDir: String) =
    (InputOptions.bankDirPath, fullPath) match {
      case (dir, "") if dir != "" => dir + "/" + nameInBankDir
      case (_, path) => path
    }
  protected def trainPath = pathWithBankDirPathAsDefault(InputOptions.trainPath, "train.ccgbank")
  protected def developPath = pathWithBankDirPathAsDefault(InputOptions.testPath, "devel.ccgbank")

  object Problem {
    def removeZeroWeightFeatures[F](indexer: FeatureIndexer[F], weights: NumericBuffer[Double]): Unit = {
      if (indexer.size > weights.size) {
        indexer.removeElemsOver(weights.size)
      }
      val oldSize = indexer.size
      val removingIdxs = weights.zipWithIndex.filter(_._1 == 0).map(_._2)
      indexer.removeIndexes(removingIdxs)
      weights.removeIndexes(removingIdxs)

      // if (indexer.size != weights.size) {
      //   println("indexer: " + indexer.size + " != weights: " + weights.size)
      //   println("oldSize: " + oldSize)
      //   println("removingIdxs size: " + removingIdxs.size)
      //   println("so expected size is: " + (oldSize - removingIdxs.size))
      // }
      assert(indexer.size == weights.size)
      println("feature size is reduced from " + oldSize + " -> " + indexer.size)
    }
  }
}
