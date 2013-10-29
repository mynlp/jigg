package enju.ccg.lexicon
import Slash._

/**
 * Parse a category string to category object.
 * WARNING: The created category by this parser has id 0. This is because the management of
 * category id assignments is expected to another class. In the current version, CategoryManager
 * do this. In other words, this class create the Category skeleton, which must be transformed
 * into the complete object with correct ids assigned by the manager class.
 */
class CategoryParser {
  var pos = 0
  var currentStr = ""
  
  def parse(catStr:String):Category = {
    val catTree = strToCategoryTree(catStr)
    categoryTreeToCategory(catTree)
  }
  def strToCategoryTree(catStr:String) = getSimplifiedCategoryTree(catStr)
  def categoryTreeToCategory(catTree:CategoryTree):Category = {
    if (catTree.isLeaf) createAtomicCategory(catTree.surface) 
    else ComplexCategory(
      0, categoryTreeToCategory(catTree.left), categoryTreeToCategory(catTree.right), catTree.slash)
  }
  def getSimplifiedCategoryTree(catStr:String) = {
    def simplify(surface:String) = {
      var simplified = removeFeaturesExceeding(2, surface)
      simplified = removeInfoFollowsFeature(simplified)
      removeNumbers(simplified)
    }
    def removeFeaturesExceeding(n:Int,surface:String) = {
      val commaIndices = surface.zipWithIndex.withFilter {
        case (c, i) => c == ',' }.map { case (c, i) => i }
      if (commaIndices.size > n)
        surface.substring(0, commaIndices(n)) + surface.substring(surface.indexOf(']'))
      else surface
    }
    def removeInfoFollowsFeature(surface:String) = surface.indexOf(']') match {
      case -1 => surface
      case featureEnd => surface.substring(0, featureEnd + 1)}
    def removeNumbers(surface:String) = 
      if (surface.last.isDigit) surface.substring(0, surface.size - 1) else surface

    val catTree = parseToCategoryTree(catStr)
    catTree.setSurface
    catTree.foreachLeaf((tree:CategoryTree) => {tree.surface = simplify(tree.surface)})
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
  
  def extractAVM(avmStr:String) = avmStr match {
    case "" => AVM.empty
    case _ => AVM.createFromValues(avmStr.split(","))
  }
  def createAtomicCategory(catStr:String) = {
    val avmStr = catStr.indexOf('[') match {
      case -1 => ""
      case begin => catStr.substring(begin + 1, catStr.indexOf(']'))
    }
    AtomicCategory(0, getBasicTag(catStr), extractAVM(avmStr))
  }
  def getBasicTag(catStr:String) = catStr.indexOf('[') match {
    case -1 => { // e.g. NP or S1
      var j = catStr.size - 1
      while (catStr(j).isDigit) j -= 1
      catStr.substring(0, j + 1)
    }
    case begin => catStr.substring(0, begin)
  }
}
