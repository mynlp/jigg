package enju.ccg.lexicon

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

  def readSentences(path:String, n:Int): Array[PoSTaggedSentence] = {
    val sentences = new ArrayBuffer[PoSTaggedSentence]

    val sentenceLines = new ArrayBuffer[String]

    takeLines(path, n).foreach { _ match {
      case "EOS" =>
        sentences += toPoSTaggedSentence(sentenceLines)
        sentenceLines.clear
      case line =>
        sentenceLines += line
    }}
    sentences.toArray
  }
  def takeLines(path:String, n:Int): Iterator[String] =
    for (line <- Source.fromFile(path).getLines.filter(_!="") match {
      case lines if (n == -1) => lines
      case lines => lines.take(n) }) yield line

}
