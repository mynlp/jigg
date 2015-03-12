package jigg.nlp.ccg.parser

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
import jigg.nlp.ccg.lexicon.{PoS, JapanesePoS, Category}
import jigg.nlp.ccg.lexicon.Direction._

trait HeadFinder extends Serializable {
  type NodeInfo = HeadFinder.NodeInfo
  def get(left:NodeInfo, right:NodeInfo): Direction
}
object HeadFinder {
  case class NodeInfo(pos:PoS, category:Category, headCategory:Category)
}

case class EnglishHeadFinder(children2dir: Map[(Int, Int), Direction]) extends HeadFinder {
  def get(left:NodeInfo, right:NodeInfo) =
    children2dir.get(left.category.id, right.category.id) match {
      case Some(dir) => dir
      case _ => Left
    }
}

object EnglishHeadFinder {
  import jigg.nlp.ccg.lexicon.{ParseTree, NodeLabel, BinaryTree, NonterminalLabel}
  def createFromParseTrees(trees: Seq[ParseTree[NodeLabel]]): EnglishHeadFinder = {
    val map = new HashMap[(Int, Int), Direction]
    trees.foreach { _.foreachTree { _ match {
      case BinaryTree(left, right, NonterminalLabel(dir, _, _)) =>
        map += (left.label.category.id, right.label.category.id) -> dir
      case _ =>
    }}}
    EnglishHeadFinder(map.toMap)
  }
}

object JapaneseHeadFinder extends HeadFinder {
  val Symbol = "記号"
  def get(left:NodeInfo, right:NodeInfo) = {
    val leftPos = left.pos.first.v
    val rightPos = right.pos.first.v
    if (rightPos == Symbol) Left else Right
  }
}
