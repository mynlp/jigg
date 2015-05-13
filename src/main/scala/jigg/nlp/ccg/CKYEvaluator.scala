package jigg.nlp.ccg

import jigg.nlp.ccg.lexicon._
import scala.collection.mutable.ArrayBuffer

/**
 * Implements an evalutor for the cky output parse trees.
 * Works only for unlabeled precision, recall and f-score.
 */
class CKYEvaluator {
  type Tree = ParseTree[String]
  
  private def calculatePrecision(parserTree: Tree, gold: Tree): Double = {
    val constGold = getConstituents(gold, new ArrayBuffer[Tree]())
    val constInput = getConstituents(parserTree, new ArrayBuffer[Tree]())
    
    val numberOfInputConst = constInput.size
    val numberOfCorrectInInput = corrConstitInInput(constInput, constGold)

    val precision = numberOfCorrectInInput / numberOfInputConst
    precision
  }
  
  def calculatePrecisions(parser: List[ParseTree[String]], gold: Tree) = for(el <- parser) yield calculatePrecision(el, gold)
  
  private def calculateRecall(gold: Tree, parserTree: Tree): Double = {
    val constGold = getConstituents(gold, new ArrayBuffer[Tree]())
    val constInput = getConstituents(parserTree, new ArrayBuffer[Tree]())
    
    val numberOfGoldConst = constGold.size
    val numberOfCorrectOfGold = corrConstitOfGold(constGold, constInput)
    
    val recall = numberOfCorrectOfGold / numberOfGoldConst
    recall
  }
  
  def calculateRecalls(gold: Tree, parser: List[ParseTree[String]]) = for(el <- parser) yield calculateRecall(gold, el)
  
  private def calculateFScore(parserTree: Tree, gold: Tree): Double = {
    val precision = calculatePrecision(parserTree, gold)
    val recall = calculateRecall(gold, parserTree)
    if(precision + recall == 0.0 ){ 0.0 }
    else{
      val fscore = (2 * precision * recall) / (precision + recall)
      fscore
    }
  }
  
  def calculateFScores(parser: List[ParseTree[String]], gold: Tree) = for(el <- parser) yield calculateFScore(el, gold)
    
  /**
   * Gets the constituents of the tree.
   * (S((NP((DT the) (NN man)) (VP eats))) has 2 constituents: S -> NP VP and NP -> DT NN
   */
  private def getConstituents(input: Tree, constit: ArrayBuffer[Tree]): ArrayBuffer[Tree] = {
    if(input.children.size == 0){
      constit
    }
    else if(input.children.size == 1){
      constit += new UnaryTree(new LeafTree(input.children(0).label), input.label)
      constit ++= getConstituents(input.children(0), new ArrayBuffer[Tree]())
      constit
    }
    else{
      constit += new BinaryTree(new LeafTree(input.children(0).label), new LeafTree(input.children(1).label), input.label)
      constit ++= getConstituents(input.children(0), new ArrayBuffer[Tree]())
      constit ++= getConstituents(input.children(1), new ArrayBuffer[Tree]())
      constit
    }
  }
  
 /**
  * Checks how many constituents of the parser tree are in the gold tree.
  */
  private def corrConstitInInput(parser: ArrayBuffer[Tree], gold: ArrayBuffer[Tree]): Double = {
    var result = 0.0
    var alreadyMatched = Set[Tree]()
    for(el <- parser){
      for(el2 <- gold){
        if(el.getClass() == el2.getClass() && !alreadyMatched.contains(el)){
          val cleanParserLabel = cleanLabel(el.label)
          val parserLabel = changeFeatureOrdering(cleanParserLabel)
          el match{
            case l: LeafTree[String] => {
              if(parserLabel == cleanLabel(el2.label)){
                result += 1.0
                alreadyMatched += el
              }
            }
            case u: UnaryTree[String] => {
              val parserChild = u.child
              val goldChild = el2.asInstanceOf[UnaryTree[String]].child
              
              val cleanParserChildLabel = cleanLabel(parserChild.label)
              val parserChildLabel = changeFeatureOrdering(cleanParserChildLabel)
              
              if(parserLabel == cleanLabel(el2.label) && parserChildLabel == cleanLabel(goldChild.label)){
                result += 1.0
                alreadyMatched += el
              }
            }
            case b: BinaryTree[String] => {
              val parserChildLeft = b.left
              val parserChildRight = b.right
              val goldChildLeft = el2.asInstanceOf[BinaryTree[String]].left
              val goldChildRight = el2.asInstanceOf[BinaryTree[String]].right
              
              val cleanParserChildLeftLabel = cleanLabel(parserChildLeft.label)
              val cleanParserChildRightLabel = cleanLabel(parserChildRight.label)
              val parserChildLeftLabel = changeFeatureOrdering(cleanParserChildLeftLabel)
              val parserChildRightLabel = changeFeatureOrdering(cleanParserChildRightLabel)
              
              if(parserLabel == cleanLabel(el2.label) && parserChildLeftLabel == cleanLabel(goldChildLeft.label) &&
                  parserChildRightLabel == cleanLabel(goldChildRight.label)){
                result += 1.0
                alreadyMatched += el
              }
            }
          }    
        }
      }
    }
    result
  }
  
  /**
  * Checks how many constituents of the gold tree are in the parser tree.
  * Very similar to @corrConstitInInput, only first two for-loops are reversed. Not very nice.
  */
  private def corrConstitOfGold(gold: ArrayBuffer[Tree], parser: ArrayBuffer[Tree]): Double = {
    var result = 0.0
    var alreadyFound = Set[Tree]()
    for(el <- gold){
      for(el2 <- parser){
        if(el.getClass() == el2.getClass() && !alreadyFound.contains(el)){
          val goldLabel = cleanLabel(el.label)
          val cleanParserLabel = cleanLabel(el2.label)
          val parserLabel = changeFeatureOrdering(cleanParserLabel)
          
          el match{
            case l: LeafTree[String] => {
              if(goldLabel == parserLabel){
                result += 1.0
                alreadyFound += el
              }
            }
            case u: UnaryTree[String] => {
              val goldChild = u.child
              val parserChild = el2. asInstanceOf[UnaryTree[String]].child
              
              val cleanParserChildLabel = cleanLabel(parserChild.label)
              val parserChildLabel = changeFeatureOrdering(cleanParserChildLabel)
              
              if(goldLabel == parserLabel && cleanLabel(goldChild.label) == parserChildLabel){
                result += 1.0
                alreadyFound += el
              }
            }
            case b: BinaryTree[String] => {
              val goldChildLeft = b.left
              val goldChildRight = b.right
              val parserChildLeft = el2.asInstanceOf[BinaryTree[String]].left
              val parserChildRight = el2.asInstanceOf[BinaryTree[String]].right
              
              val cleanParserChildLeftLabel = cleanLabel(parserChildLeft.label)
              val cleanParserChildRightLabel = cleanLabel(parserChildRight.label)
              val parserChildLeftLabel = changeFeatureOrdering(cleanParserChildLeftLabel)
              val parserChildRightLabel = changeFeatureOrdering(cleanParserChildRightLabel)
              
              if(goldLabel == parserLabel && cleanLabel(goldChildLeft.label) == parserChildLeftLabel &&
                  cleanLabel(goldChildRight.label) == parserChildRightLabel){
                result += 1.0
                alreadyFound += el
              }
            }
          }
        }
      }
    }
    result
  }
  
  /**
   * Changes the feature ordering of NP-categories because the category construction in 'LexiconReader.scala'
   * results in switched NP-categories. So, in order to compare with CCGBank gold annotation on a String level,
   * NP[mod=nm,case=nc] will be changed to NP[case=nc,mod=nm].
   */
  private def changeFeatureOrdering(input: String): String = {
    if(input.startsWith("NP") && input.contains(',')){
      val feature1 = input.substring(input.indexOf("[") + 1, input.indexOf(","))
      val feature2 = input.substring(input.indexOf(",") + 1, input.indexOf("]"))
      val nString = new StringBuilder("NP")
      nString.append("[").append(feature2).append(",").append(feature1).append("]")
      nString.toString
    }
    else{ input }
  }
    
  /**
   * Cleans the CCGBank gold categories so that they are comparable with CKY categories.
   * In detail, it gets rid of any rule symbols at the beginning, {I1}, {I2}, or '_none' kind
   * of attachments.
   */
  private def cleanLabel(input: String): String = {
    val nString = {
      if(input.contains(" ")){
        val x = input.split(" ")
        // Guarantees that it's a sensible category
        if(input.matches("^(\\<|\\>)(\\w(\\d)?)?.*") || input.startsWith("ADNext") ||
                input.startsWith("ADV0") || input.startsWith("ADNint") || input.startsWith("SSEQ")){
          x(1)
        }
        else{ x(0) }
      } 
      else{ input }
    }
    if(nString.contains("(")){
      val nnString = nString.replaceFirst("\\(", "").reverse.replaceFirst("\\)", "").reverse
      val result = {
        if(nnString.contains("_")){ nnString.replaceAll("_\\w+", "") }
        else{ nnString }
      }
      val nresult = result.replaceAll("\\[\\w+=(X1|X2),\\w+=(X1|X2)\\]", "")
      val nnresult = nresult.replaceAll("\\w+=(X1|X2)", "")
      nnresult
    }
    else{
      val result = nString.replaceAll("\\[\\w+=(X1|X2),\\w+=(X1|X2)\\]", "")
      val nresult = result.replaceAll("\\w+=(X1|X2)", "")
      nresult
    }   
  }
}