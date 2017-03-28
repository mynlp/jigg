package jigg.pipeline

/*
 Copyright 2013-2015 Hiroshi Noji
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licencses/LICENSE-2.0
     
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitation under the License.
*/

import java.util.Properties

import jigg.ml.keras.KerasParser

import scala.xml.Node
import jigg.util.XMLUtil.RichNode

class SsplitKerasAnnotator(override val name: String, override val props: Properties) extends Annotator {

  def defaultModelFileName = "ssplit-model.h5"
  def defaultTableFileName = "table.json"

  @Prop(gloss = "Model file (if omitted, the default path is used to search file)") var model = ""
  @Prop(gloss = "Lookup table for mapping character into id space") var table = ""

  readProps()

  lazy val sentenceSplitter: QueueSentenceSplitter = new QueueSentenceSplitter

  override def description =s"""${super.description}

  A sentence splitter based on NN-model training on keras.

"""

  override def init() = {
    sentenceSplitter
  }

  private[this] val sentenceIDGen = jigg.util.IDGenerator("s")

  override def annotate(annotation: Node): Node = {
    annotation.replaceAll("document") { e =>
      val splitRegex = """\n+""".r
      val line = e.text
      val sentenceBoundaries = 0 +: splitRegex.findAllMatchIn(line).map(_.end).toVector :+ line.length
      val sentences: Vector[Node] = sentenceBoundaries.sliding(2).toVector flatMap { case Seq(begin_, end_) =>
        def isSpace(c: Char) = c == ' ' || c == '\t' || c == '\n'
        val snippet = line.substring(begin_, end_)
        val begin = snippet.indexWhere(!isSpace(_)) match {
          case -1 => begin_
          case offset => begin_ + offset
        }
        val end = snippet.lastIndexWhere(!isSpace(_)) match {
          case -1 => begin_
          case offset => begin_ + offset + 1
        }

        val preSentence: String = line.substring(begin, end)
        val boundaries = sentenceSplitter.s.parsing(preSentence)

        boundaries.flatMap{x =>
          val subline = preSentence.substring(x._1, x._2)
          if (subline.isEmpty)
            None
          else{
            Option(<sentence
            id={ sentenceIDGen.next }
            characterOffsetBegin={ x._1 + begin + ""}
            characterOffsetEnd={ x._2 + + begin + ""}>{ subline }</sentence>)
          }
        }
      }
      e addChild <sentences>{ sentences }</sentences>
    }
  }

  class QueueSentenceSplitter {
    private def makeSentenceSplitter: KerasParser = model match {
      case "" =>
        System.err.println(s"No model file is given. Try to search default path: $defaultModelFileName")
        table match {
          case "" =>
            System.err.println(s"No lookup table file is given. Try to search default path: $defaultTableFileName")
            KerasParser(defaultTableFileName, defaultTableFileName)
          case tableFile =>
            KerasParser(defaultTableFileName, tableFile)
        }
      case modelFile =>
        table match {
          case "" =>
            System.err.println(s"No lookup table file is given. Try to search default path: $defaultTableFileName")
            KerasParser(model, defaultTableFileName)
          case tableFile =>
            KerasParser(model, tableFile)
        }
    }

    val s: KerasParser = makeSentenceSplitter
  }

  override def requires() = Set()
  override def requirementsSatisfied(): Set[Requirement] = Set(Requirement.Ssplit)

}

object SsplitKerasAnnotator extends AnnotatorCompanion[SsplitKerasAnnotator] {

  val model = ""
  val table = ""

}
