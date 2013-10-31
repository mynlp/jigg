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

  def readSentenceAndDerivations(path:String):Array[(GoldSuperTaggedSentence, Derivation)] = Source.fromFile(path).getLines.map { readSentenceAndDerivation(_) }.toArray
  def readSentences(path:String):Array[GoldSuperTaggedSentence] = Source.fromFile(path).getLines.map { readSentence(_) }.toArray
  
  def readSentenceAndDerivation(line:String):(GoldSuperTaggedSentence, Derivation) = {
    throw new UnsupportedOperationException
  }
  def readSentence(line:String):GoldSuperTaggedSentence = {
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

    val signs:Array[(Word, Word, PoS, Category)] = line.zipWithIndex.collect {
      case ('{', i) if !notStartLexicalSign(i) => getLexicalSign(line.substring(i+1, line.indexOf('}', i+1)))
    }.toArray
    new GoldSuperTaggedSentence(signs.map(_._1), signs.map(_._2), signs.map(_._3), signs.map(_._4))
  }

  private def getLexicalSign(str:String): (Word, Word, PoS, Category) = str.split(" ") match {
    case a if a.size == 2 => (a(0), a(1)) match {
      case (categoryStr, wordPosStr) => {
        val sep1 = wordPosStr.indexOf('/')
        val sep2 = wordPosStr.indexOf('/', sep1 + 1)

        val surfaceForm = dict.getWord(wordPosStr.substring(0, sep1))
        val baseForm = dict.getWord(wordPosStr.substring(sep1+1, sep2))
        val pos = dict.getPoS(wordPosStr.substring(sep2 + 1))

        val category = dict.getCategory(categoryStr)
        (surfaceForm, baseForm, pos, category)
      }
    }
    case _ => throw new RuntimeException("invalid form: " + str)
  }
}


