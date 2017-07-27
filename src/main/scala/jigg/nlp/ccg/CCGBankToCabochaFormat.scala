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

import scala.sys.process.Process

import java.io.{File, FileWriter, ByteArrayInputStream}

/** Creates Cabocha-formatted CCGBank sentences.
  *
  * The output of this is required when evaluating bunsetsu-dependency of CCG parser.
  * When new CCGBank is released, currently, we have to manually run this class to get the correct data.
  */
object CCGBankToCabochaFormat {

  case class Opts(
    @Help(text="Path to CCGBank file") ccgbank: File = new File(""),
    @Help(text="Path to output") output: File = new File(""),
    @Help(text="Cabocha command (path to cabocha)") cabocha: String = "cabocha"
  )

  type Tree = ParseTree[NodeLabel]

  def main(args:Array[String]) = {
    val opts = CommandLineParser.readIn[Opts](args)

    val dict = new JapaneseDictionary()
    val extractors = TreeExtractor(
      new JapaneseParseTreeConverter(dict),
      new CCGBankReader)

    val trees = extractors.readTrees(opts.ccgbank, -1, true)
    val rawString = trees map (extractors.treeConv.toSentenceFromLabelTree) map (_.wordSeq.mkString("")) mkString ("\n")
    val is = new java.io.ByteArrayInputStream(rawString.getBytes("UTF-8"))
    val out = (Process(s"${opts.cabocha} -f1") #< is).lineStream_!

    val os = jigg.util.IOUtil.openOut(opts.output.getPath)
    out foreach { line =>
      os.write(line + "\n")
    }
    os.flush
    os.close
  }
}
