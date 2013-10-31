package enju.ccg.lexicon

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

class TaggedSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS]) extends Sentence(wordSeq) with BaseFormSeq with PoSSeq {
  require (wordSeq.size == posSeq.size)

  def this(s:Sentence, baseSeq:Seq[Word], posSeq:Seq[PoS]) = this(s.wordSeq, baseSeq, posSeq)
  override def size = wordSeq.size
  def assignCandidates(candSeq:Seq[Seq[Category]]):CandAssignedSentence = new TestSentence(this, candSeq)
}

class GoldSuperTaggedSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val catSeq:Seq[Category]) extends TaggedSentence(wordSeq, baseSeq, posSeq) with GoldTagSeq {
  require (wordSeq.size == posSeq.size && posSeq.size == catSeq.size)

  def this(s:TaggedSentence, catSeq:Seq[Category]) = this(s.wordSeq, s.baseSeq, s.posSeq, catSeq)
  override def size = wordSeq.size
  override def assignCandidates(candSeq:Seq[Seq[Category]]):CandAssignedSentence = new TrainSentence(this, candSeq)
}

trait CandAssignedSentence extends TaggedSentence with CategoryCandSeq

case class TrainSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val catSeq:Seq[Category],
  override val candSeq:Seq[Seq[Category]]) extends GoldSuperTaggedSentence(wordSeq, baseSeq, posSeq, catSeq) with CandAssignedSentence {
  require (wordSeq.size == posSeq.size && posSeq.size == catSeq.size &&  catSeq.size == candSeq.size)
  
  def this(s:GoldSuperTaggedSentence, candSeq:Seq[Seq[Category]]) = this(s.wordSeq, s.baseSeq, s.posSeq, s.catSeq, candSeq)
  override def size = wordSeq.size
}

case class TestSentence(
  override val wordSeq:Seq[Word],
  override val baseSeq:Seq[Word],
  override val posSeq:Seq[PoS],
  override val candSeq:Seq[Seq[Category]]) extends TaggedSentence(wordSeq, baseSeq, posSeq) with CandAssignedSentence {
  require (wordSeq.size == posSeq.size && posSeq.size == candSeq.size)
  
  def this(s:TaggedSentence, candSeq:Seq[Seq[Category]]) = this(s.wordSeq, s.baseSeq, s.posSeq, candSeq)
  override def size = wordSeq.size
}
