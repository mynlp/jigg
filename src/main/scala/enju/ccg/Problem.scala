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
    def removeZeroWeightFeatures[F](indexer: FeatureIndexer[F], weightsList: NumericBuffer[Double]*): Unit = {
      val baseWeights = weightsList(0)
      if (indexer.size > baseWeights.size) {
        indexer.removeElemsOver(baseWeights.size)
      }
      val oldSize = indexer.size
      val removingIdxs = baseWeights.zipWithIndex.filter(_._1 == 0).map(_._2)
      indexer.removeIndexes(removingIdxs)

      weightsList.foreach { weights =>
        weights.removeIndexes(removingIdxs)
        assert(weights.size == indexer.size)
      }
      println("feature size is reduced from " + oldSize + " -> " + indexer.size)
    }
  }
}
