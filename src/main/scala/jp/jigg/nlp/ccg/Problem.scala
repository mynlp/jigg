package jp.jigg.nlp.ccg

import java.io.File
import scala.collection.mutable.HashMap

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
}
