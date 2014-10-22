package enju.ccg.parser

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class RuleTest extends FunSuite {
  val parsedSentences = new ParsedSentences
  val dict = parsedSentences.dict
  def cat(str:String) = dict.getCategory(str).get

  test("extract all rules from derivations") {
    val (sentence, derivation) = parsedSentences.simpleSentenceAndDerivation

    val rule = CFGRule.extractRulesFromDerivations(Array(derivation), JapaneseHeadFinder)
    rule.unify(cat("(NP[case=nc,mod=X1]{I1}/NP[case=nc,mod=X1]{I1}){I2}"), cat("NP[case=nc,mod=nm]{I1}_none")).get should contain (cat("NP[case=nc,mod=nm]{I1}"), ">")
    rule.raise(cat("S[mod=adn,form=base]{I1}")).get should contain (cat("(NP[case=nc,mod=X1]{I1}/NP[case=nc,mod=X1]{I1}){I2}"), "ADN")
    rule.unify(cat("NP[case=ni,mod=nm]{I1}"), cat("(S[mod=adn,form=base]{I1}\\NP[case=ni,mod=nm]{I2}){I1}")).get should contain (cat("S[mod=adn,form=base]{I1}"), "<")

    rule.unify(cat("NP[case=nc,mod=nm]{I1}_none"), cat("(NP[case=o,mod=nm]{I1}\\NP[case=nc,mod=nm]{I1}){I2}_none")).get should contain (cat("NP[case=o,mod=nm]{I1}"), "<")
  }
}
