package jigg.nlp.ccg

/*
 Copyright 2013-2015 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

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
