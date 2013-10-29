package enju.ccg.lexicon

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class JapaneseDictionaryTest extends FunSuite with ShouldMatchers {
  test("create pos with numbered hierar/conj") {
    val dict = new JapaneseDictionary
    val pos1 = dict.getPoS("動詞-接尾/仮定形") // dummy for checking the id-assigned process
    val pos2 = dict.getPoS("動詞-自立/未然レル接続")
    pos2.id should equal (1)
    pos2.v should equal ("動詞-自立/未然レル接続")
    pos2.conj match {
      case Conjugation(id, v) => { id should equal (1); v should equal ("未然レル接続") }
    }
    // fineTag ids: 動詞->0, 動詞-接尾->1, 動詞-自立->2
    pos2.first should equal (pos1.first)
    pos2.first match {
      case FineTag(id, v) => { id should equal (0); v should equal ("動詞") }
    }
    pos2.second match {
      case FineTag(id, v) => { id should equal (2); v should equal ("動詞-自立") }
    }
  }
  test("categoryDictionary registration test") {
    val dict = new JapaneseDictionary(new WordPoS2CategoryDictionary)
    val lexiconPath = findPath("data/Japanese.small.lexicon")
    val templatePath = findPath("data/template.small.lst")
    dict.readLexicon(lexiconPath, templatePath)
    checkDictionary(dict, "あふれる", "動詞-自立/基本形", Array("S[nm,base]＼NP[ga,nm,ga]", "S[adn,base]＼NP[ga,nm,ga]"))
    checkDictionary(dict, "ふくろう", "副詞-助詞類接続/_", Array("S1／S1", "NP[nc,nm]1／NP[nc,nm]1", "S[nm,stem]"))
  }
  def findPath(localPath:String) = getClass.getClassLoader.getResource(localPath).getPath
  def checkDictionary(dict:JapaneseDictionary, wordStr:String, posStr:String, expectedCategories:Seq[String]) {
    val word = dict.getWord(wordStr)
    val pos = dict.getPoS(posStr)
    val candidates = dict.getCategoryCandidates(word, pos)
    //val candidateStrs = candidates.map { c => c.toString }
    candidates.size should equal (expectedCategories.size)
    
    expectedCategories.foreach { str => dict.getCategory(str) match {
      case c => candidates.indexOf(c) should not equal (-1)
    }}
  }
}
