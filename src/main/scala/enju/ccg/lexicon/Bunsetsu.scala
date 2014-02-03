package enju.ccg.lexicon

trait BunsetsuBase {
  def offset: Int
  def wordSeq: Seq[Word]
  def posSeq: Seq[PoS]

  def shuji: Int
  def gokei: Int
  def includesParens: Boolean
  def includesPuncs: Boolean
  def word(i: Int): Word = if (i < wordSeq.size) wordSeq(i) else wordSeq.last
  def pos(i: Int): PoS = if (i < posSeq.size) posSeq(i) else posSeq.last
  def size = wordSeq.size
}

// TODO: Currently the settings below are hard-coded.
// Do we have to support other PoS systems as well by abstracting these strings?
case class Bunsetsu(
  override val offset: Int,
  override val wordSeq: Seq[Word],
  override val posSeq: Seq[PoS]
  ) extends BunsetsuBase {
  val shuji = posSeq.lastIndexWhere { p => p.first.v != "記号" && p.first.v != "助詞" && p.first.v != "接尾辞" }
  val gokei = posSeq.lastIndexWhere { _.first.v != "記号" }
  val includesParens = posSeq.indexWhere { _.second.v.startsWith("記号-括弧") } != -1
  val includesPuncs = posSeq.lastIndexWhere { p => p.second.v == "記号-読点" || p.second.v == "記号-句点" } != -1
}

trait BunsetsuSeq extends hasSize {
  def bunsetsuSeq: Seq[Bunsetsu]
  def apply(i: Int) = bunsetsuSeq(i)
  def size = bunsetsuSeq.size
}

case class BunsetsuSentence(override val bunsetsuSeq: Seq[Bunsetsu]) extends BunsetsuSeq {
  def parseWithCCGDerivation(derivation: Derivation): ParsedBunsetsuSentence = {
    val subTreeHeadMap: Array[Array[Int]] = derivation.map.map { _.map { _ => -1 } }
    val headSeq = Array.fill(derivation.map.size)(-1)

    def fillHeadsBottomup(root: Point) = {
      derivation.foreachPointBottomup({ p => derivation.get(p) match {
        case Some(AppliedRule(BinaryChildrenPoints(left, right), _)) =>
          def head(p: Point): Int = subTreeHeadMap(p.x)(p.y)
          subTreeHeadMap(p.x)(p.y) = head(right) // cache value for parent computations
          headSeq(head(left)) = head(right)
        case Some(AppliedRule(NoneChildPoint(), _)) => subTreeHeadMap(p.x)(p.y) = p.x
        case _ =>
      } }, root)
    }
    derivation.roots.foreach { r => fillHeadsBottomup(r) }

    val word2bunsetsuIdx: Array[Int] = (0 until size).flatMap { i => this(i).wordSeq.map { _ => i } }.toArray
    val bunsetsuDepsSeq: Seq[Seq[Int]] = (0 until size).map { head =>
      val headBunsetsu = this(head)
      def inHeadRange(word: Int): Boolean = word >= headBunsetsu.offset && word < headBunsetsu.offset + headBunsetsu.size
      def containsLinkToHead(idx: Int): Boolean = {
        val bunsetsu = this(idx)
        (bunsetsu.offset until bunsetsu.offset + bunsetsu.size).indexWhere { i => inHeadRange(headSeq(i)) } != -1
      }

      (0 until head).filter { containsLinkToHead(_) }
    }
    val bunsetsuHeadSeq = Array.fill(size)(-1)

    bunsetsuDepsSeq.zipWithIndex.foreach { case (deps, head) => deps.foreach { d =>
      val existingHead = bunsetsuHeadSeq(d) // sometimes, d already have a head in another position
      if (existingHead != -1) bunsetsuHeadSeq(d) = Math.min(existingHead, head)
      else bunsetsuHeadSeq(d) = head
    } }
    ParsedBunsetsuSentence(bunsetsuSeq, bunsetsuHeadSeq)
  }
}

case class ParsedBunsetsuSentence(
  override val bunsetsuSeq: Seq[Bunsetsu],
  val headSeq: Seq[Int]) extends BunsetsuSeq {

  def head(i: Int) = headSeq(i)

  def renderInCabocha: String = headSeq.zipWithIndex.zip(bunsetsuSeq).map { case ((h, i), bunsetsu) =>
    val depStr = "* " + i + " " + h + "D"
    (depStr :: (0 until bunsetsu.size).toList.map { i => bunsetsu.word(i) + "\t" + bunsetsu.pos(i) }).mkString("\n")
  }.mkString("\n")
}
