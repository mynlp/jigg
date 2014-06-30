package enju.ccg

import java.io.File
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
  protected def developPath = pathWithBankDirPathAsDefault(InputOptions.developPath, "devel.ccgbank")

  def prepareDirectoryOutput(path: String) = new File(path) match {
    case d if d.exists && d.isFile =>
      sys.error("We cannot create a directory " + path + "; there is another file in that path!")
    case d => d.mkdirs match {
      case true => // ok, success
      case false =>
        System.err.println("Directory " + path + " already exits; we override the contents.")
    }
  }

  object Problem {
    def removeZeroWeightFeatures[F](indexer: FeatureIndexer[F], weightsList: NumericBuffer[Float]*): Unit = {
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
