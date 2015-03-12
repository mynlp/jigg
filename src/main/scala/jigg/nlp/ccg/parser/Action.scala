package jigg.nlp.ccg.parser

/*
 Copyright 2013-2015 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

import jigg.nlp.ccg.lexicon.{Category, Dictionary}
import jigg.nlp.ccg.lexicon.Direction.Direction

/**
 * action and corresponding label; for speed reason, label should not have the actual object such as category, so we convert Action object into corresponding Label object when filling feature templates
 */
sealed trait Action { def toLabel:ActionLabel }
sealed trait ActionLabel {
  def mkString(dict:Dictionary):String
}

// shift the category with categoryId of the head of buffer
case class Shift(category:Category) extends Action { override def toLabel = ShiftLabel(category.id) }

@SerialVersionUID(-6619103978469031483L)
case class ShiftLabel(id:Int) extends ActionLabel {
  override def mkString(dict:Dictionary) = "SHIFT(" + dict.getCategory(id) + ")"
}

// combine two top nodes on the stack into a node which has categoryId
case class Combine(category:Category, headDir:Direction, ruleType:String) extends Action { override def toLabel = CombineLabel(category.id) }

@SerialVersionUID(-1350486416817206332L)
case class CombineLabel(id:Int) extends ActionLabel {
  override def mkString(dict:Dictionary) = "COMBINE(" + dict.getCategory(id) + ")"
}

// unary change to a node with categoryId
case class Unary(category:Category, ruleType:String) extends Action { override def toLabel = UnaryLabel(category.id) }

@SerialVersionUID(-3492899016953622825L)
case class UnaryLabel(id:Int) extends ActionLabel {
  def mkString(dict:Dictionary) = "UNARY(" + dict.getCategory(id) + ")"
}

case class Finish() extends Action { override def toLabel = FinishLabel() }

@SerialVersionUID(-6536578690403443069L)
case class FinishLabel() extends ActionLabel {
  def mkString(dict:Dictionary) = "FINISH"
}
