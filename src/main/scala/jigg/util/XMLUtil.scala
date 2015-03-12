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
    case e: Elem =>
      e.copy(child = e.child ++ newChild)
    case _ => sys.error("Can only add children to elements!")
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

  def removeText(node: Elem) = node.copy(child = (node.child map {
    // Atom includes Text or other string objects.
    case t: Atom[_] => Text("")
    case other => other
  }))

  def find(node: Node, that: String): Node = (node \ that)(0)
  def findAll(node: Node, that: String): java.util.List[Node] = node \ that
  def findSub(node: Node, that: String): Node = (node \\ that)(0)
  def findAllSub(node: Node, that: String): java.util.List[Node] = node \\ that
}
