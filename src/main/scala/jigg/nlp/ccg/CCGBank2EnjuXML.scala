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

object CCGBank2EnjuXML {

  object Opt extends Options {
    @Option(gloss="Path to CCGBank file", required=true) var ccgBankPath = ""
    @Option(gloss="Path to output (pdf)") var outputPath = ""
    @Option(gloss="Number of sentences") var numSentences = 50
  }

  def main(args:Array[String]) = {
    val runner = new Runner
    Execution.run(args, runner, Opt)
  }

  type Tree = ParseTree[NodeLabel]

  class Runner extends Runnable {

    def run = {
      val dict = new JapaneseDictionary(new Word2CategoryDictionary)

      val conv = new JapaneseParseTreeConverter(dict)

      val reader = new CCGBankReader(dict)

      val instances: Seq[(TaggedSentence, Derivation)] =
        reader.takeLines(Opt.ccgBankPath, Opt.numSentences).toSeq.map { line =>
          val trees = reader.readParseFragments(line).map { conv.toLabelTree(_) }
          (conv.toSentenceFromLabelTrees(trees), conv.toFragmentalDerivation(trees))
        }

      val fw = new FileWriter(Opt.outputPath)

      instances.zipWithIndex foreach { case ((s, d), i) => fw.write(d.renderEnjuXML(s, i) + "\n") }

      fw.flush
      fw.close
    }
  }
}
