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

import breeze.linalg.{DenseMatrix, DenseVector}
import ucar.nc2.{Variable, Group}

class Dense(inputDim: Int, outputDim: Int) extends Functor{

  override def functorName = "Dense"

  override final def convert(data: DenseMatrix[Float]): DenseMatrix[Float] = {
    val z = data * w
    for (i <- 0 until data.rows){
      z(i, ::) :+= b.t
    }
    z
  }

  private val w = DenseMatrix.zeros[Float](inputDim, outputDim)
  private val b = DenseVector.zeros[Float](outputDim)

  def h5load(weight: Variable, bias: Variable): Unit = {
    val weightData = weight.read
    val weightIndex = weightData.getIndex
    val biasData = bias.read
    val biasIndex = biasData.getIndex
    for(y <- 0 until inputDim)
      for(x <- 0 until outputDim){
        w(y, x) = weightData.getFloat(weightIndex.set(y, x))
        if(y == 0)
          b(x) = biasData.getFloat(biasIndex.set(x))
      }
  }

  override def toString: String = "Dense: {inputDim: " + inputDim + ", outputDim: " + outputDim + "}"

  def head: String = w(0 until 2, ::).toString
}

object Dense{
  def apply(inputDim:Int, outputDim:Int) = new Dense(inputDim, outputDim)

  def apply(configs: Map[String, Any], weightGroups: Group): Dense = {
    val layerName = configs("name").toString
    val params = weightGroups.findGroup(layerName)
    val weightNames = params.findAttribute("weight_names")
    val weight = params.findVariable(weightNames.getStringValue(0))
    val bias = params.findVariable(weightNames.getStringValue(1))
    val dims = weight.getDimensions
    if(dims.size != 2){
      throw new IllegalArgumentException("invalid dimension for Dense class")
    }

    val d = new Dense(dims.get(0).getLength, dims.get(1).getLength)
    d.h5load(weight, bias)
    d
  }
}
