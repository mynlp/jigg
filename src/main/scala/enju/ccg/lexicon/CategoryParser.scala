package enju.ccg.lexicon
import Slash._

/**
 * Parse a category string to category object.
 * WARNING: The created category by this parser has id 0. This is because the management of
 * category id assignments is expected to another class. In the current version, CategoryManager
 * do this. In other words, this class create the Category skeleton, which must be transformed
 * into the complete object with correct ids assigned by the manager class.
 */

object JapaneseCategoryParser extends CategoryParser {
  class JapaneseReader extends Reader {
    override def newCategoryFeature(vals: Seq[String]) = JPCategoryFeature.createFromValues(vals)
  }
  override def newReader = new JapaneseReader
}

object EnglishCategoryParser extends CategoryParser {
  class EnglishReader extends Reader {
    override val maxFeatureSize = -1
    override val slashLeft = '\\'
    override val slashRight = '/'

    override def newCategoryFeature(vals: Seq[String]) = EnCategoryFeature.createFromValues(vals)
  }
  override def newReader = new EnglishReader
}

trait CategoryParser {
  trait Reader {
    val maxFeatureSize = 2

    var pos = 0
    var currentStr = ""
    def strToCategoryTree(catStr:String) = getSimplifiedCategoryTree(catStr)
    def categoryTreeToCategory(catTree:CategoryTree):Category = {
      if (catTree.isLeaf) createAtomicCategory(catTree.surface)
      else ComplexCategory(
        0, categoryTreeToCategory(catTree.left), categoryTreeToCategory(catTree.right), catTree.slash)
    }
    def getSimplifiedCategoryTree(catStr:String) = {
      def simplify(surface:String) = {
        var simplified = removeRedundantFeatures(surface)
        simplified = removeInfoFollowsFeature(simplified)
        removeNumbers(simplified)
      }
      def removeRedundantFeatures(surface:String) = {
        if (maxFeatureSize < 0) surface
        else {
          val commaIndices = surface.zipWithIndex.withFilter {
            case (c, i) => c == ',' }.map { case (c, i) => i }
          if (commaIndices.size > maxFeatureSize)
            surface.substring(0, commaIndices(maxFeatureSize)) + surface.substring(surface.indexOf(']'))
          else surface
        }
      }
      def removeInfoFollowsFeature(surface:String) = surface.indexOf(']') match {
        case -1 => surface
        case featureEnd => surface.substring(0, featureEnd + 1)}
      def removeNumbers(surface:String) =
        if (surface.size > 0 && surface.last.isDigit) {
          surface.substring(0, surface.size - 1)
        } else surface


      val catTree = parseToCategoryTree(catStr)
      catTree.setSurface
      catTree.foreachLeaf { tree: CategoryTree =>
        tree.surface = simplify(tree.surface)
      }
      catTree
    }
    def parseToCategoryTree(catStr:String):CategoryTree = {
      pos = 0
      currentStr = catStr
      parseToCategoryTreeHelper
    }
    def parseToCategoryTreeHelper:CategoryTree = {
      val ch = peek
      val targetTree = parseTargetCategoryTree
      val slash = parseSlash
      if (slash == null) targetTree
      else {
        pos += 1
        val argumentTree = parseToCategoryTreeHelper
        CategoryTree.createInternal(slash, targetTree, argumentTree)
      }
    }
    def parseTargetCategoryTree:CategoryTree = {
      val target = if (isLeftParen(peek)) {
        pos += 1; val t = parseToCategoryTreeHelper; pos += 1; t
      } else CategoryTree.createLeaf(readAtomicCategory)

      // assume (S2/S2)1/... where we have to process 1
      // WARNING: currently, the number after the parenthesis is removed
      // in this point. This might cause a bug if future version require
      // these number in the parser.
      if (peek.toChar.isDigit) pos += 1
      target
    }
    def parseSlash = peek match {
      case `slashLeft` => Slash.Left
      case `slashRight` => Slash.Right
      case _ => null
    }
    def readAtomicCategory:String = {
      val buf = new StringBuilder()
      var ch = peek
      while (ch != 0 && !isSlash(ch) && !isRightParen(ch)) {
        buf.append(ch)
        pos += 1
        ch = peek
      }
      buf.toString
    }

    def peek:Char = if (pos < currentStr.size) currentStr(pos) else 0

    val slashLeft = '＼'
    val slashRight = '／'

    def isSlash(ch:Char) = ch == slashLeft || ch == slashRight
    def isLeftParen(ch:Char) = ch == '('
    def isRightParen(ch:Char) = ch == ')'

    def extractCategoryFeature(featureStr:String): CategoryFeature = newCategoryFeature(featureStr.split(","))

    def newCategoryFeature(vals: Seq[String]): CategoryFeature

    def createAtomicCategory(catStr:String) = {
      val featureStr = catStr.indexOf('[') match {
        case -1 => ""
        case begin => catStr.substring(begin + 1, catStr.indexOf(']'))
      }
      AtomicCategory(0, getBasicTag(catStr), extractCategoryFeature(featureStr))
    }
    def getBasicTag(catStr:String) = catStr.indexOf('[') match {
      case -1 => // e.g. NP or S1
        var j = catStr.size - 1
        while (j >= 0 && catStr(j).isDigit) j -= 1
        catStr.substring(0, j + 1)
      case begin => catStr.substring(0, begin)
    }
  }
  def newReader: Reader

  def parse(catStr:String):Category = {
    val reader = newReader
    val catTree = reader.strToCategoryTree(catStr)
    reader.categoryTreeToCategory(catTree)
  }
}
