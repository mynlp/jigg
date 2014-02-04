package enju.ccg.lexicon
import Slash._

sealed trait Category extends Numbered[Unit] { override def v:Unit = {} }

case class AtomicCategory(override val id:Int, base:String, avm:AVM = AVM.empty) extends Category {
  override def toString = avm.toString match {
    case "" => base
    case s => base + "[" + s + "]"
  }
}

// This is used when candidate shift category is empty
// It sometimes happen if for example, PoS not registered in the dictionary is detected.
object UnkCategory extends Category {
  override val id = -1
  override def toString = "UNK"
}

case class ComplexCategory(override val id:Int,
                           left:Category, right:Category,
                           slash:Slash) extends Category {
  def toStringChild(child:Category) = child match {
    case AtomicCategory(_,_,_) => child.toString
    case ComplexCategory(_,_,_,_) => "(" + child.toString + ")"
    case _ => child.toString // should never happen!
  }
  override def toString = toStringChild(left) + slash + toStringChild(right)
}
