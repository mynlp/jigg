package enju.ccg.parser
import enju.ccg.lexicon.{PoS, JapanesePoS, Category}
import enju.ccg.lexicon.Direction._

trait HeadFinder {
  type NodeInfo = HeadFinder.NodeInfo
  def get(left:NodeInfo, right:NodeInfo): Direction
}
object HeadFinder {
  case class NodeInfo(pos:PoS, category:Category, headCategory:Category)
}

// TODO: write rule; currently deterministic
object EnglishHeadFinder extends HeadFinder { def get(left:NodeInfo, right:NodeInfo) = Right }

object JapaneseHeadFinder extends HeadFinder {
  val Symbol = "記号"
  def get(left:NodeInfo, right:NodeInfo) = {
    val leftPos = left.pos.first.v
    val rightPos = right.pos.first.v
    if (rightPos == Symbol) Left else Right
  }
}
