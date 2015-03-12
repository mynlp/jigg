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

/**
 * Internal representation of Pat-of-Speech.
 * The trait gives some methods to access to the information, which might be used in some languages.
 * For example, hierar is a sequence of FineTag, which is assmed to represent hierarchy of that PoS.
 * To enable using these different types of tags transparently (it is useful in e.g., feature extractions), Conjugation or FineTag itself is also PoS.
 * WARNING: all PoSs have to have unique ids to be distinguished, so it assmed that surface forms of conj, hierar, and pos itself (full surface) are disjoint; if, for example, a FineTag have the same surface to a Conjugation, the dictionary discards the latter one. One solution to this problem is to add a symbol to each type of PoS, e.g., adding suffix 'F' to all FineTag instances when draw/inserting the dictionary.
 */
sealed trait PoS extends Numbered[String] {
  def conj:PoS = sys.error("conj is not defined in this PoS class.")
  def hierar:Seq[PoS] = sys.error("hierar is not defined in this PoS class.")
  def hierarConj:Seq[PoS] = sys.error("hierarConj is not defined in this PoS class.")
  def first = hierar(0)
  def second = if (hierar.size < 2) first else hierar(1)
  def third = if (hierar.size < 3) second else hierar(2)

  def firstWithConj = hierarConj(0)
  def secondWithConj = if (hierarConj.size < 2) firstWithConj else hierarConj(1)
  def thirdWithConj = if (hierarConj.size < 3) secondWithConj else hierarConj(2)
}
trait OptionalPoS extends PoS
trait MainPoS extends PoS

case class Conjugation(override val id:Int, override val v:String) extends OptionalPoS {
  override def toString = v
}
case class FineTag(override val id:Int, override val v:String) extends OptionalPoS {
  override def toString = v
}
case class FineWithConjugation(override val id:Int, override val v:String) extends OptionalPoS {
  override def toString = v
}
case class SimplePoS(override val id:Int, override val v:String) extends MainPoS {
  override def toString = v
}
case class JapanesePoS(override val id:Int,
                       override val v:String,
                       override val conj:PoS,
                       override val hierar:Seq[PoS],
                       override val hierarConj:Seq[PoS]) extends MainPoS {
  override def toString = v
}
