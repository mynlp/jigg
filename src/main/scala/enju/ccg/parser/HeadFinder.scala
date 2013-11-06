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
  def get(left:NodeInfo, right:NodeInfo) = Right // (left, right) match {
  //   case (JapanesePoS(_,vL,conjL,hierarL), JapanesePoS(_,vR,conjR,hierarR)) => Right
  //   case _ => throw new RuntimeException("JapaneseHeadFinder should be used with JapanesePoS")
  // }
}
