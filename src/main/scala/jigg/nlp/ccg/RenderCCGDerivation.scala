// package jigg.nlp.ccg

// import java.io.FileWriter
// import scala.collection.mutable.ArrayBuffer
// import scala.sys.process.Process
// import fig.exec.Execution
// import lexicon._

// object RenderCCGDerivation {

//   object Opt extends Options {
//     @Option(gloss="Path to CCGBank file", required=true) var ccgBankPath = ""
//     @Option(gloss="Path to output (pdf)") var outputPath = ""
//     @Option(gloss="Number of sentences") var numSentences = 50
//   }

//   def main(args:Array[String]) = {
//     val runner = new Runner
//     Execution.run(args, runner, Opt)
//   }

//   type Tree = ParseTree[NodeLabel]

//   class Runner extends Runnable {

//     def run = {
//       val dict = new JapaneseDictionary(new Word2CategoryDictionary)

//       val conv = new JapaneseParseTreeConverter(dict)

//       val reader = new CCGBankReader(dict)
//       val parseFragments: Seq[Seq[Tree]] =
//         reader.takeLines(Opt.ccgBankPath, Opt.numSentences).toSeq.map { line =>
//           reader.readParseFragments(line).map { conv.toLabelTree(_) }
//         }

//       val renderer = new TikzRenderer
//       renderer.genPdf(parseFragments.map { renderFragments(_) }.mkString("\n\n"))
//     }

//     def escape(label: NodeLabel) = {
//       val r = """([^\[,]\w+)=""".r
//       val x = label.category.toString.replaceAllLiterally("\\", "$\\backslash$")
//       r.replaceAllIn(x, "")
//     }

//     def renderFragments(fragments: Seq[Tree]): String = fragments match {
//       case Seq(root) => "\\Tree " + renderTree(root)
//       case _ => "\\Tree [.X " + fragments.map { renderTree(_) }.mkString(" ") + "]"
//     }
//     def renderTree(tree: Tree): String = tree match {
//       case UnaryTree(child, label) => s"[.${escape(label)} ${renderTree(child)} ]"
//       case BinaryTree(left, right, label) => s"[.${escape(label)} ${renderTree(left)} ${renderTree(right)} ]"
//       case LeafTree(label: TerminalLabel) => s"[.${escape(label)} ${label.word} ]"
//       case _ => sys.error("A leaf node with a non-terminal label found.")
//     }
//   }

//   class TikzRenderer {
//     val cmd = "/Users/noji/Library/TeXShop/bin/platex2pdf-utf8"

//     val header = """\documentclass[8pt,a4j]{jsarticle}
// \"""+ """usepackage{amssymb}
// \""" + """usepackage[dvipdfmx]{graphicx}
// \""" + """usepackage[dvipdfmx]{color}
// \""" + """usepackage{amsmath}
// %\""" + """usepackage{mediabb}
// \""" + """usepackage{etoolbox}
// \""" + """usepackage{tikz}
// \""" + """usepackage{tikz-qtree}

// \title{note}
// \author{}
// \date{\today}
// \pagestyle{empty}

// \begin{document}
// """
//     val footer = """
// \end{document}"""

//     def genPdf(treeStrings: String) = {
//       val texOutputPath = Opt.outputPath.dropRight(4) + ".tex"
//       val fw = new FileWriter(texOutputPath)
//       fw.write(header)
//       fw.write(treeStrings)
//       fw.write(footer)
//       fw.flush
//       fw.close

//       Process(cmd + " " + texOutputPath).run
//     }
//   }
// }
