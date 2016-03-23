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

import lexicon._

import breeze.config.{CommandLineParser, Help}

import java.io.{File, FileWriter}

object Cabocha2CoNLL {

  case class Opts(
    @Help(text="Path to CCGBank file") ccgBank: File = new File(""),
    @Help(text="Path to Cabocha file (same sentences with the CCGBank file)") cabocha: File = new File(""),
    @Help(text="Path to output in CoNLL format") output: File = new File("")
  )

  def main(args:Array[String]) = {
    val opts = CommandLineParser.readIn[Opts](args)

    val dict = new JapaneseDictionary(new Word2CategoryDictionary)

    val conv = new JapaneseParseTreeConverter(dict)
    val parseTrees = new CCGBankReader(dict).readParseTrees(opts.ccgBank.getPath, -1, true).map {
      conv.toLabelTree(_)
    }.toSeq

    val sentences = parseTrees map { conv.toSentenceFromLabelTree(_) }

    val bunsetsuSentences = new CabochaReader(sentences).readSentences(opts.cabocha.getPath)

    val fw = new FileWriter(opts.output.getPath)
    bunsetsuSentences.foreach { sent => fw.write(sent.renderInCoNLL + "\n") }
    fw.flush
    fw.close
  }
}
