package jigg.nlp.ccg

import jigg.nlp.ccg.lexicon._
import jigg.nlp.ccg.lexicon.Slash._
import scala.collection.mutable.HashMap

/**
 * Implements CCG Rules (hard-coded).
 */
trait CCGRules

/**
 * Implements unary rules, i.e. type raising rules.
 * Uses the set of atomic categories of the grammar in order to determine possible features for
 * a category.
 */
trait UnaryRule extends CCGRules{
  def isApplicable(input: Category): Boolean
  def apply(input: Category, setAC:Set[AtomicCategory]): Set[ComplexCategory]
}

/**
 * Handles: S -> NP/NP      (RelExt)
 * S[mod=adn]
 */
object TypeChangingRule1 extends UnaryRule{ 
  override def isApplicable(current: Category) = current match {
    case ac: AtomicCategory => if(ac.toString.matches("^S\\[.*(mod=adn).*\\]")) true else false
    case cc: ComplexCategory => false
  }
  
  override def apply(current: Category, setAC:Set[AtomicCategory]): Set[ComplexCategory] = {
    for(ac <- setAC if ac.base == "NP") yield ComplexCategory(ac.id*42, ac, ac, Slash.Right)
  }
}


/**
 * Handles: S\NP[1] -> NP[1]/NP[1]    (RelIn)
 */
object TypeChangingRule2 extends UnaryRule{
  override def isApplicable(current: Category) = current match {
    case ac:AtomicCategory => false
    case cc:ComplexCategory => {
      if(cc.slash.equals(Slash.Left) && cc.left.isInstanceOf[AtomicCategory] && cc.left.toString.startsWith("S") &&
          cc.right.isInstanceOf[AtomicCategory] && cc.right.toString.startsWith("NP")){
        true
      }
      else false
    }
  }
  
  override def apply(current: Category, setAC:Set[AtomicCategory]): Set[ComplexCategory] = {
    Set(ComplexCategory(0, current.asInstanceOf[ComplexCategory].right, current.asInstanceOf[ComplexCategory].right, Slash.Right))
  }
}

/**
 * Handles: S -> S/S    (Con)
 * S[mod=adv]
 */
object TypeChangingRule3 extends UnaryRule{
  override def isApplicable(current: Category) = current match {
    case ac: AtomicCategory => if(ac.toString.matches("^S\\[.*(mod=adv).*\\]")) true else false
    case cc: ComplexCategory => false
  }
  
  override def apply(current: Category, setAC:Set[AtomicCategory]): Set[ComplexCategory] = {
    for(ac <- setAC if ac.base == "S") yield ComplexCategory(ac.id*42, ac, ac, Slash.Right)
  }
}




/**
 * Implements binary combinatory rules.
 * At the moment only functional application and composition.
 */
trait BinaryRule extends CCGRules{
  def isApplicable(candidate: Category, current: Category): Boolean
  def apply(candidate: Category, current: Category): Category
}

/**
 * Implements normal forward rule (>).
 * 
 * Candidate: S/NP, Current: NP
 * --> S
 */
object ForwardApplication extends BinaryRule{  
  override def isApplicable(candidate: Category, current: Category) = current match {
    case cc:ComplexCategory => false
    case ac:AtomicCategory => {
      candidate match{
        case c:AtomicCategory => false
        case cand:ComplexCategory => {
          if(cand.slash.equals(Slash.Right) && cand.right.isInstanceOf[AtomicCategory]){
            if(cand.right.equals(current)) { true }
           /*
            * Else if candidate is underspecified on BOTH SIDES.
            * cand: NP/NP and curr: NP[ac, nm]
            */
            else if(cand.right.toStringNoFeature == cand.left.toStringNoFeature && cand.left.isInstanceOf[AtomicCategory] &&
                !cand.left.asInstanceOf[AtomicCategory].hasFeatures && !cand.right.asInstanceOf[AtomicCategory].hasFeatures &&
                cand.right.toStringNoFeature == current.toStringNoFeature){ true }
            else false
          }
          else false
        }
      }
    }
  }
  
  override def apply(candidate: Category, current: Category): Category = {
    // NP/NP + NP[ac, nm] -> NP[ac, nm]
    if(candidate.asInstanceOf[ComplexCategory].left.isInstanceOf[AtomicCategory] &&
        !candidate.asInstanceOf[ComplexCategory].left.asInstanceOf[AtomicCategory].hasFeatures &&
        !candidate.asInstanceOf[ComplexCategory].right.asInstanceOf[AtomicCategory].hasFeatures){
      current
    }
    // S[base]/NP[ac,nm] + NP[ac,nm] -> S[base]
    // (NP/NP)/NP + NP -> NP/NP
    else{
      candidate.asInstanceOf[ComplexCategory].left
    }
  }
  
  override def toString() = "Forward Application"
}

/**
 * Implements forward composition rule (>B).
 * 
 * Candidate: S/NP, current: NP/PP
 * --> S/PP
 */
object ForwardComposition extends BinaryRule{
  override def isApplicable(candidate: Category, current: Category): Boolean = {
    var result: Boolean = false
    if(candidate.isInstanceOf[ComplexCategory] && current.isInstanceOf[ComplexCategory]){
      if(candidate.asInstanceOf[ComplexCategory].slash.equals(Slash.Right) && current.asInstanceOf[ComplexCategory].slash.equals(Slash.Right)){
        if(candidate.asInstanceOf[ComplexCategory].right.isInstanceOf[AtomicCategory] && current.asInstanceOf[ComplexCategory].left.isInstanceOf[AtomicCategory]){
          if(candidate.asInstanceOf[ComplexCategory].right.equals(current.asInstanceOf[ComplexCategory].left)){
            result = true
          }
        }
      }
    }
    result
  }
  
  override def apply(candidate: Category, current: Category): Category = {
    val newId:Int = candidate.id + current.id
    val newLeft:Category = candidate.asInstanceOf[ComplexCategory].left
    val newRight:Category = current.asInstanceOf[ComplexCategory].right
    val newSlash:Slash = candidate.asInstanceOf[ComplexCategory].slash
    ComplexCategory(newId, newLeft, newRight, newSlash)
  }
  
  override def toString() = "Forward Composition"
}

/**
 * Implements simple forward crossed composition (>Bx).
 * 
 * Candidate: X/Y  Current: Y\Z
 * --> X\Z
 */
object ForwardCrossedComposition extends BinaryRule{
  override def isApplicable(candidate: Category, current: Category): Boolean = {
    var result: Boolean = false
     if(candidate.isInstanceOf[ComplexCategory] && current.isInstanceOf[ComplexCategory]){
       if(candidate.asInstanceOf[ComplexCategory].slash.equals(Slash.Right) && current.asInstanceOf[ComplexCategory].slash.equals(Slash.Left)){
         if(candidate.asInstanceOf[ComplexCategory].right.isInstanceOf[AtomicCategory] && current.asInstanceOf[ComplexCategory].left.isInstanceOf[AtomicCategory]){
           if(candidate.asInstanceOf[ComplexCategory].right.equals(current.asInstanceOf[ComplexCategory].left)){
            result = true
           }
         }
       }
     }
    result
  }
  
  override def apply(candidate: Category, current: Category): Category = {
    val newId:Int = candidate.id + current.id
    val newLeft:Category = candidate.asInstanceOf[ComplexCategory].left
    val newRight:Category = current.asInstanceOf[ComplexCategory].right
    val newSlash:Slash = current.asInstanceOf[ComplexCategory].slash
    ComplexCategory(newId, newLeft, newRight, newSlash)
  }
  
  override def toString() = "Forward Crossed Composition"
}

/**
 * Implements simple backward rule (<). Candidate must be atomic.
 * 
 * Candidate: NP, Current: S\NP
 * --> S
 */
object BackwardApplication extends BinaryRule{
  override def isApplicable(candidate: Category, current: Category) = current match {
    case ac:AtomicCategory => false
    case curr:ComplexCategory => {
      candidate match{
        case c:ComplexCategory => false
        case cand:AtomicCategory => {
          if(curr.slash.equals(Slash.Left) && curr.right.isInstanceOf[AtomicCategory]){
            if(cand.equals(curr.right)) true
           /*
            * Else if current category is underspecified on BOTH SIDES.
            * cand: NP[ac,nm] and curr: NP\NP
            */
            else if(curr.right.toStringNoFeature == curr.left.toStringNoFeature && curr.left.isInstanceOf[AtomicCategory] &&
                !curr.left.asInstanceOf[AtomicCategory].hasFeatures && !curr.right.asInstanceOf[AtomicCategory].hasFeatures &&
                curr.right.toStringNoFeature == candidate.toStringNoFeature){ true }
            else false
          }
          else false
        }
      }
    }
  }

  override def apply(candidate: Category, current: Category): Category = {
    // NP[ac, nm] + NP\NP -> NP[ac, nm]
    if(current.asInstanceOf[ComplexCategory].left.isInstanceOf[AtomicCategory] &&
        !current.asInstanceOf[ComplexCategory].left.asInstanceOf[AtomicCategory].hasFeatures &&
        !current.asInstanceOf[ComplexCategory].right.asInstanceOf[AtomicCategory].hasFeatures){
      candidate
    }
    // NP[ac, nm] + S\NP[ac,nm] -> S
    // NP[ac, nm] + (S\NP)\NP -> S\NP
    else{
      current.asInstanceOf[ComplexCategory].left
    }
  }
  
  override def toString() = "Backward Application"
}

/**
 * Implements backward composition rule (<B).
 * Note: Since this is only a special case of @BackwardRuleCompositionNested, this is 
 * a private object that is only called from said Rule.
 * 
 * Candidate: S[stem]\NP[ga], current: S[cont]\S[stem]
 * --> S[cont]\NP[ga]
 */
private object BackwardCompositionSimple extends BinaryRule{
  override def isApplicable(candidate: Category, current: Category): Boolean = {
    var result: Boolean = false
    if(candidate.isInstanceOf[ComplexCategory] && current.isInstanceOf[ComplexCategory]){
      if(candidate.asInstanceOf[ComplexCategory].slash.equals(Slash.Left) && current.asInstanceOf[ComplexCategory].slash.equals(Slash.Left)){
        if(candidate.asInstanceOf[ComplexCategory].left.isInstanceOf[AtomicCategory] && current.asInstanceOf[ComplexCategory].right.isInstanceOf[AtomicCategory]){
          if(candidate.asInstanceOf[ComplexCategory].left.equals(current.asInstanceOf[ComplexCategory].right)){
            result = true
          }
        }
      }
    }
    result
  }
  
  override def apply(candidate: Category, current: Category): Category = {
    val newId:Int = candidate.id + current.id
    val newLeft: Category = current.asInstanceOf[ComplexCategory].left
    val newRight: Category = candidate.asInstanceOf[ComplexCategory].right
    val newSlash: Slash = candidate.asInstanceOf[ComplexCategory].slash
    ComplexCategory(newId, newLeft, newRight, newSlash)
  }
}

/**
 * Implements nested backward composition rule (<B).
 * 
 * Candidate: (S[stem]\NP[ga])\NP[ni], current: S[cont]\S[stem]
 * --> (S[cont]\NP[ga])\NP[ni]
 */
object BackwardCompositionNested extends BinaryRule{
  override def isApplicable(candidate: Category, current: Category): Boolean = {
    var result: Boolean = false
    if(candidate.isInstanceOf[ComplexCategory] && current.isInstanceOf[ComplexCategory]){
      if(candidate.asInstanceOf[ComplexCategory].slash.equals(Slash.Left) && current.asInstanceOf[ComplexCategory].slash.equals(Slash.Left)){
        candidate.asInstanceOf[ComplexCategory].left match{
          case ac: AtomicCategory => result = BackwardCompositionSimple.isApplicable(candidate, current)
          case cc: ComplexCategory => {
            if(cc.slash.equals(Slash.Left)){
              result = BackwardCompositionNested.isApplicable(cc, current)
            }
          }
        }
      }
    }
    result
  }
  
  override def apply(candidate: Category, current: Category): Category = {
    candidate.asInstanceOf[ComplexCategory].left match{
      case ac: AtomicCategory => BackwardCompositionSimple.apply(candidate, current)
      case cc: ComplexCategory => {
        val newId:Int = candidate.id + current.id
        val newLeft:Category = BackwardCompositionNested.apply(cc, current)
        val newRight:Category = candidate.asInstanceOf[ComplexCategory].right
        val newSlash:Slash = candidate.asInstanceOf[ComplexCategory].slash
        ComplexCategory(newId, newLeft, newRight, newSlash)
      }
    }
  }
  
  override def toString() = "Backward Composition"
}


/**
 * Implements simple backward crossed composition (<Bx).
 * 
 * Candidate: X\Y  Current: Y/Z
 * --> X/Z
 */
object BackwardCrossedComposition extends BinaryRule{
  override def isApplicable(candidate: Category, current: Category): Boolean = {
    var result: Boolean = false
     if(candidate.isInstanceOf[ComplexCategory] && current.isInstanceOf[ComplexCategory]){
       if(candidate.asInstanceOf[ComplexCategory].slash.equals(Slash.Left) && current.asInstanceOf[ComplexCategory].slash.equals(Slash.Right)){
         if(candidate.asInstanceOf[ComplexCategory].right.isInstanceOf[AtomicCategory] && current.asInstanceOf[ComplexCategory].left.isInstanceOf[AtomicCategory]){  // Maybe irrelevant?
           if(candidate.asInstanceOf[ComplexCategory].right.equals(current.asInstanceOf[ComplexCategory].left)){
            result = true
          }
         }
       }
     }
    result
  }
  
  override def apply(candidate: Category, current: Category): Category = {
    val newId:Int = candidate.id + current.id
    val newLeft:Category = candidate.asInstanceOf[ComplexCategory].left
    val newRight:Category = current.asInstanceOf[ComplexCategory].right
    val newSlash:Slash = current.asInstanceOf[ComplexCategory].slash
    ComplexCategory(newId, newLeft, newRight, newSlash)
  }
  
  override def toString() = "Backward Crossed Composition"
}