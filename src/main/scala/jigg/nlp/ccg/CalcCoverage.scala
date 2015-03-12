package jigg.nlp.ccg

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

import scala.collection.mutable.ArrayBuffer
import fig.exec.Execution
import lexicon._

object CalcCoverage {
  def main(args:Array[String]) = {
    val runner = new CalcCoverageRunner
    Execution.run(args, runner,
      "input", InputOptions,
      "output", OutputOptions,
      "train", TrainingOptions,
      "dict", DictionaryOptions,
      "tagger", TaggerOptions,
      "parser", ParserOptions)
  }
}

class CalcCoverageRunner extends JapaneseSuperTagging with Runnable {
  def run = {
    dict = newDictionary
    val trainTrees = readParseTreesFromCCGBank(trainPath, -1, true)
    val develTrees = readParseTreesFromCCGBank(developPath, -1, true)

    val calc = new Calculator(trainTrees, develTrees)

    println("\nlexical category coverage:")
    calc.lexicalCategoryCoverage

    println("\nrule coverage:")
    calc.ruleCoverage
  }

  class Calculator(trainTrees: Seq[ParseTree[NodeLabel]], develTrees: Seq[ParseTree[NodeLabel]]) {
    def evalCoverage[A](countsInTrain: Map[A, Int], instances: Seq[A]) = {
      val coverage = instances.map { countsInTrain.getOrElse(_, 0) }.groupBy(identity).mapValues(_.size).toSeq.sortWith(_._1 < _._1)

      println("all instances: " + instances.size)
      var accum = 0.0
      coverage.take(10).foreach { case (occurInTrain, numTokens) =>
        val frac = numTokens.toDouble / instances.size.toDouble
        accum += frac
        println(f"$occurInTrain: $numTokens ($frac%.5f) ($accum%.5f)")
        //println(f"|$occurInTrain|$numTokens|$frac%.5f|$accum%.5f|")
      }
    }

    def lexicalCategoryCoverage = {
      val catCounts = trainTrees.flatMap { _.getSequence.map { _.label.category } }.groupBy(identity).mapValues(_.size)

      println("\ntype coverage:")
      evalCoverage(catCounts, develTrees.flatMap { _.getSequence.map { _.label.category } }.distinct)

      println("\ntoken coverage")
      evalCoverage(catCounts, develTrees.flatMap { _.getSequence.map { _.label.category } })

      val trainSentences = trainTrees map { parseTreeConverter.toSentenceFromLabelTree(_) }
      val develSentences = develTrees map { parseTreeConverter.toSentenceFromLabelTree(_) }
      setCategoryDictionary(trainSentences)

      println("\nsuper tagging upper bound")
      val categoryFoundTokens = develSentences.map { s =>
        (0 until s.size).filter { i =>
          dict.getCategoryCandidates(s.base(i), s.pos(i)).contains(s.cat(i))
        }.size
      }.sum
      val numInstances = develSentences.map { _.size }.sum
      val frac = categoryFoundTokens.toDouble / numInstances.toDouble

      println(f"token: $categoryFoundTokens / $numInstances ($frac%.5f)")
    }

    trait Rule
    case class UnaryRule(parent: Category) extends Rule
    case class BinaryRule(parent: Category) extends Rule

    def ruleCoverage = {
      def cat(t: ParseTree[NodeLabel]) = t.label.category

      def extractAllRules(trees: Seq[ParseTree[NodeLabel]]): Seq[Rule] = {
        val rules = new ArrayBuffer[Rule]
        trees foreach { _.foreachTree { tree => tree.children match {
          case left :: right :: Nil =>
            rules += BinaryRule(cat(tree))
          case child :: Nil =>
            rules += UnaryRule(cat(tree))
          case _ =>
        }}}
        rules
      }
      val ruleCounts = extractAllRules(trainTrees).groupBy(identity).mapValues(_.size)

      println("type coverage:")
      evalCoverage(ruleCounts, extractAllRules(develTrees).distinct)

      println("token coverage")
      evalCoverage(ruleCounts, extractAllRules(develTrees))
    }
  }
}
