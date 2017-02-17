package jigg.pipeline

/*
 Copyright 2013-2016 Hiroshi Noji

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

import java.util.Properties

import scala.xml.{Node, Elem, Text, Atom}
import jigg.util.XMLUtil.RichNode

/** This simple annotator just segments a sentence by spaces, i.e.,
  * assuming the input sentence is already correctly tokenized.
  */
class SpaceTokenizerAnnotator(override val name: String, override val props: Properties)
    extends SentencesAnnotator {

  override def newSentenceAnnotation(sentence: Node): Node = {

    val sindex = sentence \@ "id"
    val text = sentence.text
    val range = (0 until text.size)

    def isSpace(c: Char) = c == ' ' || c == '\t'

    val begins = 0 +: (1 until text.size).filter { i => isSpace(text(i-1)) && !isSpace(text(i)) }

    val ends = begins map {
      range indexWhere (i=>isSpace(text(i)), _) match {
        case -1 => text.size
        case e => e
      }
    }

    val tokenSeq = begins.zip(ends).zipWithIndex map { case ((b, e), i) =>
      <token
        id={ sindex + "_tok" + i }
        form={ text.substring(b, e) }
        characterOffsetBegin={ b+"" }
        characterOffsetEnd={ e+"" }/>
    }
    val tokens = <tokens annotators={ name }>{ tokenSeq }</tokens>
    sentence addChild tokens
  }

  override def requires = Set(Requirement.Ssplit)
  override def requirementsSatisfied = Set(Requirement.Tokenize)
}
