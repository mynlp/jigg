package jigg.util

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

import scala.xml._
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.collection.JavaConversions._

object XMLUtil {
  def addChild(n: Node, newChild: NodeSeq): Node = n match {
    case e: Elem => e.copy(child = e.child ++ newChild)
    case _ => sys.error("Can only add children to elements!")
  }

  /** If other nodes with the same XML tag as `newChild` exist in `n`, it
    * overrides the existing elements. If `attr` is set, the element only with the
    * same value of attribute is recognized as "the same XML tag".
    *
    * For example, when `n` constains `<dependencies type="basic">...</dependencies>`,
    * `<dependencies type="collapsed">...</dependencies>` in `newChild` will be added
    * if `attr=Some("type")`.
    */
  def addOrOverwriteChild(n: Node, newChild: NodeSeq, attr: Option[String] = None): Node
  = n match {
    case e: Elem =>
      // remove duplicate
      def addOrOverwrite(origChild: NodeSeq): NodeSeq = {
        // val addedLabels = newChild.map(_.label)
        val notInNew: Node => Boolean = attr match {
          case None =>
            node => !newChild.exists(_.label == node.label)
          case Some(a) =>
            node => {
              val targetValue = node \@ a
              !newChild.exists { child =>
                child.label == node.label && child \@ a == targetValue
              }
            }
        }
        val uniqueInOrig = origChild.filter(notInNew)
        uniqueInOrig ++ newChild
      }
      e.copy(child = addOrOverwrite(e.child))
    case _ => sys.error("Can only add children to elements!")
  }

  @deprecated(message="Use addOrOverwriteChild instead.", "3.6.1")
  def addOrOverrideChild(n: Node, newChild: NodeSeq, attr: Option[String] = None): Node =
    addOrOverwriteChild(n, newChild, attr)

  def addAttribute(n: Node, k: String, v: String): Node = n match {
    case e: Elem => e % Attribute(None, k, Text(v), Null)
    case _ => n
  }

  def addAttributes(n: Node, kvs: Map[String, String]): Node = {
    kvs.foldLeft(n) { case (node, (k, v)) => addAttribute(node, k, v) }
  }

  def replaceChild(n: Node, newChild: NodeSeq): Node = n match {
    case e: Elem => e.copy(child=newChild)
    case _ => n
  }

  def addAnnotatorName(n: Node, annotator: String): Node = {
    val newAnnotators = (n \@ "annotators") match {
      case "" => annotator
      case sofar =>
        if (sofar.split(' ').contains(annotator)) sofar // already annotated
        else sofar + " " + annotator
    }
    addAttribute(n, "annotators", newAnnotators)
  }

  /** The idiom below is borrowed from:
    * http://stackoverflow.com/questions/21391942/eliminate-duplicates-change-label-with-scala-xml-transform-ruletransformer
    * The previous version used `RuleTransformer` and `RewriteRule` but I found it has some problem.
    */
  def replaceAll(root: Node, label: String)(body: Elem => Node) = {
    def recurse(node: Node): Seq[Node] = node match {
      case e: Elem if (e.label == label) =>
        body(e)
      case e: Elem => e.copy(child = e.nonEmptyChildren.map(recurse(_).headOption).flatten)
      case _ => node
    }
    recurse(root).head
  }

  /** Return concatenation of all Atom[_] elements in the child.
    * (is there any possibilities that a node has more than one such element in child?)
    */
  def text(node: Node): String = node.child.collect {
    case t: Atom[_] => t.data
  }.mkString.trim

  def removeText(node: Elem) = node.copy(child = (node.child map {
    // Atom includes Text or other string objects.
    case t: Atom[_] => Text("")
    case other => other
  }))

  def find(node: Node, that: String): Node = (node \ that)(0)
  def findAll(node: Node, that: String): java.util.List[Node] = node \ that
  def findSub(node: Node, that: String): Node = (node \\ that)(0)
  def findAllSub(node: Node, that: String): java.util.List[Node] = node \\ that

  def hasChild(nodes: NodeSeq): Boolean = nodes match {
    case x: Elem => getNonEmptyChild(x).length > 0
    case _ => nodes forall hasChild
  }

  def getNonEmptyChild(nodes: NodeSeq): NodeSeq = nodes match {
    case x: Elem => x.child filterNot (_.isAtom)
    case x => (x.map(getNonEmptyChild(_))).flatten
  }

  def getAttributionList(node: Node): Seq[(String, String)] = (
    for{
      elem <- node.attributes.seq
      n = elem.key -> elem.value.toString
    } yield n
    ).toSeq

  /** If a string formatted by PrettyPrinter is inputted, the XML.Node.child method
    * which is used in a subsequent step, e.g. SentenceAnnotator, returns an array
    * containing empty elements.
    * To avoid this issue, we create a new XML object using by the XMLUtil.getChildNode method.
    */
  def unFormattedXML(node: Node): Node = {
    val childNode = getNonEmptyChild(node)
    def recurse(nodes: NodeSeq): NodeSeq = nodes map { n =>
      val text = XMLUtil.text(n)
      // To maintain the original text, we define an new node.
      val newNode = text match{
        case "" => <xml></xml>
        case t => <xml>{t}</xml>
      }
      val children = n match {
        case n if getNonEmptyChild(n).filterNot(x => hasChild(x)).length == 0 =>
          recurse(getNonEmptyChild(n))
        case n =>
          getNonEmptyChild(n).filterNot(x => hasChild(x))
      }
      addChild(newNode.copy(label = n.label, attributes = n.attributes), children)
    }
    replaceChild(node, recurse(childNode))
  }
}
