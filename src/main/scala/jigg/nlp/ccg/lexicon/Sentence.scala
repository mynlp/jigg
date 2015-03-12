package jigg.nlp.ccg.lexicon

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

// TODO: for output purpose, it is more convenient if sentence has Seq[String], which preserves the sequence of original surface forms.

trait hasSize {
  def size:Int
}
trait WordSeq extends hasSize {
  def wordSeq:Seq[Word]
  def word(i:Int) = wordSeq(i)
}
trait BaseFormSeq extends hasSize {
  def baseSeq:Seq[Word]
  def base(i:Int) = baseSeq(i)
}
trait PoSSeq extends hasSize {
  def posSeq:Seq[PoS]
  def pos(i:Int) = posSeq(i)
}
trait GoldTagSeq extends hasSize {
  def catSeq:Seq[Category]
  def cat(i:Int) = catSeq(i)
}
trait CategoryCandSeq  extends hasSize {
  def candSeq:Seq[Seq[Category]]
  def cand(i:Int) = candSeq(i)
}

class Sentence(override val wordSeq:Seq[Word]) extends WordSeq {
  override def size = wordSeq.size
}

trait TaggedSentence extends Sentence with BaseFormSeq with PoSSeq {
  type AssignedSentence <: CandAssignedSentence
  def assignCands(candSeq:Seq[Seq[Category]]): AssignedSentence
}

class PoSTaggedSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS]) extends Sentence(wordSeq) with TaggedSentence {
  require (wordSeq.size == posSeq.size)

  def this(s:Sentence, baseSeq:Seq[Word], posSeq:Seq[PoS]) = this(s.wordSeq, baseSeq, posSeq)
  override def size = wordSeq.size

  override type AssignedSentence = TestSentence
  def assignCands(candSeq:Seq[Seq[Category]]): AssignedSentence = new TestSentence(this, candSeq)
}

class GoldSuperTaggedSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val catSeq:Seq[Category]) extends Sentence(wordSeq) with TaggedSentence with GoldTagSeq {
  require (wordSeq.size == posSeq.size && posSeq.size == catSeq.size)

  def this(s:TaggedSentence, catSeq:Seq[Category]) = this(s.wordSeq, s.baseSeq, s.posSeq, catSeq)
  override def size = wordSeq.size

  override type AssignedSentence = TrainSentence
  def assignCands(candSeq:Seq[Seq[Category]]): AssignedSentence = new TrainSentence(this, candSeq)
}

trait CandAssignedSentence extends TaggedSentence with CategoryCandSeq

case class TestSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val candSeq:Seq[Seq[Category]]) extends PoSTaggedSentence(wordSeq, baseSeq, posSeq) with CandAssignedSentence {
  require (wordSeq.size == posSeq.size && posSeq.size == candSeq.size)

  def this(s:PoSTaggedSentence, candSeq:Seq[Seq[Category]]) = this(s.wordSeq, s.baseSeq, s.posSeq, candSeq)
  override def size = wordSeq.size
}

case class TrainSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val catSeq:Seq[Category],
  override val candSeq:Seq[Seq[Category]]) extends GoldSuperTaggedSentence(wordSeq, baseSeq, posSeq, catSeq) with CandAssignedSentence {
  require (wordSeq.size == posSeq.size && posSeq.size == catSeq.size &&  catSeq.size == candSeq.size)

  def this(s:GoldSuperTaggedSentence, candSeq:Seq[Seq[Category]]) = this(s.wordSeq, s.baseSeq, s.posSeq, s.catSeq, candSeq)
  override def size = wordSeq.size

  def containsNoGoldWord = catSeq.contains(None)
  def numCandidatesContainGold = candSeq.zip(catSeq).foldLeft(0) {
    case (n, (cand, gold)) if (cand.contains(gold)) => n + 1
    case (n, _) => n
  }

  def pickUpGoldCategory: TrainSentence = {
    val newCandSeq = candSeq.zip(catSeq).map { case (cand, gold) =>
      if (cand.contains(gold)) cand else cand :+ gold
    }
    this.copy(candSeq = newCandSeq);
  }
}
