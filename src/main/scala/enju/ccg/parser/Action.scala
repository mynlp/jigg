package enju.ccg.parser

import enju.ccg.lexicon.{Category, Dictionary}
import enju.ccg.lexicon.Direction.Direction

/**
 * action and corresponding label; for speed reason, label should not have the actual object such as category, so we convert Action object into corresponding Label object when filling feature templates
 */
sealed trait Action { def toLabel:ActionLabel }
sealed trait ActionLabel {
  def mkString(dict:Dictionary):String
}

// shift the category with categoryId of the head of buffer
case class Shift(category:Category) extends Action { override def toLabel = ShiftLabel(category.id) }
case class ShiftLabel(id:Int) extends ActionLabel {
  override def mkString(dict:Dictionary) = "SHIFT(" + dict.getCategory(id) + ")"
}

// combine two top nodes on the stack into a node which has categoryId
case class Combine(category:Category, headDir:Direction, ruleType:String) extends Action { override def toLabel = CombineLabel(category.id) }
case class CombineLabel(id:Int) extends ActionLabel {
  override def mkString(dict:Dictionary) = "COMBINE(" + dict.getCategory(id) + ")"
}

// unary change to a node with categoryId
case class Unary(category:Category, ruleType:String) extends Action { override def toLabel = UnaryLabel(category.id) }
case class UnaryLabel(id:Int) extends ActionLabel {
  def mkString(dict:Dictionary) = "UNARY(" + dict.getCategory(id) + ")"
}

case class Finish() extends Action { override def toLabel = FinishLabel() }
case class FinishLabel() extends ActionLabel {
  def mkString(dict:Dictionary) = "FINISH"
}
