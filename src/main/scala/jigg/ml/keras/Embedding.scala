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

class Embedding(vocabulary: Int, outDim: Int) extends Functor{

  override def functorName = "Embedding"

  override final def convert(data: DenseMatrix[Float]): DenseMatrix[Float] = {
    val arrayOfId = data.reshape(data.size, 1)
    val length = arrayOfId.size
    val z = DenseMatrix.zeros[Float](length, outDim)
    for(i <- 0 until length){
      z(i, ::) := w(arrayOfId(i, 0).asInstanceOf[Int]).t
    }
    z
  }

  private val w = new Array[DenseVector[Float]](vocabulary).map(_ => DenseVector.zeros[Float](outDim))

  def h5load(weight: Variable):Unit = {
    val weightData = weight.read
    val weightIndex = weightData.getIndex
    for(y <- 0 until vocabulary)
      for(x <- 0 until outDim)
        w(y)(x) = weightData.getFloat(weightIndex.set(y, x))
  }

}

object Embedding{
  def apply(vocabulary: Int, outDim: Int) = new Embedding(vocabulary, outDim)

  def apply(configs: Map[String, Any], weightGroups: Group): Embedding = {
    val layerName = configs("name").toString
    val params = weightGroups.findGroup(layerName)
    val weightNames = params.findAttribute("weight_names")
    val weight = params.findVariable(weightNames.getStringValue(0))
    val dims = weight.getDimensions
    if(dims.size != 2){
      throw new IllegalArgumentException("Invalid dimension for Embedding class")
    }
    val e = new Embedding(dims.get(0).getLength, dims.get(1).getLength)
    e.h5load(weight)
    e
  }
}
