package enju.ccg.parser

import scala.collection.mutable.HashMap
import enju.ccg.lexicon.Category

trait Rule {
  def unify(left:Category, right:Category): Option[Category]
  def raise(child:Category): Option[Category]
  def headFinder:HeadFinder
}

// rules are restricted to CFG rules extracted from the training CCGBank
class CFGRule(val binaryRules:HashMap[(Int,Int), Category], // category ids -> category
              val unaryRules:HashMap[Int, Category],
              override val headFinder:HeadFinder) extends Rule {
  def unify(left:Category, right:Category) = binaryRules.get((left.id, right.id))
  def raise(child:Category) = unaryRules.get(child.id)
}
