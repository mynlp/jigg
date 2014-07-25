package enju.ccg

import java.io.FileWriter
import scala.collection.mutable.ArrayBuffer
import scala.sys.process.Process
import fig.exec.Execution
import lexicon._

object CCGBank2EnjuXML {

  object Opt extends Options {
    @Option(gloss="Path to CCGBank file", required=true) var ccgBankPath = ""
    @Option(gloss="Path to output (pdf)") var outputPath = ""
    @Option(gloss="Number of sentences") var numSentences = 50
  }

  def main(args:Array[String]) = {
    val runner = new Runner
    Execution.run(args, runner, Opt)
  }

  type Tree = ParseTree[NodeLabel]

  class Runner extends Runnable {

    def run = {
      val dict = new JapaneseDictionary(new Word2CategoryDictionary)

      val conv = new JapaneseParseTreeConverter(dict)

      val reader = new CCGBankReader(dict)
      val parseFragments: Seq[Seq[Tree]] =
        reader.takeLines(Opt.ccgBankPath, Opt.numSentences).toSeq.map { line =>
          reader.readParseFragments(line).map { conv.toLabelTree(_) }
        }

      val fw = new FileWriter(Opt.outputPath)

      parseFragments.zipWithIndex foreach { case (t, i) => fw.write(xmlFormat(t, i) + "\n") }

      fw.flush
      fw.close
    }

    def xmlFormat(trees: Seq[Tree], i: Int): String =
      s"""<sentence id="s$i" parse_status="success">""" + trees.map { xmlFormat(_) }.mkString + "</sentence>"

    def xmlFormat(tree: Tree): String = tree match {
      case UnaryTree(child, label) =>
        cons(label) + xmlFormat(child) + "</cons>"
      case BinaryTree(left, right, label) =>
        cons(label) + xmlFormat(left) + xmlFormat(right) + "</cons>"
      case LeafTree(label: TerminalLabel) => cons(label) + tok(label) + "</cons>"
      case _ => sys.error("A leaf node with a non-terminal label found.")
    }
    def removeFeatureKeys(category: String) = {
      val r = """([^\[,]\w+)=""".r
      r.replaceAllIn(category, "")
    }
    def cons(label: NodeLabel) = s"""<cons id="c${label.category.id}" cat="${removeFeatureKeys(label.category.toString)}">"""
    def tok(label: TerminalLabel) = s"""<tok id="t${label.category.id}" surface="${label.word}" pos="${label.pos}">${label.word}</tok>"""
  }
}
