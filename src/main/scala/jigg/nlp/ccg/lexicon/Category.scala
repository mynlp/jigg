package jigg.nlp.ccg.lexicon
import Slash._

sealed trait Category extends Numbered[Unit] {
  override def v:Unit = {}
  def toStringNoFeature: String
}

@SerialVersionUID(6748884927580538343L)
case class AtomicCategory(override val id:Int, base:String, feature:CategoryFeature) extends Category {
  override def toString = feature.toString match {
    case "" => base
    case s => base + "[" + s + "]"
  }

  override def toStringNoFeature = base
  
  def hasFeatures = feature.toString match {
    case "" => false
    case _ => true
  }
}
@SerialVersionUID(3754315949719248198L)
case class ComplexCategory(override val id:Int,
                           left:Category, right:Category,
                           slash:Slash) extends Category {
  def toStringChild(child:Category) = child match {
    case AtomicCategory(_,_,_) => child.toString
    case ComplexCategory(_,_,_,_) => "(" + child.toString + ")"
  }
  override def toString = toStringChild(left) + slash + toStringChild(right)

  def toStringChildNoFeature(child:Category) = child match {
    case AtomicCategory(_,_,_) => child.toStringNoFeature
    case ComplexCategory(_,_,_,_) => "(" + child.toStringNoFeature + ")"
  }
  override def toStringNoFeature = toStringChildNoFeature(left) + slash + toStringChildNoFeature(right)
}
