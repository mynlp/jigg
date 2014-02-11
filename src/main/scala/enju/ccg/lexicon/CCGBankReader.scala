package enju.ccg.lexicon

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

class CCGBankReader(dict:Dictionary) {
  type Tree = ParseTree[String]

  var pos = 0
  var currentLine = ""

  def readParseTrees(path: String, n: Int, train: Boolean): Iterator[Tree] =
    for (line <- takeLines(path, n)) yield readParseTree(line, train)

  def takeLines(path:String, n:Int): Iterator[String] =
    for (line <- Source.fromFile(path).getLines.filter(_!="") match {
      case lines if (n == -1) => lines
      case lines => lines.take(n) }) yield line

  def readParseTree(line: String, train: Boolean): Tree = {
    val parser = new TreeParser
    val parses = parser.parse(line)
    assert(parses.size == 1)
    parses(0)
  }

  class TreeParser {
    import java.io._
    def parse(str:String): Array[Tree] = collectTrees(new StringReader(str))
    def parseFromPath(path:String): Array[Tree] =
      collectTrees(new BufferedReader(new InputStreamReader(new FileInputStream(path))))

    def collectTrees(in:java.io.Reader): Array[Tree] = {
      val nodes = new ArrayBuffer[Tree]
      val treeReader = newTreeReader(in)

      foreachTree(treeReader, { nodes += _ })
      in.close
      nodes.toArray
    }
    def foreachTree(treeReader: TreeReader, f:Tree => Unit): Unit = {
      def readTrees: Unit = {
        val tree = treeReader.readTree
        tree foreach { t => f(t); readTrees }
      }
      readTrees
    }
  }

  trait TreeReader {
    val in: java.io.Reader
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
    def readTree: Option[Tree]
  }

  class ATreeReader(override val in: java.io.Reader) extends TreeReader {
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

    def readChildren: Seq[Tree] = {
      skipSpaces
      val children = new ArrayBuffer[Tree]
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
    def readIntermediateNode(ruleSymbol:String): Tree = {
      skipSpaces
      val label = ruleSymbol + ' ' + readString
      readChildren match {
        case Seq(child) => UnaryTree(child, label) // unary
        case Seq(left, right) => BinaryTree(left, right, label) // binary
        case _ => sys.error("parse error; more than 2 child nodes are found!")
      }
    }

    def isIntermediate(a:String): Boolean =
      a == "<" || a == ">" || a == "Φ" || a == "ADV" || a == "ADN"

    def readTree: Option[Tree] = {
      skipSpaces
      peekChar match {
        case '{' => // new tree
          readChar // '{'
          val ruleSymbol = readString // {< , {AVD, etc
          val tree = if (isIntermediate(ruleSymbol)) readIntermediateNode(ruleSymbol) else {
            skipSpaces
            LeafTree(ruleSymbol + ' ' + readString)
          }
          readChar // '}'

          Some(tree)
        case '}' => sys.error("Extract '}' found")
        case -1 => None
      }
    }
  }
  def newTreeReader(in: java.io.Reader): TreeReader = new ATreeReader(in)
}

class EnglishCCGBankReader(dict:Dictionary) extends CCGBankReader(dict) {
  class EnglishTreeReader(override val in: java.io.Reader) extends TreeReader {
    def readLabel: String = {
      val sb = new StringBuilder
      readChar // <
      var c = peekChar
      //assert(c.toChar == '<')
      while (c != -1 && c != '>') {
        sb.append(c.toChar)
        readChar
        c = peekChar
      }
      readChar
      sb.toString
    }
    def removeBracket(label: String) = {
      assert(label(0) == '<' && label.last == '>')
      label.slice(1, label.size - 1)
    }

    def readChildren: Seq[Tree] = {
      skipSpaces
      val children = new ArrayBuffer[Tree]
      while (!(peekChar == ')')) {
        val tree = readTree match {
          case Some(t) => t
          case None => sys.error("parser error")
        }
        children += tree
        skipSpaces
      }
      children
    }
    def readIntermediateNode(label: String): Tree = {
      skipSpaces
      readChildren match {
        case Seq(child) => UnaryTree(child, label) // unary
        case Seq(left, right) => BinaryTree(left, right, label) // binary
        case _ => sys.error("parse error; more than 2 child nodes are found!")
      }
    }
    def isNonterminal(label: String): Boolean = label(0) == 'T'

    def readTree: Option[Tree] = {
      skipSpaces
      peekChar match {
        case '(' => // new tree
          readChar // '('
          //val nodeLabel = removeBracket(readLabel)
          val nodeLabel = readLabel
          val tree = if (isNonterminal(nodeLabel)) readIntermediateNode(nodeLabel) else {
            skipSpaces
            LeafTree(nodeLabel)
          }
          readChar // ')'

          Some(tree)
        case ')' => sys.error("Extract ')' found")
        case -1 => None
      }
    }
  }
  override def newTreeReader(in: java.io.Reader) = new EnglishTreeReader(in)
}
