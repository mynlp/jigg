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

class CabochaReader[S<:TaggedSentence](ccgSentences: Seq[S]) {
  def readSentences(path: String): Seq[ParsedBunsetsuSentence] = {
    val bunsetsuStart = """\* (\d+) (-?\d+)[A-Z]""".r
    def addBunsetsuTo(curSent: List[(String, Int)], curBunsetsu: List[String]) = curBunsetsu.reverse match {
      case Nil => curSent
      case headIdx :: tail => (tail.mkString(""), headIdx.toInt) :: curSent
    }

    val bunsetsuSegedSentences: List[List[(String, Int)]] =
      scala.io.Source.fromFile(path).getLines.filter(_ != "").foldLeft(
        (List[List[(String, Int)]](), List[(String, Int)](), List[String]())) {
        case ((processed, curSent, curBunsetsu), line) => line match {
          case bunsetsuStart(_, nextHeadIdx) =>
            (processed, addBunsetsuTo(curSent, curBunsetsu), nextHeadIdx :: Nil) // use first elem as the head idx
          case "EOS" => (addBunsetsuTo(curSent, curBunsetsu).reverse :: processed, Nil, Nil)
          case word => (processed, curSent, word.split("\t")(0) :: curBunsetsu)
        }
      }._1.reverse

    ccgSentences.zip(bunsetsuSegedSentences).map { case (ccgSentence, bunsetsuSentence) =>
      val bunsetsuSegCharIdxs: List[Int] = bunsetsuSentence.map { _._1.size }.scanLeft(0)(_+_).tail // 5 10 ...
      val ccgWordSegCharIdxs: List[Int] = ccgSentence.wordSeq.toList.map { _.v.size }.scanLeft(0)(_+_).tail // 2 5 7 10 ...

      assert(bunsetsuSegCharIdxs.last == ccgWordSegCharIdxs.last)
      val bunsetsuSegWordIdxs: List[Int] = ccgWordSegCharIdxs.zipWithIndex.foldLeft((List[Int](), 0)) { // 1 3 ...
        case ((segWordIdxs, curBunsetsuIdx), (wordIdx, i)) =>
          if (wordIdx >= bunsetsuSegCharIdxs(curBunsetsuIdx)) (i :: segWordIdxs, curBunsetsuIdx + 1)
          else (segWordIdxs, curBunsetsuIdx) // wait until wordIdx exceeds the next bunsetsu segment
      }._1.reverse
      val bunsetsuSeq = bunsetsuSegWordIdxs.zip(-1 :: bunsetsuSegWordIdxs).map { case (bunsetsuIdx, prevIdx) =>
        val offset = prevIdx + 1
        Bunsetsu(offset,
          ccgSentence.wordSeq.slice(offset, bunsetsuIdx + 1),
          ccgSentence.posSeq.slice(offset, bunsetsuIdx + 1))
      }
      ParsedBunsetsuSentence(bunsetsuSeq, bunsetsuSentence.map { _._2 })
    }
  }
}
