package enju.ccg.parser

import enju.ccg.lexicon.{PoS, Word, Category, CandAssignedSentence, AppliedRule, TrainSentence, Derivation}
import enju.ccg.ml.{Perceptron, FeatureIndexer, Example}
import scala.collection.mutable.ArrayBuffer

// these are return types of the parser
case class WrappedAction(v:Action, isGold:Boolean, partialFeatures:Array[Int] = Array.empty[Int])
case class StatePath(state:State, actionPath:List[WrappedAction], score:Double = 0) {
  // remove feature information (which might be heavy for later process, e.g., error analysis)
  def toLight:StatePath = {
    val lighterActionPath = actionPath.map { case WrappedAction(v, isGold, _) => WrappedAction(v, isGold) }
    StatePath(state, lighterActionPath, score)
  }
  def fullFeatures:Array[Int] = actionPath.flatMap { _.partialFeatures }.toArray
}

trait TransitionBasedParser {
  def rule:Rule

  def possibleActions(state:State, sentence:CandAssignedSentence):List[Action] = {
    def possibleCombine:List[Action] = {
      val actions:Option[List[Action]] = for {
        s1 <- state.s1; s0 <- state.s0
        (combinedCategories) <- rule.unify(s1.category, s0.category)
      } yield combinedCategories.map {
        case (category, ruleType) =>
          val leftNodeInfo = HeadFinder.NodeInfo(
            sentence.pos(s1.head), s1.category, s1.headCategory)
          val rightNodeInfo = HeadFinder.NodeInfo(
            sentence.pos(s0.head), s0.category, s0.headCategory)
          Combine(category, rule.headFinder.get(leftNodeInfo, rightNodeInfo), ruleType)
      }.toList
      actions getOrElse Nil
    }
    def possibleUnary:List[Action] = {
      val actions:Option[List[Action]] = for {
        s0 <- state.s0
        parentCategories <- rule.raise(s0.category)
      } yield parentCategories.map {
        case (category, ruleType) => Unary(category, ruleType)
      }.toList
      actions getOrElse Nil
    }
    def possibleShifts:List[Action] = if (state.j < sentence.size) sentence.cand(state.j).map { Shift(_) }.toList else Nil
    def addFinishIfNecessary(actions:List[Action]) =
      // TODO: this procedure might be unappropriate
      //actions match { case Nil => Finish() :: actions; case _ => actions }

      // this latter adds Finish action regardness of whether there are other actions
      if (state.j >= sentence.size) Finish() :: actions else actions
    val actions = possibleCombine ::: possibleUnary ::: possibleShifts
    addFinishIfNecessary(actions)
  }

  def trainSentences(sentences: Array[TrainSentence], golds:Array[Derivation]):Int = {
    var correct = 0
    sentences.zip(golds).zipWithIndex.foreach {
      case ((sentence, derivation), numProcessed) =>
        if (trainSentence(sentence, derivation)) correct += 1
    }
    correct
  }
  def trainSentence(sentence: TrainSentence, gold:Derivation): Boolean
  def predict(sentence: CandAssignedSentence): Derivation
}

class DeterministicDecoder(
  val indexer:FeatureIndexer[LF],
  val extractors:FeatureExtractors,
  val classifier:Perceptron[ActionLabel],
  val oracleGen:OracleGenerator,
  override val rule:Rule,
  val initialState:State) extends TransitionBasedParser {

  def trainSentence(sentence: TrainSentence, gold:Derivation): Boolean = {
    val oracle = oracleGen.gen(sentence, gold, rule)

    val stateOracleSeq: Seq[(State, Action)] = {
      val stateSeq = ArrayBuffer(initialState)
      val oracleSeq = new ArrayBuffer[Action]

      while (oracleSeq.isEmpty || oracleSeq.last != Finish()) {
        val goldAction = oracle.goldActions(stateSeq.last)(0)
        oracleSeq += goldAction
        if (goldAction != Finish())
          stateSeq += stateSeq.last.proceed(goldAction, true)
      }
      stateSeq zip oracleSeq
    }

    var allCorrect = true
    stateOracleSeq foreach { case (state, goldAction) =>
      val goldLabel = goldAction.toLabel
      val unlabeledFeatures = extractors.extractUnlabeledFeatures(sentence, state)
      val examples = possibleActions(state, sentence).map { action =>
        val featureIdxs = unlabeledFeatures.map { _.assignLabel(action.toLabel) }.map { indexer.getIndex(_) }.toArray
        Example(featureIdxs, action.toLabel)
      }
      val predict = classifier.predict(examples)._1
      classifier.update(examples, goldLabel)
      classifier.c -= 1.0
      allCorrect = allCorrect && predict == goldLabel
    }
    classifier.c += 1.0 // only update c when one sentence is processed
    allCorrect
  }
  def predict(sentence: CandAssignedSentence): Derivation = sys.error("Please use BeamSearchDecoder with k=1 when predict!")
}
