package jigg.nlp.ccg

import jigg.nlp.ccg.lexicon._
import jigg.nlp.ccg.lexicon.Slash._
import scala.io.Source
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
 * Implements a lexicon reader for the latest ccgbank (ccgbank-20150216).
 * Works with 'CCGrammar.scala' and retrieves the lexicon in a format
 * usable with CKY parser.
 */
class LexiconReader{
  private[this] val terminalsBuffer = ArrayBuffer[String]()
  private[this] val acBuffer = ArrayBuffer[AtomicCategory]()
  private[this] val ccBuffer = ArrayBuffer[ComplexCategory]()
  private[this] val mapping = new HashMap[String, HashMap[String, Set[Category]]]
  
  /**
   * In addition to getting a set of all terminals (i.e. tokens) and atomic categories in the lexicon,
   * it also outputs a mapping from token to pos to a Set of associated categories.
   * The mapping is not used in current version of cky parser.
   */
  def getLexicon(path: String): Tuple3[Set[String], Set[AtomicCategory], HashMap[String, HashMap[String, Set[Category]]]] = {
    /* Main part. Pre-processes the lines and creates categories for each category string in line. */
    val source = Source.fromFile(path)
    for(line <- source.getLines()){
      if(!line.isEmpty){
        val contents = line.replaceAll("\\{I\\d\\}", "").replaceAll("_.*\\>", "").replaceAll("(,)?fin=(t|f)", "").split(" ")
        val cleanContents = Array.ofDim[String](contents.size)
        cleanContents(0) = contents(0)
        for(i <- 1 to contents.size - 1){
          cleanContents(i) = contents(i).substring(0, contents(i).indexOf("_"))
        }
        val token = cleanContents(0).substring(0, cleanContents(0).indexOf("/"))
        val pos = cleanContents(0).substring(cleanContents(0).indexOf("/") + 1, cleanContents(0).length)
        
        if(token != "@UNK@"){ terminalsBuffer += token }
      
        if(!mapping.contains(token)){
          mapping += (token -> HashMap[String, Set[Category]]())
          mapping(token) += (pos -> Set())
        }
        else if(mapping.contains(token) && !(mapping(token).contains(pos))){
          mapping(token) += (pos -> Set())
        }
        cleanContents.drop(1).foreach { x: String =>
          val cat = decomposeCategory(x)
          mapping(token)(pos) += cat
        }
      }
    }
    source.close()
    
    @serializable val result = (terminalsBuffer.toSet, acBuffer.toSet, mapping)
    result
  }
  
  /** Checks whether an atomic category in question is already present or not. */
  private def atomicCategoryExistent(acTemp: AtomicCategory) = acBuffer.filter(_.base == acTemp.base).exists(_.toString == acTemp.toString)
    
    
  /** For a String that is an atomic category, construct one. */
  private def getAtomicCategory(cat: String): AtomicCategory = {
    //println(cat)
    val base = cat.substring(0, cat.indexOf("["))
    val features = cat.substring(cat.indexOf("[") + 1, cat.indexOf("]")).split(",")
    val feat = for(f <- features) yield f.substring(f.indexOf("=") + 1, f.length)
      
    val nCatTemp = AtomicCategory(0, base, JPCategoryFeature.createFromValues(feat.toSeq))
    /* If there are no categories at all, take temporary one as first one (index = 0). */
    if(acBuffer.isEmpty){
      acBuffer += nCatTemp
      nCatTemp
    }
    else{
      /* If the category already exists, give out the existing one. */
      if(atomicCategoryExistent(nCatTemp)){
        acBuffer.find(_.toString == nCatTemp.toString) match{
          case None => AtomicCategory(-1, base, JPCategoryFeature.createFromValues(feat.toSeq))  // NEVER REACHED
          case Some(x) => x
        }
      }
      /* Else, construct new Category with new maximum ID. */
      else{
        val ids = for(ac <- acBuffer) yield ac.id
        val nCat = AtomicCategory(ids.max + 1, base, JPCategoryFeature.createFromValues(feat.toSeq))
        acBuffer += nCat
        nCat
      }
    }
  }
    
    
  /**
   * Checks whether a complex category in question is already present or not.
   * If so, returns the already existing one, if not, creates a new category
   * based on the input temporary category.
   */
  private def checkAndGetComplexCategory(ccTemp: ComplexCategory): ComplexCategory = {
    if(ccBuffer.exists(_.toString == ccTemp.toString)){
      ccBuffer.find(_.toString == ccTemp.toString) match{
        case None => ComplexCategory(-1, ccTemp.left, ccTemp.right, ccTemp.slash)  // NEVER REACHED
        case Some(x) => x
      }
    }
    else{
      val ids = for(cc <- ccBuffer) yield cc.id
      val nCat = ComplexCategory(ids.max + 1, ccTemp.left, ccTemp.right, ccTemp.slash)
      ccBuffer += nCat
      nCat
    }
  }
    
    
  /**
   * Breaks up a simple complex category X/Y where X and Y are atomic categories
   * and creates a complex category.
   */
  private def getComplexCategory(cat: String): ComplexCategory = {
    val compositeCat = cat.split("(/|\\\\)")
    val newLeft = getAtomicCategory(compositeCat(0))
    val newRight= getAtomicCategory(compositeCat(1))
    val nCat = {
      if(cat.matches(".+/.+")){ ComplexCategory(0, newLeft, newRight, Slash.Right) }
      else { ComplexCategory(0, newLeft, newRight, Slash.Left) }
    }
    if(ccBuffer.isEmpty){
      ccBuffer += nCat
      nCat
    }
    else{ checkAndGetComplexCategory(nCat) }
  }
    
    
  /**
   * Decomposes a symmetrical complex category. This means that both sides are complex catgeories.
   * 
   * ((NP[case=X1,mod=X2]/NP[case=X1,mod=X2]))\((NP[case=X1,mod=X2]/NP[case=X1,mod=X2]))
   * (((((S[mod=nm,form=stem]\NP[case=ga,mod=nm]))\NP[case=o,mod=nm]))\(NP[case=ni,mod=nm]/NP[case=ni,mod=nm]))
   */
  private def decomposeComplexSymmetrical(cat: String): ComplexCategory = {
    val PAT = "^(\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]((\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+)+".r
    var rareCase = false
    val leftSide = {
      PAT.findFirstIn(cat) match {
        case None => AtomicCategory(-1, "", JPCategoryFeature.createFromValues(Seq("")))    // NEVER REACHED
        case Some(x) => {
          val subCategory = x.replaceFirst("\\(", "").reverse.replaceFirst("\\)", "").reverse
          try{
            decomposeCategory(subCategory)
          }
          /*
           * This handles an extremely rare case (2 in whole training corpus).
           */
          catch{
            case e: java.lang.StringIndexOutOfBoundsException => {
              rareCase = true
              val PAT_ALT = "^((\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]((\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+)+)+((\\/|\\\\)((\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]((\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+)+)+)?".r.unanchored
              PAT_ALT.findFirstIn(cat) match{
                case None => AtomicCategory(-1, "", JPCategoryFeature.createFromValues(Seq("")))    // NEVER REACHED
                case Some(x) => {
                  val subCategory = x.replaceFirst("\\(", "").reverse.replaceFirst("\\)", "").reverse
                  decomposeCategory(subCategory)
                }
              }
            }
          }
        }
      }
    }
    val PAT2 = "(\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+$".r
    val rightSide = {
      if(!rareCase){
        PAT2.findFirstIn(cat) match {
          case None => AtomicCategory(-1, "", JPCategoryFeature.createFromValues(Seq("")))    // NEVER REACHED
          case Some(x) => {
            val subCategory = x.replaceFirst("\\(", "").reverse.replaceFirst("\\)", "").reverse
            decomposeCategory(subCategory)
          }
        }
      }
      else{
        println("We have a rare case!")
        val PAT2_ALT = "((\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]((\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+)+)+((\\/|\\\\)((\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]((\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+)+)+)?$".r.unanchored 
        PAT2_ALT.findFirstIn(cat) match {
          case None => AtomicCategory(-1, "", JPCategoryFeature.createFromValues(Seq("")))    // NEVER REACHED
          case Some(x) => {
            val subCategory = x.replaceFirst("\\(", "").reverse.replaceFirst("\\)", "").reverse
            decomposeCategory(subCategory)
          }
        }
      }
    }
    /* Get length of substring that matches left side of category. */
    val index = {
      PAT.findFirstIn(cat) match{
        case None => 0                // NEVER REACHED
        case Some(x) => x.length()
      }
    }
    /* Since above index points to the character after the last of the left side, it is the Slash. */
    val nCatTemp = cat.charAt(index) match {
      case '/' => ComplexCategory(0, leftSide, rightSide, Slash.Right)
      case _ => ComplexCategory(0, leftSide, rightSide, Slash.Left)
    }
    checkAndGetComplexCategory(nCatTemp)
  }
    
    
  /**
   * Decomposes asymmetrical complex categories. This means that the left side is a complex,
   * and the right side is an atomic category.
   *
   * ((S[mod=nm,form=stem]\NP[case=ga,mod=nm]))\NP[case=ni,mod=nm]
   * ((((S[mod=nm,form=stem]\NP[case=ga,mod=nm]))\NP[case=o,mod=nm]))\NP[case=ni,mod=nm]
   * ((((S[mod=X1,form=X2]/S[mod=X1,form=X2]))/((S[mod=X1,form=X2]/S[mod=X1,form=X2]))))\NP[case=nc,mod=nm]
   */
  private def decomposeComplexAsymmetrical(cat: String): ComplexCategory = {
    val PAT = "^((\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]((\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+)+)+((\\/|\\\\)((\\()+\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]((\\/|\\\\)\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\](\\))+)+)+)?".r.unanchored 
    val leftSide = PAT.findFirstIn(cat) match{
      case None => AtomicCategory(-1, "", JPCategoryFeature.createFromValues(Seq("")))    // NEVER REACHED
      case Some(x) => {
        val subCategory = x.replaceFirst("\\(", "").reverse.replaceFirst("\\)", "").reverse
        decomposeCategory(subCategory)
      }
    }
    val PAT2 = "\\w+\\[(\\w+=(\\w+|X1|X2)(,)?)+\\]$".r.unanchored
    val rightSide = PAT2.findFirstIn(cat) match{
      case None => AtomicCategory(-1, "", JPCategoryFeature.createFromValues(Seq("")))    // NEVER REACHED
      case Some(x) => getAtomicCategory(x)
    }
    /* Checks direction of Slash. */
    val nCatTemp = {
      cat.reverse.charAt(cat.reverse.indexOf(")") - 1) match {
        case '/' => ComplexCategory(0, leftSide, rightSide, Slash.Right)
        case _ => ComplexCategory(0, leftSide, rightSide, Slash.Left)
      }
    }
    checkAndGetComplexCategory(nCatTemp)
  }
    
    
  /**
   * Decomposes a complex category and determines what kind it is
   * (symmetrical, asymmetrical, normal).
   */
  private def decomposeComplex(cat: String): ComplexCategory = {
    /* Checks if it's an asymmetrcial complex category: (X/X)/X */
    if(cat.startsWith("(") && !cat.reverse.startsWith(")")){
      decomposeComplexAsymmetrical(cat)
    }     
    /* Checks if it's a symmetrical category: (X/X)/(X/X) */
    else if(cat.startsWith("(") && cat.reverse.startsWith(")")){
      decomposeComplexSymmetrical(cat)
    }
    /* Applies to normal complex categories: X/X */
    else{
      getComplexCategory(cat)
    }
  }
    
    
  /**
   * If category starts with "(" it is complex -> invoke @decomposeComplex.
   * Else, it is atomic -> invoke @getAtomicCategory.
   */
  def decomposeCategory(el: String): Category = {    
    if(el.startsWith("(")){
      val category = el.replaceFirst("\\(", "").reverse.replaceFirst("\\)", "").reverse
      decomposeComplex(category)
    }   
    else{ getAtomicCategory(el) }
  }
}