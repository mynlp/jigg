package jigg.nlp.ccg

/*
 Copyright 2013-2016 Hiroshi Noji

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

import lexicon._

import breeze.config.{CommandLineParser, Help}

import java.io.{File, FileWriter}

trait EvalParser {

  type Settings = EvalParser.Settings

  def main(args: Array[String]) = {

    val settings = CommandLineParser.readIn[Settings](args)

    if (settings.output.getPath != "")
      prepareDirectoryOutput(settings.output)

    val evaluater = mkEvaluater(settings)

    evaluater.eval()
  }

  def prepareDirectoryOutput(f: File) = f match {
    case d if d.exists && d.isFile =>
      sys.error("Failed create directory " + f.getPath)
    case d => d.mkdirs match {
      case true => // ok, success
      case false =>
        System.err.println("Directory " + f.getPath +
          " already exits; we override the contents.")
    }
  }

  def mkEvaluater(settings: Settings): Evaluater

  trait Evaluater {

    def settings: Settings

    val model = ParserModel.loadFrom(settings.model.getPath)
    val runner = new ParserRunner(model, settings.decoder)

    val (sentences, golds) = readGoldTrees()

    def eval() = {

      val numInstances = sentences.foldLeft(0) { _ + _.size }

      val before = System.currentTimeMillis
      val predDerivations = runner.decode(sentences)
      val parsingTime = System.currentTimeMillis - before

      val sentencePerSec = (sentences.size.toDouble / (parsingTime / 1000)).formatted("%.1f")
      val wordPerSec = (numInstances.toDouble / (parsingTime / 1000)).formatted("%.1f")

      System.err.println("parsing time: " + parsingTime + "ms; " + sentencePerSec + "s/sec; " + wordPerSec + "w/sec")

      reportScores(predDerivations)
      outputDerivations(predDerivations)
    }

    private def readGoldTrees(): (Array[GoldSuperTaggedSentence], Array[Derivation]) = {
      val bank = CCGBank.select(settings.bank, model.taggerModel.dict)
      val trees = settings.useTest match {
        case true => bank.testTrees
        case false => bank.devTrees
      }
      (bank sentences trees, bank derivations trees)
    }

    protected def reportScores(predDerivations: Array[Derivation]): Unit =
      reportCategoryAccuracy(predDerivations)

    protected def reportCategoryAccuracy(predDerivations: Array[Derivation]): Unit = {
      val (numCorrects, numCompletes) = sentences.zip(predDerivations).foldLeft(0, 0) {
        case ((corrects, completes), (sentence, derivation)) =>
          val numCorrectTokens = sentence.catSeq.zip(derivation.categorySeq).foldLeft(0) {
            case (correctSoFar, (gold, Some(pred))) if (gold == pred) => correctSoFar + 1
            case (correctSoFar, _) => correctSoFar
          }
          (corrects + numCorrectTokens, completes + (if (numCorrectTokens == sentence.size) 1 else 0))
      }
      val numInstances = sentences.foldLeft(0) { _ + _.size }
      System.err.println("\ncategory accuracies:")
      System.err.println("-----------------------")
      System.err.println("token accuracy: " + numCorrects.toDouble / numInstances.toDouble)
      System.err.println("sentence accuracy: " + numCompletes.toDouble / sentences.size.toDouble)
    }

    private def outputDerivations(derivations: Array[Derivation]) =
      settings.output.getPath match {
        case "" =>
        case path =>

          def outputInFormat(
            path: String,
            conv: (TaggedSentence, Derivation, Int)=>String) = {

            val fw = new FileWriter(path)
            sentences.zip(derivations).zipWithIndex.map {
              case ((s, deriv), i) =>
                fw write (conv(s, deriv, i) + "\n")
            }
            fw.flush
            fw.close
          }
          outputInFormat(path + "/pred", (s, deriv, i) => deriv render s)
          outputInFormat(path + "/pred.xml", (s, deriv, i) => deriv renderEnjuXML (s, i))
      }
  }
}

object EvalParser {

  import ParserRunner.Params

  case class Settings(
    @Help(text="Load model path") model: File,
    @Help(text="Eval on test set?") useTest: Boolean = false,
    @Help(text="Output path (no output if empty)") output: File = new File(""),
    @Help(text="Path to cabocha-format corpus for evaluating bunsetsu dependencies") cabocha: File = new File(""),
    decoder: Params = new Params(),
    bank: Opts.BankInfo
  )
}


object EvalJapaneseParser extends EvalParser {

  def mkEvaluater(settings: Settings) = new JapaneseEvaluater(settings)

  class JapaneseEvaluater(val settings: Settings) extends Evaluater {

    val goldCabochaSentences = readCabochaSentences(settings.cabocha.getPath, sentences)

    def readCabochaSentences[S<:TaggedSentence](
      path: String,
      ccgSentences: Seq[S]): Seq[ParsedBunsetsuSentence] =
      new CabochaReader(ccgSentences).readSentences(path)

    override def reportScores(predDerivations: Array[Derivation]) = {
      reportCategoryAccuracy(predDerivations)
      reportAccuracyAndOutputBunsetsuDep(predDerivations)
    }

    private def reportAccuracyAndOutputBunsetsuDep(predDerivations: Array[Derivation]) = {

      val goldBankSentences = cabochaSentencesFromDerives(golds)
      val predCabochaSentences = cabochaSentencesFromDerives(predDerivations)
      val connectedCabochaSentences =
        cabochaSentencesFromDerives(predDerivations map (_.toSingleRoot))

      reportDepAccuracy(
        predCabochaSentences, connectedCabochaSentences, goldBankSentences)

      outputBunsetsuDeps(predCabochaSentences)
    }

    private def cabochaSentencesFromDerives(derives: Seq[Derivation]) =
      goldCabochaSentences.zip(derives).map {
        case (sent, deriv) =>
          BunsetsuSentence(sent.bunsetsuSeq).parseWithCCGDerivation(deriv)
      }

    private def reportDepAccuracy(
      preds: Seq[ParsedBunsetsuSentence],
      connected: Seq[ParsedBunsetsuSentence],
      golds: Seq[ParsedBunsetsuSentence]) = {

      System.err.println("\ndependency accuracies against CCGBank dependency:")
      System.err.println("-----------------------")
      evaluateBunsetsuDepAccuracy(preds, golds)

      System.err.println("\nevaluation with modified derivations:")
      evaluateBunsetsuDepAccuracy(connected, golds)

      val tagger = new SuperTaggerRunner(model.taggerModel, settings.decoder.tagger)

      val targetIdxs = sentences.zipWithIndex.withFilter { case (s, i) =>
        tagger.assignKBest(s).numCandidatesContainGold == s.size
      }.map(_._2)
      // TODO: share the code with the above evaluations
      evaluateBunsetsuDepAccuracy(targetIdxs map preds, targetIdxs map golds)

      System.err.println("\nevaluation with modified derivations:")
      evaluateBunsetsuDepAccuracy(targetIdxs map connected, targetIdxs map golds)
    }

    private def evaluateBunsetsuDepAccuracy(
      preds: Seq[ParsedBunsetsuSentence],
      golds: Seq[ParsedBunsetsuSentence]) = {

      val (numCorrects, numCompletes) = preds.zip(golds).foldLeft(0, 0) {
        case ((corrects, completes), (pred, gold)) =>
          val numCorrectHeads =
            gold.headSeq.dropRight(1).zip(pred.headSeq).count(a => a._1 == a._2)
          (corrects + numCorrectHeads,
            completes + (if (numCorrectHeads == pred.size - 1) 1 else 0))
      }
      val (numConnectedB, numConnectedS) = preds.foldLeft(0, 0) {
        case ((connectedB, connectedS), pred) =>
          val numConnectedHeads = pred.headSeq.dropRight(1).count(_ != -1)
          (connectedB + numConnectedHeads,
            connectedS + (if (numConnectedHeads == pred.size - 1) 1 else 0))
      }

      val numInstances = preds.map(_.size - 1).sum

      def format_acc(numer: Int, denom: Int) =
        "%f (%d/%d)".format(numer.toDouble / denom.toDouble, numer, denom)

      System.err.println("bunsetsu accuracy: " + format_acc(numCorrects, numInstances))
      System.err.println("sentence accuracy: " + format_acc(numCompletes, preds.size))
      System.err.println("Coverage of fully connected analysis (bunsetsu): " +
        format_acc(numConnectedB, numInstances))
      System.err.println("Coverage of fully connected analysis (sentence): " +
        format_acc(numConnectedS, preds.size))

      val (activeNumCorrect, activeSum) = preds.zip(golds).foldLeft(0, 0) {
        case ((corrects, sum), (pred, gold)) =>
          val activeHeadIdxs =
            (0 until pred.headSeq.size - 1).filter(pred.headSeq(_) != -1)
          val numCorrectHeads =
            activeHeadIdxs.count { i => pred.headSeq(i) == gold.headSeq(i) }
          (corrects + numCorrectHeads, sum + activeHeadIdxs.size)
      }
      System.err.println("active bunsetsu accuracy: " +
        format_acc(activeNumCorrect, activeSum))
    }

    private def outputBunsetsuDeps(sentences: Seq[ParsedBunsetsuSentence]) =
      settings.output.getPath match {
        case "" =>
        case path =>
          System.err.println("\nSaving predicted bunsetsu dependencies to " + path)

          outputBunsetsuDepsIn(_.renderInCabocha, sentences, path + "/pred.cabocha")
          outputBunsetsuDepsIn(_.renderInCoNLL, sentences, path + "/pred.conll")
          System.err.println("done")
      }

    private def outputBunsetsuDepsIn(
      conv: ParsedBunsetsuSentence=>String,
      sentences:Seq[ParsedBunsetsuSentence],
      path: String) = {

      val fw = new FileWriter(path)
      sentences foreach { sent => fw.write(conv(sent) + "\n") }
      fw.flush
      fw.close
    }
  }
}

object EvalEnglishParser extends EvalParser {

  def mkEvaluater(settings: Settings) = new EnglishEvaluater(settings)

  class EnglishEvaluater(val settings: Settings) extends Evaluater
}
