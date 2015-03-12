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

import jigg.nlp.ccg.lexicon.{Category, Derivation, Point, UnaryChildPoint, BinaryChildrenPoints, AppliedRule}

import scala.collection.mutable.{HashMap, HashSet}
import java.io.{ObjectOutputStream, ObjectInputStream}

trait Rule {
  def unify(left:Category, right:Category): Option[Array[(Category, String)]]
  def raise(child:Category): Option[Array[(Category, String)]]
  def headFinder:HeadFinder
}

// rules are restricted to CFG rules extracted from the training CCGBank
case class CFGRule(val binaryRules:Map[(Int,Int), Array[(Category, String)]], // category ids -> (category, ruleType)
                   val unaryRules:Map[Int, Array[(Category, String)]],
                   override val headFinder:HeadFinder) extends Rule {
  def unify(left:Category, right:Category):Option[Array[(Category, String)]] = binaryRules.get((left.id, right.id))
  def raise(child:Category):Option[Array[(Category, String)]] = unaryRules.get(child.id)
}

object CFGRule {
  def extractRulesFromDerivations(derivations: Array[Derivation], headFinder:HeadFinder): CFGRule = {
    val binaryRules = new HashMap[(Int, Int), HashSet[(Category, String)]]
    val unaryRules = new HashMap[Int, HashSet[(Category, String)]]

    derivations.foreach { deriv =>
      deriv.foreachPoint({ point:Point => deriv.get(point) match {
        case Some(AppliedRule(UnaryChildPoint(child), ruleType)) =>
          val parents = unaryRules.getOrElseUpdate(child.category.id, new HashSet[(Category, String)])
          parents += ((point.category, ruleType))
        case Some(AppliedRule(BinaryChildrenPoints(left, right), ruleType)) =>
          val parents = binaryRules.getOrElseUpdate((left.category.id, right.category.id), new HashSet[(Category, String)])
          parents += ((point.category, ruleType))
        case _ =>
      }})
    }
    new CFGRule(binaryRules.map { case (k, v) => k -> v.toArray }.toMap,
                unaryRules.map { case (k, v) => k -> v.toArray }.toMap,
                headFinder)
  }
}
