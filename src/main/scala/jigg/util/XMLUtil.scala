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

object XMLUtil { self =>

  implicit class RichNode(node: Node) {
    def addChild(newChild: NodeSeq): Node = node match {
      case e: Elem => e.copy(child = e.child ++ newChild)
      case _ => sys.error("Can only add children to elements!")
    }

    /** If other nodes with the same XML tag as `newChild` exist in `node`, it
      * overrides the existing elements. If `attr` is set, the element only with the
      * same value of attribute is recognized as "the same XML tag".
      *
      * For example, when `node` constains `<dependencies type="basic">...</dependencies>`,
      * `<dependencies type="collapsed">...</dependencies>` in `newChild` will be added
      * if `attr=Some("type")`.
      */
    def addOrOverwriteChild(newChild: NodeSeq, attr: Option[String] = None): Node =
      node match {
        case e: Elem =>
          // remove duplicate
          def addOrOverwrite(origChild: NodeSeq): NodeSeq = {
            // val addedLabels = newChild.map(_.label)
            val notInNew: Node => Boolean = attr match {
              case None =>
                n => !newChild.exists(_.label == n.label)
              case Some(a) =>
                n => {
                  val targetValue = n \@ a
                  !newChild.exists { child =>
                    child.label == n.label && child \@ a == targetValue
                  }
                }
            }
            val uniqueInOrig = origChild.filter(notInNew)
            uniqueInOrig ++ newChild
          }
          e.copy(child = addOrOverwrite(e.child))
        case _ => sys.error("Can only add children to elements!")
      }

    def addAttribute(k: String, v: String): Node = node match {
      case e: Elem => e % Attribute(None, k, Text(v), Null)
      case _ => node
    }

    def addAttributes(kvs: Map[String, String]): Node =
      kvs.foldLeft(node) { case (n, (k, v)) => n addAttribute(k, v) }

    def replaceChild(newChild: NodeSeq): Node = node match {
      case e: Elem => e.copy(child=newChild)
      case _ => node
    }

    def addAnnotatorName(annotator: String): Node = {
      val newAnnotators = (node \@ "annotators") match {
        case "" => annotator
        case sofar =>
          if (sofar.split(' ').contains(annotator)) sofar // already annotated
          else sofar + " " + annotator
      }
      node addAttribute ("annotators", newAnnotators)
    }

    /** The idiom below is borrowed from:
      * http://stackoverflow.com/questions/21391942/eliminate-duplicates-change-label-with-scala-xml-transform-ruletransformer
      * The previous version used `RuleTransformer` and `RewriteRule` but I found it has some problem.
      */
    def replaceAll(label: String)(body: Elem=>Node): Node = {
      def recurse(n: Node): Seq[Node] = n match {
        case e: Elem if (e.label == label) =>
          body(e)
        case e: Elem => e.copy(child = e.nonEmptyChildren.map(recurse(_).headOption).flatten)
        case _ => n
      }
      recurse(node).head
    }

    /** Return concatenation of all Atom[_] elements in the child.
      * (is there any possibilities that a node has more than one such element in child?)
      */
    def textElem(): String = node.child.collect {
      case t: Atom[_] => t.data
    }.mkString.trim

    def removeText(): Node = node match {
      case node: Elem => node.copy(child = (node.child map {
        // Atom includes Text or other string objects.
        case t: Atom[_] => Text("")
        case other => other
      }))
      case _ => sys.error("removeText is only allowed for Elem.")
    }

    def hasChild(): Boolean = node.nonAtomChild.size > 0
    def nonAtomChild(): NodeSeq = node.map(_.child filterNot (_.isAtom)).flatten
    def attrs(): Seq[(String, String)] =
      node.attributes.seq.map { e => (e.key, e.value.toString) }.toSeq

    /** If a string formatted by PrettyPrinter is inputted, the XML.Node.child method
      * which is used in a subsequent step, e.g. SentenceAnnotator, returns an array
      * containing empty elements.
      * To avoid this issue, we create a new XML object using by the XMLUtil.getChildNode method.
      */
    def toUnformatted() = {
      def recurse(nodes: NodeSeq): NodeSeq = nodes map { n =>
        // To maintain the original text, we define an new node.
        val tmpNode = n.textElem match {
          case "" => <xml/>
          case t => <xml>{t}</xml>
        }
        val newNode = tmpNode.copy(label = n.label, attributes = n.attributes)
        if (n.hasChild) {
          newNode addChild (recurse(n.nonAtomChild))
        } else {
          newNode
        }
      }
      val childNode = node.nonAtomChild
      node replaceChild recurse(childNode)
    }

    // These are for Java users but can implicit class be used from Java?
    def find(that: String) = self.find(node, that)
    def findAll(that: String) = self.findAll(node, that)
    def findSub(that: String) = self.findSub(node, that)
  }

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def addChild(n: Node, newChild: NodeSeq): Node = n addChild newChild

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def addOrOverwriteChild(n: Node, newChild: NodeSeq, attr: Option[String] = None): Node
  = n addOrOverwriteChild (newChild, attr)

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def addAttribute(n: Node, k: String, v: String): Node = n addAttribute (k, v)

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def addAttributes(n: Node, kvs: Map[String, String]): Node = n addAttributes kvs

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def replaceChild(n: Node, newChild: NodeSeq): Node = n replaceChild newChild

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def addAnnotatorName(n: Node, annotator: String): Node = n addAnnotatorName annotator

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def replaceAll(root: Node, label: String)(body: Elem => Node) =
    root.replaceAll(label)(body)

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def text(node: Node): String = node.textElem

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def removeText(node: Node) = node.removeText()

  def find(node: Node, that: String): Node = (node \ that)(0)
  def findAll(node: Node, that: String): java.util.List[Node] = node \ that
  def findSub(node: Node, that: String): Node = (node \\ that)(0)
  def findAllSub(node: Node, that: String): java.util.List[Node] = node \\ that

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def hasChild(node: Node): Boolean = node.hasChild

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def getNonEmptyChild(node: Node): NodeSeq = node.nonAtomChild

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def getAttributionList(node: Node): Seq[(String, String)] = node.attrs

  @deprecated(message="Use implicit method in RichNode instead.", "3.6.2")
  def unFormattedXML(node: Node): Node = node.toUnformatted()
}
