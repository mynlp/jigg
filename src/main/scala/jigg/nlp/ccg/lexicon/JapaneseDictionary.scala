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

// trait JapanesePoSDictionary {
//   private val conjugationManager = new NumberedManager[Conjugation] {
//     def createInstance(newId:Int, str:String) = Conjugation(newId, str)
//   }
//   private val fineTagManager = new NumberedManager[FineTag] {
//     def createInstance(newId:Int, str:String) = FineTag(newId, str)
//   }
//   protected val posManager: NumberedManager[PoS] = new NumberedManager[PoS] {
//     def getFineTag(str:String) = posManager.getOrCreate(str, { (newId, str) => FineTag(newId, str) })
//     def getConjugation(str:String) = posManager.getOrCreate(str, { (newId, str) => Conjugation(newId, str) })
//     def getFineWithConjugation(str:String) = posManager.getOrCreate(str, { (newId, str) => FineWithConjugation(newId, str) })

//     def createInstance(newId:Int, str:String) = {
//       def getHierarPoS(hierarStr:String, conjStr:String): (Array[PoS], Array[PoS]) = {
//         val splitIdxs = (hierarStr.zipWithIndex.withFilter {
//           case (c, i) => c == '-' }.map { case (c, i) => i }) ++ (hierarStr.size :: Nil)
//         val fineTagStrs:Array[String] = splitIdxs.map { i => hierarStr.substring(0, i) }.toArray
//         val fineTagWithConjStrs = fineTagStrs.map { _ + "+" + conjStr }
//         val fineTags:Array[PoS] = fineTagStrs.map { getFineTag(_) }
//         val fineConjTags:Array[PoS] = fineTagWithConjStrs.map { getFineWithConjugation(_) }
//         (fineTags, fineConjTags)
//       }
//       def create(conjStr:String, hierarStr:String) = getHierarPoS(hierarStr, conjStr) match {
//         case (hierar, hierarConj) => JapanesePoS(newId, str, getConjugation(conjStr), hierar, hierarConj)
//       }
//       str.split('/') match {
//         case a if a.size == 2 => create(a(1), a(0))
//         case _ => create("_", str) // we accept this case by reason of BOS PoS case; throw new RuntimeException("invalid Japanese PoS sequence: " + str)
//       }
//     }
//   }
//   // def getConjugation(str:String):Conjugation = conjugationManager.getOrCreate(str)
//   // def getConjugation(id:Int):Conjugation = conjugationManager(id)
//   def getFineTag(str:String):FineTag = fineTagManager.getOrCreate(str)
//   def getFineTag(id:Int):FineTag = fineTagManager(id)
// }

class JapaneseDictionary(categoryDictionary:CategoryDictionary = new Word2CategoryDictionary) extends Dictionary(categoryDictionary) {
  override val posManager = new JapanesePoSManager

  def readLexicon(lexiconPath:String, templatePath:String, unkType:String = "@UNK@") = {
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
            val candidates = categories.map { c => getCategoryOrCreate(templateMap(c)) }.toArray
            val wordStr = wordPos.substring(0, sep)
            val word = getWordOrCreate(wordStr)
            val pos = getPoSOrCreate(wordPos.substring(sep+1))
            if (wordStr == unkType) categoryDictionary.registUnkCandiates(pos, candidates)
            else categoryDictionary.registCandidates(word, pos, candidates)
        }
        case _ => sys.error("fail to parse lexicon file at line : " + line)
      }
    }
  }
}
