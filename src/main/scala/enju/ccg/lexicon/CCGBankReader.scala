package enju.ccg.lexicon

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

class CCGBankReader(dict:Dictionary) {
  var pos = 0
  var currentLine = ""
  
  case class JapaneseLeafItem(word:Word, base:Word, pos:PoS)

  def japaneseLeafStrToItem(train:Boolean) = (leafStr:String) =>
  getLexicalSign(leafStr, train) match {
    case (word, base, pos, category) => LeafNode(JapaneseLeafItem(word, base, pos), category)
  }
  val str2category = (categoryStr:String) => dict.getCategoryOrCreate(categoryStr)
  def japaneseTreeParser(train:Boolean) = new TreeParser[JapaneseLeafItem](japaneseLeafStrToItem(train), str2category)

  def readSentenceAndDerivations(path:String, n:Int, train:Boolean):(Array[GoldSuperTaggedSentence], Array[Derivation]) = {
    val (a,b) = takeLines(path, n).map { readSentenceAndDerivation(_, train) }.toSeq.unzip
    (a.toArray, b.toArray)
  }

  def readSentencesTrain(path:String, n:Int) = readSentences(path, n, true)
  def readSentencesTest(path:String, n:Int) = readSentences(path, n, false)
  def readSentences(path:String, n:Int, train:Boolean):Array[GoldSuperTaggedSentence] = takeLines(path, n).map { readSentence(_, train) }.toArray
  
  def takeLines(path:String, n:Int) = Source.fromFile(path).getLines.filter(_!="") match {
    case lines if (n == -1) => lines
    case lines => lines.take(n)
  }
  
  def readSentenceAndDerivation(line:String, train:Boolean):(GoldSuperTaggedSentence, Derivation) = {
    type Item = JapaneseLeafItem
    
    val treeParser = japaneseTreeParser(train)
    val parseTrees:Array[ParseTree[Item]] = treeParser.parse(line)
    assert(parseTrees.size == 1, "Something wrong: readSentenceAndDerivation should not read more than one sentence. input: " + line)
    val leafNodes: Seq[LeafNode[Item]] = parseTrees(0).getSequence

    val goldSentence = new GoldSuperTaggedSentence(leafNodes.map(_.info.word),
                                                   leafNodes.map(_.info.base),
                                                   leafNodes.map(_.info.pos),
                                                   leafNodes.map(_.label))
    val derivation = parseTrees(0).toDerivation
    (goldSentence, derivation)
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

    val signs:Array[(Word, Word, PoS, Category)] = line.zipWithIndex.collect {
      case ('{', i) if !notStartLexicalSign(i) => getLexicalSign(
        line.substring(i+1, line.indexOf('}', i+1)), train)
    }.toArray
    new GoldSuperTaggedSentence(signs.map(_._1), signs.map(_._2), signs.map(_._3), signs.map(_._4))
  }

  private def getLexicalSign(str:String, train:Boolean): (Word, Word, PoS, Category) = {
    def getWord(str:String) = if (train) dict.getWordOrCreate(str) else dict.getWord(str)
    def getPoS(str:String) = if (train) dict.getPoSOrCreate(str) else dict.getPoS(str)
    //def getCategory(str:String) = if (train) Some(dict.getCategoryOrCreate(str)) else dict.getCategory(str)
    def getCategory(str:String) = dict.getCategoryOrCreate(str) // NOTE: unknown category is meaning-less; when we meet an unknown category at test time, create the new category. 
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

  // leafParser absorbs differences of leaf node surface structure
  class TreeParser[T](leafParser: String=>ParseTree[T], categoryParser: String=>Category) {
    import java.io._
    def parse(str:String): Array[ParseTree[T]] = collectTrees(new StringReader(str))
    def parseFromPath(path:String): Array[ParseTree[T]] = 
      collectTrees(new BufferedReader(new InputStreamReader(new FileInputStream(path))))

    def collectTrees(in:java.io.Reader): Array[ParseTree[T]] = {
      val nodes = new ArrayBuffer[ParseTree[T]]
      foreachTree(in, { nodes += _ })
      in.close
      nodes.toArray
    }
    def foreachTree(in:java.io.Reader, f:ParseTree[T]=>Unit): Unit = {
      var nextc = -1 // preserve next char
      
      def doRead = in.read
      def peekChar = {
        if (nextc == -1) nextc = doRead
        nextc
      }
      def readChar = if (nextc == -1) doRead else { val c = nextc; nextc = -1; c }
      
      def skipSpaces = {
        var c = peekChar
        while (c != -1 && Character.isWhitespace(c)) {
          readChar
          c = peekChar
        }
      }
      def readString: String = {
        val sb = new StringBuilder
        var c = peekChar
        while (c != -1 && !Character.isWhitespace(c) && c != '{' && c != '}') {
          sb.append(c.toChar)
          readChar
          c = peekChar
        }
        sb.toString
      }
      def readLabel: Category = {
        skipSpaces
        categoryParser(readString)
      }
      def readChildren: Seq[ParseTree[T]] = {
        skipSpaces
        val children = new ArrayBuffer[ParseTree[T]]
        while (!(peekChar == '}')) {
          val tree = readTree match {
            case Some(t) => t
            case None => sys.error("parser error")
          }
          children += tree
          skipSpaces
        }
        children
      }
      def readIntermediateNode: ParseTree[T] = {
        val label = readLabel
        readChildren match {
          case Seq(child) => UnaryTree(child, label) // unary
          case Seq(left, right) => BinaryTree(left, right, label) // binary
          case _ => sys.error("parse error; more than 2 child nodes are found!")
        }
      }

      def isIntermediate(a:String): Boolean = 
        a == "<" || a == ">" || a == "Φ" || a == "ADV" || a == "ADN"

      def readTree: Option[ParseTree[T]] = {
        skipSpaces
        peekChar match {
          case '{' => // new tree
            readChar // '{'
            val ruleSymbol = readString // \{< , \{AVD, etc
            val tree = if (isIntermediate(ruleSymbol)) readIntermediateNode else {
              skipSpaces
              val leafStr = ruleSymbol + ' ' + readString // leaf
              leafParser(leafStr)
            }
            readChar // '}'

            Some(tree)
          case '}' => sys.error("Extract '}' found")
          case -1 => None
        }
      }
      def readTrees: Unit = {
        val tree = readTree
        tree foreach { t => f(t); readTrees }
      }
      readTrees
    }
  }
}
