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
import jigg.util.IOUtil

import breeze.config.{CommandLineParser, Help}

import java.io.{File, FileWriter}

/** Input: CCGBank file (e.g., train.ccgbank) from stdin.
  * Output: Gold bunsetsu dependencies according to the CCGBank in CoNLL format.
  */
object GoldBunsetsuDepInCoNLL {

  case class Opts(
    @Help(text="Path to Cabocha file (same sentences with the CCGBank file)") cabocha: File = new File("")
  )

  def main(args:Array[String]) = {
    val opts = CommandLineParser.readIn[Opts](args)

    val dict = new JapaneseDictionary(new Word2CategoryDictionary)

    val conv = new JapaneseParseTreeConverter(dict)
    val parseTrees = new CCGBankReader(dict)
      .readParseTrees(IOUtil.openStandardIterator, -1, true)
      .map(conv.toLabelTree _).toSeq
    val goldDerivs = parseTrees.map(conv.toDerivation)
    val sentences = parseTrees.map(conv.toSentenceFromLabelTree)

    val bunsetsuSentencesWithPredHead =
      new CabochaReader(sentences).readSentences(opts.cabocha.getPath)

    val bunsetsuSentencesWithGoldHead =
      bunsetsuSentencesWithPredHead zip goldDerivs map { case (sentence, deriv) =>
        BunsetsuSentence(sentence.bunsetsuSeq).parseWithCCGDerivation(deriv)
      }
    for (sentence <- bunsetsuSentencesWithGoldHead) {
      println(sentence.renderInCoNLL)
    }
  }
}
