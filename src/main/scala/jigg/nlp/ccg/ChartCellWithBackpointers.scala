package jigg.nlp.ccg

import jigg.nlp.ccg.lexicon._
import scala.collection.mutable.HashMap

/**
 * Implements a backpointer for a ChartCell for tracing trees (i.e. derivation process).
 * Consists of a Tuple2[Int, Int], indicating the range of the ChartCell, and the Category
 * associated with the pointed ChartCell.
 */
class BackPointer(tup: Tuple2[Int, Int], symbol: Category){
  val sFromTo: Tuple2[Int, Int] = tup
  val associatedCat: Category = symbol
   
  /** Gets spansFrom of the associated Cell. */
  def getSpansFrom(): Int = sFromTo._1
  
  /** Gets spansTo of the associated Cell. */
  def getSpansTo(): Int = sFromTo._2
  
  override def toString: String = {
    val s:StringBuilder = new StringBuilder("[" + sFromTo._1 + ", " + sFromTo._2 + "] is (" + associatedCat.toString + ")")
    s.toString
  }
  
  def isEmpty() = if(tup._1 == -1 && tup._2 == -1) true else false
}

/**
 * Implements a ChartCell with BackPointers.
 * Specifically, the 4th argument contains a Map that maps the potential categories of this Cell
 * to a Set of BackPointer-Tuples (since every Cell/Category is produced by two other Cells).
 */
class ChartCellWithBackpointers(token:String, i: Int, j: Int, possibleCat: HashMap[(Category, Double), Set[(BackPointer, BackPointer)]]){
  private var associatedToken: String = token
  val spansFrom:Int = i
  val spansTo:Int = j
  val candidateCats:HashMap[(Category, Double), Set[(BackPointer, BackPointer)]] = possibleCat
  
  /** Simplifies the query if Cell is empty to query if there are no candidate categories. */
  def isEmpty(): Boolean = if(candidateCats.isEmpty) true else false
  
  def setToken(t:String) { this.associatedToken = t }
  def getToken() = associatedToken
  
  /**
   * Adds just a category to the Cell without BackPointers, meaning that its the lowermost Cell
   * (i.e. it contains the token, which is derived from no other Cell).
   */
  def addOnlyCandidate(cand:Category, prob: Double) { candidateCats += ((cand, prob) -> Set[(BackPointer, BackPointer)]()) }
  
  /**
   * Standard addCandidate method.
   * Adds to the Cell a candidate category with two BackPointers associated to the Cells
   * that result in this candidate category.
   */
  def addCandidate(cand:Category, prob: Double, bp1: BackPointer, bp2: BackPointer) = candidateCats.get(cand, prob) match {
    case None => candidateCats += ((cand, prob) -> Set((bp1, bp2)))  
    case Some(tupleSet) => {
      var alreadyPresent: Boolean = false
      for(tupel <- tupleSet){
        if(bp1.sFromTo.equals(tupel._1.sFromTo) && bp2.sFromTo.equals(tupel._2.sFromTo)){
          if(bp1.associatedCat.toString == tupel._1.associatedCat.toString && bp2.associatedCat.toString == tupel._2.associatedCat.toString){
            alreadyPresent = true
          }
        }
      }
      if(!alreadyPresent){
        candidateCats((cand, prob)) += Tuple2(bp1, bp2)
      }
    }
  }
  
  /**
   * Taking a category as input, gets the set of BackPointers that indicate where this category
   * has been derived from.
   */
  def getCellsForCandidate(cand:Category, prob: Double):Set[Tuple2[BackPointer, BackPointer]] = candidateCats.get((cand, prob)) match {
    case None => Set[Tuple2[BackPointer, BackPointer]]()
    case Some(tupleSet) => tupleSet
  }
  
  override def toString: String = {
    val s = new StringBuilder("Cell [" + spansFrom + ", " + spansTo + "] (" + this.associatedToken + ") contains " + candidateCats.toString)
    s.toString
  }
}
