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

import scala.reflect.ClassTag

import java.io.File

trait CCGBank {

  val info: Opts.BankInfo

  def train: File
  def dev: File
  def test: File
  def template: File
  def lexicon: File

  def trainSentences: Array[GoldSuperTaggedSentence] =
    extractor.readSentences(train, info.trainSize, true)

  def devSentences: Array[GoldSuperTaggedSentence] =
    extractor.readSentences(dev, info.devSize, false)

  def testSentences: Array[GoldSuperTaggedSentence] =
    extractor.readSentences(test, info.testSize, false)

  def trainTrees: Array[ParseTree[NodeLabel]] =
    extractor.readTrees(train, info.trainSize, true)

  def devTrees: Array[ParseTree[NodeLabel]] =
    extractor.readTrees(dev, info.devSize, false)

  def testTrees: Array[ParseTree[NodeLabel]] =
    extractor.readTrees(test, info.testSize, false)

  def sentences(trees: Array[ParseTree[NodeLabel]]): Array[GoldSuperTaggedSentence] =
    extractor.sentences(trees)

  def derivations(trees: Array[ParseTree[NodeLabel]]): Array[Derivation] =
    extractor.derivations(trees)

  protected def extractor: TreeExtractor
}

object CCGBank {
  def select(info: Opts.BankInfo, dict: Dictionary) = info.lang match {
    case "ja" => new JapaneseCCGBank(info, dict)
    case _ => sys.error("yet unsupported.")
  }
}

class JapaneseCCGBank(val info: Opts.BankInfo, dict: Dictionary) extends CCGBank {

  def train = new File(info.dir, "train.ccgbank")
  def dev = new File(info.dir, "devel.ccgbank")
  def test = new File(info.dir, "test.ccgbank")
  def template = new File(info.dir, "template.lst")
  def lexicon = new File(info.dir, "Japanese.lexicon")

  val extractor = TreeExtractor(
    new JapaneseParseTreeConverter(dict),
    new CCGBankReader(dict))
}

case class TreeExtractor(treeConv: ParseTreeConverter, reader: CCGBankReader) {

  def readSentences(f: File, size: Int, isTrain: Boolean = false):
      Array[GoldSuperTaggedSentence] =
    readAndConvertTrees(f, size, treeConv.toSentenceFromStringTree _, isTrain)

  def readTrees(f: File, size: Int, isTrain: Boolean = false):
      Array[ParseTree[NodeLabel]] =
    readAndConvertTrees(f, size, treeConv.toLabelTree _, isTrain)

  def readAndConvertTrees[A:ClassTag](
    f: File, size: Int, conv: ParseTree[String]=>A, isTrain: Boolean = false): Array[A] =
    reader.readParseTrees(f.getPath, size, isTrain).map(conv).toArray

  def sentences(trees: Array[ParseTree[NodeLabel]]): Array[GoldSuperTaggedSentence] =
    trees map (treeConv.toSentenceFromLabelTree)

  def derivations(trees: Array[ParseTree[NodeLabel]]): Array[Derivation] =
    trees map (treeConv.toDerivation)

}
