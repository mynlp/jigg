package enju.util

import scala.xml._

object XMLUtil {
  def addChild(n: Node, newChild: Node): Node = n match {
    case e: Elem =>
      e.copy(child = e.child ++ newChild)
    case _ => sys.error("Can only add children to elements!")
  }
}
