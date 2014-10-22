package enju.ccg

import java.io.FileWriter
import scala.collection.mutable.ArrayBuffer
import scala.sys.process.Process
import fig.exec.Execution
import lexicon._
import scala.collection.mutable.HashMap

object OutputCategoryList {

  def main(args:Array[String]) = {
    val runner = new Runner
    Execution.run(args, runner,
      "input", InputOptions,
      "output", OutputOptions,
      "train", TrainingOptions,
      "dict", DictionaryOptions,
      "tagger", TaggerOptions,
      "parser", ParserOptions)
  }

  type Tree = ParseTree[NodeLabel]

  class Runner extends JapaneseSuperTagging with Runnable {

    def run = {
      dict = newDictionary
      val trainTrees = readParseTreesFromCCGBank(trainPath, -1, true)

      val trainSentences = trainTrees map { parseTreeConverter.toSentenceFromLabelTree(_) }

      val categoreis = new HashMap[Category, (Int, GoldSuperTaggedSentence)]
      //val categoreis = new HashSet[String]

      trainSentences foreach { sentence =>
        (0 until sentence.size) foreach { i =>
          val cat = sentence.cat(i)
          categoreis.get(cat) match {
            case Some((_, exist)) if sentence.size > exist.size =>
              categoreis += ((cat, (i, sentence)))
            case None => categoreis += ((cat, (i, sentence)))
            case _ =>
          }
        }
      }
      def highlight(sentence: Sentence, i: Int) = {
        val tokens = sentence.wordSeq
        // tokens.take(i).mkString("") + s"\\x1b[1;31m{${tokens(i)}}\\x1b[0m" + tokens.drop(i+1).mkString("")
        tokens.slice(i-5, i).mkString("") + s"[01;31m${tokens(i)}[00m" + tokens.slice(i+1, i+6).mkString("")
      }

      var fw = new FileWriter("./category.lst")
      categoreis foreach { case (cat, (i, sentence)) =>
        fw.write("%s\t%s\t%s\n".format(cat, sentence.pos(i), highlight(sentence, i)))
      }
      fw.flush
      fw.close

      val noFeatureCategories = new HashMap[String, ((Int, GoldSuperTaggedSentence))]
      categoreis foreach { case (cat, (i, sentence)) =>
        val nofeature = cat.toStringNoFeature
        noFeatureCategories.get(nofeature) match {
          case None => noFeatureCategories += ((nofeature, (i, sentence)))
          case _ =>
        }
      }

      fw = new FileWriter("./category.nofeature.lst")
      noFeatureCategories foreach { case (cat, (i, sentence)) =>
        fw.write("%s\t%s\t%s\n".format(cat, sentence.pos(i), highlight(sentence, i)))
      }
      fw.flush
      fw.close
    }
  }
}
