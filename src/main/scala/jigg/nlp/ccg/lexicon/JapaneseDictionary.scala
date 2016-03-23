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

import scala.collection.mutable.HashMap
import scala.io.Source

class JapanesePoSManager extends PoSManager {
  override val unknown = getOrCreate("UNK_POS/UNK_CONJUGATION")

  override def createWithId(original:PoS): PoS = original match {
    case JapanesePoS(id, v, conj, hierar, hierarConj) =>
      val withIdConj = assignID(conj)
      val withIdHierar = hierar.map { assignID(_) }
      val withIdHierarConj = hierarConj.map { assignID(_) }
      JapanesePoS(newId, v, withIdConj, withIdHierar, withIdHierarConj)
    case Conjugation(id, v) => Conjugation(newId, v)
    case FineTag(id, v) => FineTag(newId, v)
    case FineWithConjugation(id, v) => FineWithConjugation(newId, v)
    case _ => sys.error("A strange object is found in JapanesePoS:" + original)
  }
  override def createCanonicalInstance(str:String): PoS = {
    def canonicalJapanesePoS(hierarStr:String, conjStr:String) = {
      val hyphenIdxs = hierarStr.indices.filter(hierarStr(_) == '-')
      val splitIdxs = hyphenIdxs :+ hierarStr.size // full surface is also a fineTag
      val fineTagStrs = splitIdxs.map { i => hierarStr.substring(0, i) }.toArray
      val fineTagWithConjStrs = fineTagStrs.map { _ + "+" + conjStr }.toArray

      val fineTags:Array[PoS] = fineTagStrs.map { FineTag(0, _) }
      val fineConjTags:Array[PoS] = fineTagWithConjStrs.map { FineWithConjugation(0, _) }
      val conj = Conjugation(0, conjStr)
      JapanesePoS(0, str, conj, fineTags, fineConjTags)
    }
    str.split('/') match {
      case a if a.size == 2 => canonicalJapanesePoS(a(0), a(1))
      case _ => canonicalJapanesePoS(str, "_")
    }
  }
}

class JapaneseDictionary(categoryDictionary:CategoryDictionary = new Word2CategoryDictionary) extends Dictionary(categoryDictionary) {
  override val posManager = new JapanesePoSManager
}

object JapaneseDictionary {

  def setCategoryDictionaryFromLexicon(
    dict: Dictionary,
    lexiconPath: String,
    templatePath: String,
    unkEntry: String = "@UNK@") = {

    import dict._

    val templateMap = Source.fromFile(templatePath).getLines.map {
      line => line.split("\t") match {
        case a if a.size == 2 => a(0) -> a(1)
        case _ => sys.error("fail to parse template file at line : " + line)
      }
    }.toMap

    Source.fromFile(lexiconPath).getLines.foreach {
      line => line.trim.split("\\s") match {
        case a if a.size >= 2 => (a(0), a.drop(1)) match {
          case (wordPos, categories) =>
            val sep = wordPos.indexOf("/")
            val candidates = categories.map {
              c => getCategoryOrCreate(templateMap(c))
            }.toArray
            val wordStr = wordPos.substring(0, sep)
            val word = getWordOrCreate(wordStr)
            val pos = getPoSOrCreate(wordPos.substring(sep+1))
            if (wordStr == unkEntry)
              categoryDictionary.registUnkCandiates(pos, candidates)
            else categoryDictionary.registCandidates(word, pos, candidates)
        }
        case _ => sys.error("fail to parse lexicon file at line : " + line)
      }
    }

  }

}
