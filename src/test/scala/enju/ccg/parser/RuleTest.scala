package enju.ccg.parser

import org.scalatest.FunSuite
import org.scalatest.Matchers._

class RuleTest extends FunSuite {
  val parsedSentences = new ParsedSentences
  val dict = parsedSentences.dict
  def cat(str:String) = dict.getCategory(str).get
  
  test("extract all rules from derivations") {
    val tree = parsedSentences.simpleParseTree
    val derivation = tree.toDerivation
    
    val rule = CFGRule.extractRulesFromDerivations(Array(derivation), JapaneseHeadFinder)
    rule.unify(cat("NP[nc,nm]1／NP[nc,nm]1"), cat("NP[nc,nm]")).get should contain (cat("NP[nc,nm]"), ">")
    rule.raise(cat("S[adn,base]")).get should contain (cat("NP[nc,nm]1／NP[nc,nm]1"), "ADN")
    rule.unify(cat("NP[ni,nm]"), cat("S[adn,base]＼NP[ni,nm]")).get should contain (cat("S[adn,base]"), "<")
    
    rule.unify(cat("NP[nc,nm]"), cat("NP[o,nm]＼NP[nc,nm]sem")).get should contain (cat("NP[o,nm]"), "<")
  }
}
