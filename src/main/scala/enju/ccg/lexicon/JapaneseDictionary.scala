package enju.ccg.lexicon
import scala.collection.mutable.HashMap
import scala.io.Source

trait JapanesePoSDictionary {
  private val conjugationManager = new NumberedManager[Conjugation] {
    def createInstance(newId:Int, str:String) = Conjugation(newId, str)
  }
  private val fineTagManager = new NumberedManager[FineTag] {
    def createInstance(newId:Int, str:String) = FineTag(newId, str)
  }
  protected val posManager = new NumberedManager[PoS] {
    def createInstance(newId:Int, str:String) = str.split('/') match { 
      case a if a.size == 2 => (a(0), a(1)) match {
        case (hierarStr, conjStr) => {
          val splitIdxs = hierarStr.zipWithIndex.withFilter { 
            case (c, i) => c == '-' }.map { case (c, i) => i }.toArray
          val fineTags = new Array[FineTag](splitIdxs.size + 1)
          0 until splitIdxs.size foreach {
            i => fineTags(i) = getFineTag(hierarStr.substring(0, splitIdxs(i))) 
          }
          fineTags(fineTags.size - 1) = getFineTag(hierarStr)
          val conj = getConjugation(conjStr)
          JapanesePoS(newId, str, conj, fineTags)
        }
      }
      case _ => throw new RuntimeException("invalid Japanese PoS sequence: " + str)
    }
  }
  def getConjugation(str:String):Conjugation = conjugationManager.getOrCreate(str)
  def getConjugation(id:Int):Conjugation = conjugationManager(id)
  def getFineTag(str:String):FineTag = fineTagManager.getOrCreate(str)
  def getFineTag(id:Int):FineTag = fineTagManager(id)
}

class JapaneseDictionary(categoryDictionary:CategoryDictionary = new Word2CategoryDictionary) extends Dictionary(categoryDictionary) with JapanesePoSDictionary {
  def readLexicon(lexiconPath:String, templatePath:String, unkType:String = "@UNK@") = {
    val templateMap = Source.fromFile(templatePath).getLines.map {
      line => line.split("\t") match {
        case a if a.size == 2 => a(0) -> a(1)
        case _ => throw new RuntimeException("fail to parse template file at line : " + line)
      }
    }.toMap
    Source.fromFile(lexiconPath).getLines.foreach {
      line => line.trim.split("\\s") match {
        case a if a.size >= 2 => (a(0), a.drop(1)) match {
          case (wordPos, categories) => {
            val sep = wordPos.indexOf("/")
            val candidates = categories.map { c => getCategory(templateMap(c)) }.toArray
            val wordStr = wordPos.substring(0, sep)
            val word = getWord(wordStr)
            val pos = getPoS(wordPos.substring(sep+1))
            if (wordStr == unkType) categoryDictionary.registUnkCandiates(pos, candidates)
            else categoryDictionary.registCandidates(word, pos, candidates)
          }
        }
        case _ => throw new RuntimeException("fail to parse lexicon file at line : " + line)
      }
    }
  }
}
