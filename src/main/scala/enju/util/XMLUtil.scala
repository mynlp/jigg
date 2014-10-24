package enju.util

import scala.xml._
import scala.xml.transform.{RewriteRule, RuleTransformer}

object XMLUtil {
  def addChild(n: Node, newChild: NodeSeq): Node = n match {
    case e: Elem =>
      e.copy(child = e.child ++ newChild)
    case _ => sys.error("Can only add children to elements!")
  }

  /** The idiom below is borrowed from:
      * https://gist.github.com/zentrope/728034
      * http://stackoverflow.com/questions/2199040/scala-xml-building-adding-children-to-existing-nodes
    */
  def replaceAll(node: Node, label: String)(body: Elem => Node) = {
    object replaceRule extends RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case e: Elem if e.label == label =>
          body(e)
        case other =>
          other
      }
    }
    new RuleTransformer(replaceRule).transform(node).head
  }

  def removeText(node: Elem) = node.copy(child = (node.child map {
    // Atom includes Text or other string objects.
    case t: Atom[_] => Text("")
    case other => other
  }))
}
