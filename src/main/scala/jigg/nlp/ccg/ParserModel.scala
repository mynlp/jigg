package jigg.nlp.ccg

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

import parser.{
  ActionLabel,
  BeamSearchDecoder,
  DeterministicDecoder,
  InitialFullState,
  LF => Feature,
  StaticOracleGenerator
}
import jigg.ml.{FeatureIndexer, Perceptron}

import parser.{FeatureExtractors, Rule}

case class ParserModel(
  taggerModel: SuperTaggerModel,
  indexer: FeatureIndexer[Feature],
  weights: Array[Float],
  extractors: FeatureExtractors,
  rule: Rule) {

  /** beam = 1 is specially treated only at training.
    */
  def mkTrainDecoder(beam: Int, perceptron: Perceptron[ActionLabel]) = beam match {
    case 1 => mkDeterministicDecoder(perceptron)
    case _ => mkBeamDecoder(beam, perceptron)
  }

  def mkDecoder(beam: Int, perceptron: Perceptron[ActionLabel]) =
    mkBeamDecoder(beam, perceptron)

  private def mkBeamDecoder(beam: Int, perceptron: Perceptron[ActionLabel]) =
    new BeamSearchDecoder(indexer,
      extractors,
      perceptron,
      StaticOracleGenerator,
      rule,
      beam,
      InitialFullState)

  private def mkDeterministicDecoder(perceptron: Perceptron[ActionLabel]) =
    new DeterministicDecoder(indexer,
      extractors,
      perceptron,
      StaticOracleGenerator,
      rule,
      InitialFullState)
}

object ParserModel {

  def saveTo(path: String, model: ParserModel) = {
    System.err.println("Saving parser model to " + path)
    val os = jigg.util.IOUtil.openBinOut(path)
    os.writeObject(model)
    os.close
  }

  def loadFrom(path: String): ParserModel = {
    jigg.util.LogUtil.track(s"Loading parser model in $path ...") {
      val in = jigg.util.IOUtil.openBinIn(path)
      val model = in.readObject.asInstanceOf[ParserModel]
      in.close
      model
    }
  }

  def loadFromJar(beam: Int): ParserModel = {
    val loader = Thread.currentThread.getContextClassLoader
    val modelName = defaultModelPath(beam)
    jigg.util.LogUtil.track("Loading parser model in $modelName ...") {
      val input = loader.getResourceAsStream(modelName)
      val in = jigg.util.IOUtil.openZipBinIn(input)
      val model = in.readObject.asInstanceOf[ParserModel]
      in.close
      model
    }
  }

  def defaultModelPath(beam: Int) = s"ccg-models/parser/beam=${beam}.ser.gz"
}
