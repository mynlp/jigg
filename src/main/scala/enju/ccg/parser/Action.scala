package enju.ccg.parser
import enju.ccg.lexicon.Category
import Direction._

/**
 * action and corresponding label; for speed reason, label should not have the actual object such as category, so we convert Action object into corresponding Label object when filling feature templates
 */
sealed trait Action { def toLabel:ActionLabel }
sealed trait ActionLabel

// shift the category with categoryId of the head of buffer
case class Shift(category:Category) extends Action { override def toLabel = ShiftLabel(category.id) }
case class ShiftLabel(id:Int) extends ActionLabel

// combine two top nodes on the stack into a node which has categoryId
case class Combine(category:Category, headDir:Direction) extends Action { override def toLabel = CombineLabel(category.id) }
case class CombineLabel(id:Int) extends ActionLabel

// unary change to a node with categoryId
case class Unary(category:Category) extends Action { override def toLabel = UnaryLabel(category.id) }
case class UnaryLabel(id:Int) extends ActionLabel

case class Finish() extends Action { override def toLabel = FinishLabel() }
case class FinishLabel() extends ActionLabel
