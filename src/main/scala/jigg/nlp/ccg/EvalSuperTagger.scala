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

/** Report accuracy on dev set.
  */
object EvalSuperTagger {

  import SuperTaggerRunner.Params

  case class Settings(
    @Help(text="Load model path") model: File,
    @Help(text="Eval on test set?") useTest: Boolean = false,
    @Help(text="Output path (no output if empty)") output: File = new File(""),
    decoder: Params = new Params(),
    bank: Opts.BankInfo
  )

  def main(args: Array[String]) = {

    val settings = CommandLineParser.readIn[Settings](args)

    val evaluater = new Evaluater(settings)
    evaluater.eval()
  }

  class Evaluater(settings: Settings) {

    val model = SuperTaggerModel.loadFrom(settings.model.getPath)
    val runner = new SuperTaggerRunner(model, settings.decoder)

    // types not registered in the model are treated as unknown
    val knownTypes: Set[String] = model.dict.vocabulary

    def eval() = {
      val bank = CCGBank.select(settings.bank, model.dict)

      System.err.println("Reading CCGBank ...")
      val evalSentences = settings.useTest match {
        case true => bank.testSentences
        case false => bank.devSentences
      }

      val numInstances = evalSentences.foldLeft(0) { _ + _.size }
      System.err.println("done; # evaluating sentences: " + evalSentences.size)

      val before = System.currentTimeMillis

      val assignedSentences = runner.assignKBests(evalSentences).toArray

      val taggingTime = System.currentTimeMillis - before
      val sentencePerSec = (evalSentences.size.toDouble / (taggingTime / 1000)).formatted("%.1f")
      val wordPerSec = (numInstances.toDouble / (taggingTime / 1000)).formatted("%.1f")

      System.err.println("tagging time: " + taggingTime + "ms; " + sentencePerSec + "s/sec; " + wordPerSec + "w/sec")
      evaluateTokenSentenceAccuracy(assignedSentences)
      // outputPredictions(assignedSentences)
    }

    def evaluateTokenSentenceAccuracy(sentences: Array[TrainSentence]) = {
      var sumUnk = 0
      var correctUnk = 0

      def isKnown(word: Word): Boolean = knownTypes contains word.v

      // val unkType = model.dict.unkType
      val (numCorrect, numComplete) = sentences.foldLeft(0, 0) {
        case ((cor, comp), sent) =>
          val numCorrectToken = sent.numCandidatesContainGold
          (cor + numCorrectToken, comp + (if (numCorrectToken == sent.size) 1 else 0))
      }
      val (numUnks, numCorrectUnks): (Int,Int) = sentences.foldLeft(0, 0) {
        case ((sum, cor), sent) =>
          val unkIdxes = (0 until sent.size).filter { i => !isKnown(sent.word(i)) }
          val unkCorrects = unkIdxes.foldLeft(0) {
            case (n, i) if (sent.cand(i).contains(sent.cat(i))) => n + 1
            case (n, _) => n
          }
          (sum + unkIdxes.size, cor + unkCorrects)
      }
      val numInstances = sentences.foldLeft(0) { _ + _.size }

      def acc(numer: Int, denom: Int) = numer.toDouble / denom.toDouble + " (" + numer + "/" + denom + ")"

      System.err.println()
      System.err.println("token accuracy: " + acc(numCorrect, numInstances))
      System.err.println("sentence accuracy: " + numComplete.toDouble / sentences.size.toDouble)
      System.err.println("unknown accuracy: " + acc(numCorrectUnks, numUnks))

      val sumCandidates = sentences.foldLeft(0) { case (sum, s) => sum + s.candSeq.map(_.size).sum }
      System.err.println("# average of candidate labels after super-tagging: " + (sumCandidates.toDouble / numInstances.toDouble).formatted("%.2f"))
      System.err.println()
    }

    def outputPredictions[S<:CandAssignedSentence](sentences: Array[S]) =
      settings.output.getPath match {
        case "" =>
        case path =>
          System.err.println("saving tagger prediction results to " + path)
          val fw = new FileWriter(path)
          sentences.foreach { sentence =>
            (0 until sentence.size).foreach { i =>
              fw.write(sentence.word(i) + " " + sentence.cand(i).mkString(" ") + "\n")
            }
            fw.write("\n")
          }
          fw.flush
          fw.close
          System.err.println("done.")
      }
  }
}
