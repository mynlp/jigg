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
import scala.xml.Node

import jigg.ml.keras.{KerasModel, KerasParser}
import jigg.util.{HDF5Object, IOUtil, LookupTable}
import jigg.util.XMLUtil.RichNode

trait KerasAnnotator extends Annotator {

  def defaultModelPath: String
  def defaultTablePath: String

  def model: String
  def table: String

  def loadParser(): KerasParser  = {
    val hdf5 = model match {
      case "" =>
        System.err.println(s"No model file is given. Defaulting to $defaultModelPath ...")
        HDF5Object.fromResource(defaultModelPath)
      case _ =>
        HDF5Object.fromFile(model)
    }
    val kerasModel = new KerasModel(hdf5)

    val lookupTable = table match {
      case "" =>
        System.err.println(s"No lookup table file is given. Defaulting to $defaultTablePath ...")
        LookupTable.fromResource(defaultTablePath)
      case _ =>
        LookupTable.fromFile(table)
    }
    new KerasParser(kerasModel, lookupTable)
  }
}

class SsplitKerasAnnotator(override val name: String, override val props: Properties) extends KerasAnnotator {

  def defaultModelPath = "jigg-models/keras/ssplit_model.h5"
  def defaultTablePath = "jigg-models/keras/jpnLookupCharacter.json"

  @Prop(gloss = "Model file (use the default if ommited)") var model = ""
  @Prop(gloss = "Lookup table for mapping character into id space") var table = ""

  readProps()

  // lazy val sentenceSplitter: QueueSentenceSplitter = new QueueSentenceSplitter
  lazy val sentenceSplitter = loadParser()

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
      val sentences: Seq[Node] = sentenceBoundaries.sliding(2).toArray.flatMap { case Seq(begin_, end_) =>
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
        val boundaries = sentenceSplitter.parsing(preSentence)

        boundaries.flatMap { x =>
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

  override def requires() = Set()
  override def requirementsSatisfied(): Set[Requirement] = Set(Requirement.Ssplit)

}
