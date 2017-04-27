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

import scala.collection.mutable.ArrayBuffer
import scala.sys.process.Process

import java.io.{File, FileWriter}


object CCGBank2EnjuXML {

  case class Opts(
    @Help(text="Path to CCGBank file") ccgBank: File = new File(""),
    @Help(text="Path to output (xml)") output: File = new File(""),
    @Help(text="Number of sentences") numSentences: Int = 50
  )

  def main(args:Array[String]) = {
    val opts = CommandLineParser.readIn[Opts](args)

    val dict = new JapaneseDictionary(new Word2CategoryDictionary)

    val conv = new JapaneseParseTreeConverter(dict)

    val reader = new CCGBankReader(dict)

    val instances: Seq[(TaggedSentence, Derivation)] =
      reader.takeLines(IOUtil.openIterator(opts.ccgBank.getPath), opts.numSentences).toSeq.map { line =>
        val trees = reader.readParseFragments(line).map { conv.toLabelTree(_) }
        (conv.toSentenceFromLabelTrees(trees), conv.toFragmentalDerivation(trees))
      }

    val fw = new FileWriter(opts.output.getPath)

    instances.zipWithIndex foreach { case ((s, d), i) => fw.write(d.renderEnjuXML(s, i) + "\n") }

    fw.flush
    fw.close
  }
}
