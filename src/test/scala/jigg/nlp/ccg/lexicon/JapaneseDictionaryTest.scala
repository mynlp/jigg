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

class JapaneseDictionaryTest extends FunSuite {
  def findPath(localPath:String) = getClass.getClassLoader.getResource(localPath).getPath
  def checkDictionary(dict:JapaneseDictionary, wordStr:String, posStr:String, expectedCategories:Seq[String]) {
    val word = dict.getWord(wordStr)
    val pos = dict.getPoS(posStr)
    val candidates = dict.getCategoryCandidates(word, pos)
    //val candidateStrs = candidates.map { c => c.toString }
    candidates.size should equal (expectedCategories.size)

    expectedCategories.foreach { str => dict.getCategory(str).get match {
      case c => candidates.indexOf(c) should not equal (-1)
    }}
  }
  test("create pos with numbered hierar/conj") {
    val dict = new JapaneseDictionary
    val pos1 = dict.getPoSOrCreate("動詞-接尾/仮定形") // dummy for checking the id-assigned process
    val pos2 = dict.getPoSOrCreate("動詞-自立/未然レル接続")
    pos2.v should equal ("動詞-自立/未然レル接続")
    pos2.conj match {
      case Conjugation(id, v) => { v should equal ("未然レル接続") }
      case _ => fail
    }
    // fineTag ids: 動詞->0, 動詞-接尾->1, 動詞-自立->2
    pos2.first should equal (pos1.first)
    pos2.first match {
      case FineTag(id, v) => { v should equal ("動詞") }
      case _ => fail
    }
    pos2.second match {
      case FineTag(id, v) => { v should equal ("動詞-自立") }
      case _ => fail
    }
    val vals = List(pos2.first, pos2.second, pos2.firstWithConj, pos2.secondWithConj, pos2.conj, pos2)
    vals.map { _.id }.distinct.size should equal (6)
  }
  test("categoryDictionary registration test") {
    val dict = new JapaneseDictionary(new WordPoS2CategoryDictionary)
    val lexiconPath = findPath("data/Japanese.small.lexicon")
    val templatePath = findPath("data/template.small.lst")
    dict.readLexicon(lexiconPath, templatePath)
    checkDictionary(dict, "あふれる", "動詞-自立/基本形", Array("S[nm,base]＼NP[ga,nm,ga]", "S[adn,base]＼NP[ga,nm,ga]"))
    checkDictionary(dict, "ふくろう", "副詞-助詞類接続/_", Array("S1／S1", "NP[nc,nm]1／NP[nc,nm]1", "S[nm,stem]"))
  }
  test("category lookup with WordSecondFineTag2... successfully discard information about conjugation.") {
    val dict = new JapaneseDictionary(new WordSecondFineTag2CategoryDictionary)
    val lexiconPath = findPath("data/Japanese.unkVerb.lexicon")
    val templatePath = findPath("data/template.unkVerb.lst") // this is dummy; the lexicon above need no mappings
    dict.readLexicon(lexiconPath, templatePath)

    val candidates = dict.getCategoryCandidates(dict.getWord("みちご"), dict.getPoS("動詞-非自立/連用形"))
  }
}
