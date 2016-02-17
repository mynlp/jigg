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

import java.io.FileWriter
import scala.collection.mutable.ArrayBuffer
import scala.sys.process.Process
import fig.exec.Execution
import lexicon._

/** Creates Cabocha-formatted CCGBank sentences.
  *
  * The output of this is required when evaluating bunsetsu-dependency of CCG parser.
  * When new CCGBank is released, currently, we have to manually run this class to get the correct data.
  */
object CCGBankToMecabFormat {

  object Opt extends Options {
    @Option(gloss="Path to CCGBank file", required=true) var ccgBankPath = ""
    @Option(gloss="Path to output") var outputPath = ""
  }

  def main(args:Array[String]) = {
    val runner = new Runner
    Execution.run(args, runner, Opt)
  }

  type Tree = ParseTree[NodeLabel]

  class Runner extends JapaneseSuperTagging with Runnable {

    def run = {
      dict = newDictionary
      val trees = readParseTreesFromCCGBank(Opt.ccgBankPath, -1, true)
      val rawString = trees map { parseTreeConverter.toSentenceFromLabelTree(_) } map { _.wordSeq.mkString("") } mkString("\n")
      val is = new java.io.ByteArrayInputStream(rawString.getBytes("UTF-8"))
      val out = (Process("cabocha -f1") #< is).lineStream_!

      val os = jigg.util.IOUtil.openOut(Opt.outputPath)
      out foreach { line =>
        os.write(line + "\n")
      }
      os.flush
      os.close
    }
  }
}
