package enju.ccg.parser
import enju.ccg.lexicon.{PoS, Word, Category}

trait hasSize {
  def size:Int
}

trait WordSeq extends hasSize {
  def wordSeq:Seq[Word]
  def word(i:Int) = wordSeq(i)
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

class TaggedSentence(
  override val wordSeq:Seq[Word],
  override val posSeq:Seq[PoS]) extends Sentence(wordSeq) with PoSSeq {
  require (wordSeq.size == posSeq.size)
  def this(s:Sentence, posSeq:Seq[PoS]) = this(s.wordSeq, posSeq)
  
  override def size = wordSeq.size
}

class GoldSuperTaggedSentence(
  override val wordSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val catSeq:Seq[Category]) extends TaggedSentence(wordSeq, posSeq) with GoldTagSeq {
  require (wordSeq.size == posSeq.size && posSeq.size == catSeq.size)

  def this(s:TaggedSentence, catSeq:Seq[Category]) = this(s.wordSeq, s.posSeq, catSeq)
  override def size = wordSeq.size
}

trait CandAssignedSentence extends TaggedSentence with CategoryCandSeq

case class TrainSentence(
  override val wordSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val catSeq:Seq[Category],
  override val candSeq:Seq[Seq[Category]]) extends GoldSuperTaggedSentence(wordSeq, posSeq, catSeq) with CandAssignedSentence {
  require (wordSeq.size == posSeq.size && posSeq.size == catSeq.size &&  catSeq.size == candSeq.size)
  
  def this(s:GoldSuperTaggedSentence, candSeq:Seq[Seq[Category]]) = this(s.wordSeq, s.posSeq, s.catSeq, candSeq)
  override def size = wordSeq.size
}

case class TestSentence(
  override val wordSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val candSeq:Seq[Seq[Category]]) extends TaggedSentence(wordSeq, posSeq) with CandAssignedSentence {
  require (wordSeq.size == posSeq.size && posSeq.size == candSeq.size)
  def this(s:TaggedSentence, candSeq:Seq[Seq[Category]]) = this(s.wordSeq, s.posSeq, candSeq)
  override def size = wordSeq.size
}
