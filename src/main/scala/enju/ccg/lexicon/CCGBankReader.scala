package enju.ccg.lexicon

import scala.io.Source

class CCGBankReader(dict:Dictionary) {
  var pos = 0
  var currentLine = ""
  
  // def readLine(line:String):(GoldSuperTaggedSentence, Derivation) = {
  //   pos = 0
  //   currentLine = line

  // }
  // def getTree(line:String):

  def readSentenceAndDerivations(path:String, n:Int):Array[(GoldSuperTaggedSentence, Derivation)] = takeLines(path, n).map { readSentenceAndDerivation(_) }.toArray

  def readSentencesTrain(path:String, n:Int) = readSentences(path, n, true)
  def readSentencesTest(path:String, n:Int) = readSentences(path, n, false)
  def readSentences(path:String, n:Int, train:Boolean):Array[GoldSuperTaggedSentence] = takeLines(path, n).map { readSentence(_, train) }.toArray
  
  def takeLines(path:String, n:Int) = Source.fromFile(path).getLines match {
    case lines if (n == -1) => lines
    case lines => lines.take(n)
  }
  
  def readSentenceAndDerivation(line:String):(GoldSuperTaggedSentence, Derivation) = {
    throw new UnsupportedOperationException
  }
  def readSentence(line:String, train:Boolean):GoldSuperTaggedSentence = {
    def notStartLexicalSign(i:Int) = line.substring(i, i + 3) match {
      case ("{< ") => true; 
      case ("{> ") => true
      case ("{Φ ") => true
      case _ => line.substring(i, i + 5) match {
        case ("{ADV ") => true
        case ("{ADN ") => true
        case _ => false
      }
    }
    pos = 0
    currentLine = line

    val signs:Array[(Word, Word, PoS, Option[Category])] = line.zipWithIndex.collect {
      case ('{', i) if !notStartLexicalSign(i) => getLexicalSign(
        line.substring(i+1, line.indexOf('}', i+1)), train)
    }.toArray
    new GoldSuperTaggedSentence(signs.map(_._1), signs.map(_._2), signs.map(_._3), signs.map(_._4))
  }

  private def getLexicalSign(str:String, train:Boolean): (Word, Word, PoS, Option[Category]) = {
    def getWord(str:String) = if (train) dict.getWordOrCreate(str) else dict.getWord(str)
    def getPoS(str:String) = if (train) dict.getPoSOrCreate(str) else dict.getPoS(str)
    def getCategory(str:String) = if (train) Some(dict.getCategoryOrCreate(str)) else dict.getCategory(str)
    str.split(" ") match {
      case a if a.size == 2 => (a(0), a(1)) match {
        case (categoryStr, wordPosStr) => {
          val sep1 = wordPosStr.indexOf('/')
          val sep2 = wordPosStr.indexOf('/', sep1 + 1)

          val surfaceForm = getWord(wordPosStr.substring(0, sep1))
          val baseForm = getWord(wordPosStr.substring(sep1+1, sep2))
          val pos = getPoS(wordPosStr.substring(sep2 + 1))

          val category = getCategory(categoryStr)
          (surfaceForm, baseForm, pos, category)
        }
      }
      case _ => throw new RuntimeException("invalid form: " + str)
    }
  }
}


