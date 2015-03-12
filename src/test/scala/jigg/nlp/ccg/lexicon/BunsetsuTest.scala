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

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class BunsetsuTest extends FunSuite {
  test("A gold derivation with cabocha bunsetsu-segments recover gold dependencies") {
    import jigg.nlp.ccg.parser.ParsedSentences
    val parsedSentences = new ParsedSentences
    val (sentence, derivation) = parsedSentences.simpleSentenceAndDerivation

    val bunsetsuSentence = BunsetsuSentence(Array(
      Bunsetsu(0, sentence.wordSeq.slice(0, 2), sentence.posSeq.slice(0, 2)), // 政権 に
      Bunsetsu(2, sentence.wordSeq.slice(2, 4), sentence.posSeq.slice(2, 4)), // 影響 を
      Bunsetsu(4, sentence.wordSeq.slice(4, 5), sentence.posSeq.slice(4, 5)), // 及ぼす
      Bunsetsu(5, sentence.wordSeq.slice(5, 6), sentence.posSeq.slice(5, 6)))) // こと

    val parsed = bunsetsuSentence.parseWithCCGDerivation(derivation)
    parsed.headSeq should equal (Seq(2, 2, 3, -1))
  }
}
