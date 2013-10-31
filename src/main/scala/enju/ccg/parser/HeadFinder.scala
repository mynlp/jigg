package enju.ccg.parser
import enju.ccg.lexicon.{PoS, JapanesePoS}
import enju.ccg.lexicon.Direction._

trait HeadFinder {
  def get(left:PoS, right:PoS):Direction
}

// TODO: write rule; currently deterministic
object EnglishHeadFinder extends HeadFinder { def get(left:PoS, right:PoS) = Right }

object JapaneseHeadFinder extends HeadFinder {
  def get(left:PoS, right:PoS) = (left, right) match {
    case (JapanesePoS(_,vL,conjL,hierarL), JapanesePoS(_,vR,conjR,hierarR)) => Right
    case _ => throw new RuntimeException("JapaneseHeadFinder should be used with JapanesePoS")
  }
}
