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
    override def newCategoryFeature(vals: Seq[String]) = EnCategoryFeature.createFromValues(vals)
  }
  override def newReader = new EnglishReader
}

trait CategoryParser {
  trait Reader {
    var pos = 0
    var currentStr = ""
    def strToCategoryTree(catStr:String) = getSimplifiedCategoryTree(catStr)
    def categoryTreeToCategory(catTree:CategoryTree):Category = {
      if (catTree.isLeaf) createAtomicCategory(catTree.surface)
      else ComplexCategory(
        0, categoryTreeToCategory(catTree.left), categoryTreeToCategory(catTree.right), catTree.slash)
    }
    // (NP[case=X1,mod=X2]{I1}/NP[case=X1,mod=X2]{I1}){I2}_none -> NP[case=X1,mod=X2]/NP[case=X1,mod=X2]
    def getSimplifiedCategoryTree(catStr:String) = {
      def simplify(surface:String) = {
        removeSemanticInfo(surface)
        // simplified = removeInfoFollowsFeature(simplified)
        // removeNumbers(simplified)
      }
      def removeSemanticInfo(surface: String) = surface.indexOf('{') match {
        case -1 => surface
        case begin => surface.substring(0, begin)
      }
      // def removeInfoFollowsFeature(surface:String) = surface.indexOf(']') match {
      //   case -1 => surface
      //   case featureEnd => surface.substring(0, featureEnd + 1)}

      // def removeNumbers(surface:String) =
      //   if (surface.size > 0 && surface.last.isDigit) {
      //     surface.substring(0, surface.size - 1)
      //   } else surface

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
      // if (peek.toChar.isDigit) pos += 1

      // 2014/10/22: This is new mechanism to skip the semantic info
      // Currently the below is rather ad-hoc.
      // TODO: refactor this class to fully interpret with the current category definition
      if (peek.toChar == '{') {
        while (peek.toChar != '}') pos += 1
        pos += 1
      }
      // if (peek.toChar == ')') {
      //   while (peek.toChar == ')') pos += 1
      // }

      target
    }
    def parseSlash = peek match {
      case c if isSlashLeft(c) => Slash.Left
      case c if isSlashRight(c) => Slash.Right
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

    def isSlashLeft(c: Char) = c == '\\'
    def isSlashRight(c: Char) = c == '/'

    def isSlash(c:Char) = isSlashLeft(c) || isSlashRight(c)
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
