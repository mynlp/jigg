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

import scala.collection.mutable.ArrayBuffer
import scala.xml._

import jigg.pipeline.Annotation

object TreesUtil {

  def streeToNode(tree: String, sentence: Node, annotator: String) = {
    val tokens = tree.replaceAllLiterally("(", " ( ").replaceAllLiterally(")", " ) ").trim.split("\\s+")

    val tokenSeq = (sentence \ "tokens").head \ "token"
    var tokIdx = -1
    def nextTokId = { tokIdx += 1; tokenSeq(tokIdx) \@ "id" }

    val spans = new ArrayBuffer[Node]

    // Fill in spans; return the id of constructed subtree, and the arrived index.
    def readTopdown(idx: Int): (String, Int) = {

      def collectChildren(curChildren: List[String], cur: Int): (Seq[String], Int) =
        tokens(cur) match {
          case ")" =>
            (curChildren.reverse, cur)
          case "(" =>
            val (nextChildId, nextIdx) = readTopdown(cur)
            collectChildren(nextChildId :: curChildren, nextIdx)
        }

      tokens(idx) match {
        case "(" =>
          def skipParen(i: Int = 0): Int = {
            if (tokens(idx + i) == "(") skipParen(i + 1)
            else i
          }
          val parenCount = skipParen()

          val labelIdx = idx + parenCount
          val label = tokens(labelIdx)

          val (children, closeIdx) = tokens(labelIdx + 1) match {
            case "(" => collectChildren(Nil, labelIdx + 1)
            case word => (Nil, labelIdx + 1 + 1)
          }
          val thisId = children match {
            case Nil => nextTokId
            case children => Annotation.ParseSpan.nextId
          }
          if (!children.isEmpty) {
            val childStr = children mkString " "
            spans += <span id={ thisId } symbol={ label } children={ childStr }/>
          }
          for (i <- 0 until parenCount) { assert(tokens(closeIdx + i) == ")") }
          (thisId, closeIdx + parenCount)
      }
    }

    val (rootId, _) =  readTopdown(0)
    <parse root={ rootId } annotators={ annotator }>{ spans }</parse>
  }
}
