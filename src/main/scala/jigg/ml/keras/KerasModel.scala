package jigg.ml.keras

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

import breeze.linalg.DenseMatrix
import jigg.util.HDF5Object
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, _}

class KerasModel(path: String) {

  private val model = HDF5Object(path)

  private val kerasAttribute = model.checkAndGetAttribute("keras_version")
  private val modelAttribute = model.checkAndGetAttribute("model_config")

  private val weightGroups = model.checkAndGetGroup("model_weights")

  def parseConfigToSeq(config: String): Seq[Map[String, Any]] = {
    val jsonValue = parse(config)
    implicit val formats = DefaultFormats
    val jsonList = jsonValue.extract[Map[String, Any]]
    jsonList("config").asInstanceOf[Seq[Map[String, Any]]]
  }

  private val modelValues = parseConfigToSeq(modelAttribute.getValue(0).toString)

  def getConfigs(x: Map[String, Any]): Map[String, Any] = x("config").asInstanceOf[Map[String,Any]]

  def constructNetwork(values: Seq[Map[String, Any]]): Seq[Functor] = values.map{
    x => {
      val configs = getConfigs(x)
      val functor = x("class_name").toString match {
        case "Activation" =>
          configs("activation").toString match{
            case "relu" => Relu
            case "softmax" => Softmax
            case "sigmoid" => Sigmoid
            case "tanh" => Tanh
          }
        case "Convolution1D" =>
          Convolution1D(configs, weightGroups)
        case "Dense" =>
          Dense(configs, weightGroups)
        case "Embedding" =>
          Embedding(configs, weightGroups)
        case "Flatten" => Flatten
        case _ => Empty
      }
      functor
    }
  }

  private val graph:Seq[Functor] = constructNetwork(modelValues)

  def convert(input: DenseMatrix[Float]): DenseMatrix[Float] = callFunctors(input, graph)

  private def callFunctors(input: DenseMatrix[Float], unprocessed:Seq[Functor]): DenseMatrix[Float] = unprocessed match {
    case functor :: tail =>
      val interOutput = functor.convert(input)
      callFunctors(interOutput, tail)
    case Nil => input
  }
}

object KerasModel{
  def apply(path: String): KerasModel = new KerasModel(path)
}
