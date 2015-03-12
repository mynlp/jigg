package jigg.nlp.ccg.lexicon

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

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

/** Read the output of mecab with -Ochasen option.
  */
class MecabReader(dict:Dictionary) {
  def toPoSTaggedSentence(lines:Seq[String]) = {
    val terminalSeq = lines.map { line =>
      val splitted = line.split('\t')
      val word = dict.getWordOrCreate(splitted(0))
      val base = dict.getWordOrCreate(splitted(2))

      val conjStr = if (splitted.size > 6) splitted(5) else "_"
      val posStr = splitted(3) + "/" + conjStr

      val pos = dict.getPoSOrCreate(posStr)
      (word, base, pos)
    }
    new PoSTaggedSentence(
      terminalSeq.map(_._1),
      terminalSeq.map(_._2),
      terminalSeq.map(_._3))
  }
  def readSentences(in:Source, n:Int): Array[PoSTaggedSentence] = {
    val sentences = new ArrayBuffer[PoSTaggedSentence]

    val sentenceLines = new ArrayBuffer[String]

    takeLines(in, n).foreach { _ match {
      case "EOS" =>
        sentences += toPoSTaggedSentence(sentenceLines)
        sentenceLines.clear
      case line =>
        sentenceLines += line
    }}
    sentences.toArray
  }
  def readSentences(path:String, n:Int): Array[PoSTaggedSentence] =
    readSentences(Source.fromFile(path), n)
  def takeLines(in:Source, n:Int): Iterator[String] =
    for (line <- in.getLines.filter(_!="") match {
      case lines if (n == -1) => lines
      case lines => lines.take(n) }) yield line

}
