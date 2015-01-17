package jp.jigg.nlp.ccg.lexicon

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class BunsetsuTest extends FunSuite {
  test("A gold derivation with cabocha bunsetsu-segments recover gold dependencies") {
    import jp.jigg.nlp.ccg.parser.ParsedSentences
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
