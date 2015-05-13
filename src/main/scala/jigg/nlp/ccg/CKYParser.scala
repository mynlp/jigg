package jigg.nlp.ccg

import jigg.nlp.ccg.lexicon._
import jigg.nlp.ccg.lexicon.Slash._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
 * Implements a CKY-based parser that has backpointers for tracing trees.
 * Takes a @CCGrammar as input.
 */
class CKYParser(grammar: CCGrammar){
  private[this] val rules:Set[_ <: CCGRules] = grammar.rules
  private[this] val features:Set[AtomicCategory] = grammar.atomicCategories
  
  /**
   * Parses a super-tagged sentence according to the CKY algorithm.
   */
  def parseSentence(sentence: TaggedSentence, sentenceInfo: Array[Array[(Category, Float)]]): Array[Array[ChartCellWithBackpointers]] = {
    val startTime = System.nanoTime()
    val chart = Array.ofDim[ChartCellWithBackpointers](sentence.size, sentence.size + 1)    // initiate Chart with n x n+1 dimensions
        
    /* Fill the chart with Cells that contain no associated tokens and no candidate categories.*/
    for(i <- 0 to sentence.size - 1){
      for(j <- 1 to sentence.size){
        chart(i)(j) = new ChartCellWithBackpointers("", i, j, HashMap[(Category, Double), Set[(BackPointer, BackPointer)]]())
      }
    }
    
    /* Add to each 'token Cell' its corresponding token and candidate categories.*/
    for(i <- 0 to sentence.size - 1){
      val j = i + 1
      val currToken = sentence.word(i).toString
      chart(i)(j).setToken(currToken)
      
      val possCats = sentenceInfo(i)
      for(cat <- possCats){
        chart(i)(j).addOnlyCandidate(cat._1, cat._2)
      }
    }
    
    /*
     * Commence filling rest. Moves columns (from second one) to the right.
     * Moves all the Cells in each column bottom-up.
     */
    for(index <- 2 to sentence.size){
      for(j <- index - 1 to 0 by - 1){
        val currs = chart(j)(index).candidateCats.keySet.toArray
          
        var currID = 0
        while(currID < currs.length){        // Using 'while' instead of 'for' for speed reasons.
          val currentCatPair = currs(currID)
          val currentCat = currentCatPair._1
          val currentProb = currentCatPair._2
        
          var isBinaryApplicable:Boolean = false
           /* Applies to everything that is not [0, x], i.e. which can potentially have a left side of a binary rule.*/
          if(j != 0){
             /* Goes through all (relevant) cells in lines above current one.*/
            for(j2 <- j - 1 to 0 by -1){
              val cands = chart(j2)(j).candidateCats.keySet.toArray
              
              var candID = 0
              while(candID < cands.length){
                val candidCatPair = cands(candID)
                val candidCat = candidCatPair._1
                val candidProb = candidCatPair._2

                /* Go through every binary rule.*/
                for(brule <- rules.filter(_.isInstanceOf[BinaryRule])){
                  if(brule.asInstanceOf[BinaryRule].isApplicable(candidCat, currentCat)){
                    val resultCat = brule.asInstanceOf[BinaryRule].apply(candidCat, currentCat)
                    val resultProb = currentProb + candidProb
                    chart(j2)(index).addCandidate(resultCat, resultProb, new BackPointer(Tuple2(j2, j), candidCat), new BackPointer(Tuple2(j, index), currentCat))
                    isBinaryApplicable = true
                  }
                }
                candID += 1
              }
            }
          }
          /*
           * If current category can potentially be type raised (i.e. it starts with "S"),
           * none of the binary rules could be applied AND current cell is not the upper right one
           * (i.e. spanning the whole sentence), apply unary rules.
           */
          if(currentCat.toString.startsWith("S") && !isBinaryApplicable && !(j == 0 && index == sentence.size)){
            for(urule <- rules.filter(_.isInstanceOf[UnaryRule])){
              if(urule.asInstanceOf[UnaryRule].isApplicable(currentCat)){
                val res:Set[ComplexCategory] = urule.asInstanceOf[UnaryRule].apply(currentCat, features)
                for(cc <- res){
                  /*
                   * Because type-raising means that category was derived from only one Cell instead of two, assign an impossible backpointer
                   * as second backpointer. Class BackPointer's "isEmpty" checks for this.
                   * When tracing Parse Trees, if second bp is empty, Unary Tree is constructed.
                   */
                  chart(j)(index).addCandidate(cc, currentProb, new BackPointer(Tuple2(j, index), currentCat), new BackPointer(Tuple2(-1, -1), currentCat))
                }
              }
            }
          }
          currID += 1
        }
      }
    }
    val endTime = System.nanoTime()
    val resultTime: Double = (endTime - startTime)/1000000000d
    println("\nParsing took "+ resultTime + " s")
    chart
  }
  
  /**
   * Prints the filled chart.
   */
  def printChart(chart: Array[Array[ChartCellWithBackpointers]]): Unit = {
    for(i <- 0 to chart.size - 1){
      for(j <- 1 to chart.size){ println(chart(i)(j).toString) }
    }
  }
  
  type Tree = ParseTree[String]
  
  /*
  def printParseTrees(chart: Array[Array[ChartCellWithBackpointers]]): Unit = {
    if(chart(0)(chart.size).isEmpty()){ Console.err.println("No parse for the whole sentence!") }
    else{
      for(rt <- chart(0)(chart.size).candidateCats.keySet if rt.isInstanceOf[AtomicCategory] && (rt.toString.startsWith("S") || rt.toString.startsWith("NP"))){
        val result:ArrayBuffer[Tree] = traceParse(chart, rt, 0, chart.size)
        for(tree <- result.toList){
          println(tree.toString()) 
        }
      }
    }
  }
  * 
  */
  
  def getMostProbableParseTrees(chart: Array[Array[ChartCellWithBackpointers]]): List[Tree] = {
    if(chart(0)(chart.size).isEmpty()){
      Console.err.println("No parse for the whole sentence!")
      List[Tree]()
    }
    else{
      val resultBuffer = ArrayBuffer[Tuple2[Tree, Double]]()
      for(rt <- chart(0)(chart.size).candidateCats.keySet){
        val mostProbableRoot = chart(0)(chart.size).candidateCats.keySet.reduceLeft((x,y) => if(x._2 > y._2) x else y)
        //val mostProbableRoot = rt        // WEG
        val result = traceMostProbableParse(chart, mostProbableRoot._1, 0, chart.size)
        val resultList = result.toList
        for(el <- resultList){
          resultBuffer += el
        }
      }
      val list = resultBuffer.toList
      if(list.size == 1){
        List(list.head._1)
      }
      else{
        val mostProbableTreePair = list.reduceLeft((x,y) => if(x._2 > y._2) x else y)
        List(mostProbableTreePair._1)
      }
    }
  }
  
  def traceMostProbableParse(chart: Array[Array[ChartCellWithBackpointers]], start:Category, spansFrom: Int, spansTo: Int): ArrayBuffer[Tuple2[Tree, Double]] = {
    val ptList = ArrayBuffer[Tuple2[Tree, Double]]()
    if(spansTo == spansFrom + 1){
      val nTree = new LeafTree(start.toString + ' ' + chart(spansFrom)(spansTo).getToken)
      val cats = chart(spansFrom)(spansTo).candidateCats.keySet
      val tempSet = cats.filter(_.toString == start.toString)
      if(!tempSet.isEmpty){
        val temp = tempSet.head
        /* This is the probability of the category of the token. */
        val nProb = temp._2
      
        ptList += (Tuple2(nTree, nProb))
        ptList
      }
      else{
        ptList += (Tuple2(nTree, 0.0))
        ptList
      }
    }
    else{
      val catsInChartCell = chart(spansFrom)(spansTo).candidateCats.keySet  
      val temp = catsInChartCell.filter(_._1.toString == start.toString).head // THIS SHOULD ONLY CONTAIN ONE!
      val thisCat = temp._1
      val thisProb = temp._2
      
      val bp = chart(spansFrom)(spansTo).candidateCats(temp)
      val bps = bp.head
      //for(bps <- bp){
        val leftSpansFrom = bps._1.getSpansFrom()
        val leftSpansTo = bps._1.getSpansTo()
        val leftAssocCat = bps._1.associatedCat
        if(bps._2.isEmpty){
          val unarySide = traceMostProbableParse(chart, leftAssocCat, leftSpansFrom, leftSpansTo)
          
          // gets most probable tree
          val x = unarySide.reduceLeft((x,y) => if(x._2 > y._2) x else y)
          val nTree = new UnaryTree(x._1, start.toString)
          ptList += (Tuple2(nTree, x._2))
        }
        else{
          val rightSpansFrom = bps._2.getSpansFrom()
          val rightSpansTo = bps._2.getSpansTo()
          val rightAssocCat = bps._2.associatedCat
            
          val oneSide:ArrayBuffer[Tuple2[Tree, Double]] = traceMostProbableParse(chart, leftAssocCat, leftSpansFrom, leftSpansTo)
          val otherSide:ArrayBuffer[Tuple2[Tree, Double]] = traceMostProbableParse(chart, rightAssocCat, rightSpansFrom, rightSpansTo)
          
          val label = start.toString
          
          // gets most probable trees
          val mptLeft = oneSide.reduceLeft((x,y) => if(x._2 > y._2) x else y)
          val mptRight = otherSide.reduceLeft((x,y) => if(x._2 > y._2) x else y)
          val nTree = new BinaryTree(mptLeft._1, mptRight._1, label)
          val nProb = mptLeft._2 * mptRight._2
          ptList += (Tuple2(nTree, nProb)) 
        }
      //}
      val bestParse = ptList.reduceLeft((x,y) => if(x._2 > y._2) x else y)
      ArrayBuffer(bestParse)
    }
  }
}