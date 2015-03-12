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

import scala.collection.mutable.ArrayBuffer
import fig.exec.Execution
import lexicon._

object Cabocha2CoNLL {

  object Opt extends Options {
    @Option(gloss="Path to CCGBank file", required=true) var ccgBankPath = ""
    @Option(gloss="Path to Cabocha file (same sentences with the CCGBank file)", required=true) var cabochaPath = ""
    @Option(gloss="Path to output in CoNLL format") var outputPath = ""
  }

  def main(args:Array[String]) = {
    val runner = new Runner
    Execution.run(args, runner, Opt)
  }

  class Runner extends Runnable {
    def run = {
      val dict = new JapaneseDictionary(new Word2CategoryDictionary)

      val conv = new JapaneseParseTreeConverter(dict)
      val parseTrees = new CCGBankReader(dict).readParseTrees(Opt.ccgBankPath, -1, true).map { conv.toLabelTree(_) }.toSeq

      val sentences = parseTrees map { conv.toSentenceFromLabelTree(_) }

      val bunsetsuSentences = new CabochaReader(sentences).readSentences(Opt.cabochaPath)

      val fw = new java.io.FileWriter(Opt.outputPath)
      bunsetsuSentences.foreach { sent => fw.write(sent.renderInCoNLL + "\n") }
      fw.flush
      fw.close
    }
  }
}
