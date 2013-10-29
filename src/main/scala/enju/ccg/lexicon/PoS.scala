package enju.ccg.lexicon

case class Conjugation(override val id:Int, override val v:String) extends Numbered[String]
case class FineTag(override val id:Int, override val v:String) extends Numbered[String]

sealed trait PoS extends Numbered[String] {
  def conj:Conjugation = throw new RuntimeException("conj is not defined in this PoS class.")
  def hierar:Seq[FineTag] = throw new RuntimeException("hierar is not defined in this PoS class.")
  def first = hierar(0)
  def second = if (hierar.size < 2) first else hierar(1)
  def third = if (hierar.size < 3) second else hierar(2)
}
case class SimplePoS(override val id:Int, override val v:String) extends PoS {
  override def toString = v
}
case class JapanesePoS(override val id:Int,
                       override val v:String,
                       override val conj:Conjugation,
                       override val hierar:Seq[FineTag]) extends PoS {
  override def toString = v
}
