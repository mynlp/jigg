package enju.ccg.parser

import enju.ccg.lexicon.{Category, Derivation, Point, UnaryChildPoint, BinaryChildrenPoints}

import scala.collection.mutable.{HashMap, HashSet}

trait Rule {
  def unify(left:Category, right:Category): Option[Array[Category]]
  def raise(child:Category): Option[Array[Category]]
  def headFinder:HeadFinder
}

// rules are restricted to CFG rules extracted from the training CCGBank
class CFGRule(val binaryRules:Map[(Int,Int), Array[Category]], // category ids -> category
              val unaryRules:Map[Int, Array[Category]],
              override val headFinder:HeadFinder) extends Rule {
  def unify(left:Category, right:Category):Option[Array[Category]] = binaryRules.get((left.id, right.id))
  def raise(child:Category):Option[Array[Category]] = unaryRules.get(child.id)
}

object CFGRule {
  def extractRulesFromDerivations(derivations: Array[Derivation], headFinder:HeadFinder): CFGRule = {
    val binaryRules = new HashMap[(Int, Int), HashSet[Category]]
    val unaryRules = new HashMap[Int, HashSet[Category]]
    
    derivations.foreach { deriv =>
      deriv.foreachPoint({ point:Point => deriv.get(point) match {
        case Some(UnaryChildPoint(child)) =>
          val parents = unaryRules.getOrElseUpdate(child.category.id, new HashSet[Category])
          parents += point.category
        case Some(BinaryChildrenPoints(left, right)) =>
          val parents = binaryRules.getOrElseUpdate((left.category.id, right.category.id), new HashSet[Category])
          parents += point.category
        case _ =>
      }})
    }
    new CFGRule(binaryRules.map { case (k, v) => k -> v.toArray }.toMap[(Int,Int),Array[Category]],
                unaryRules.map { case (k, v) => k -> v.toArray }.toMap,
                headFinder)
  }
}
