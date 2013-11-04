package enju.ccg

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
  protected def developPath = pathWithBankDirPathAsDefault(InputOptions.testPath, "develop.ccgbank")
  
}
