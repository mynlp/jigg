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

// Convolution operator for filtering neighborhoods of one-dimensional inputs.
class Convolution1D(outCh: Int, width: Int, inputDim: Int, padding: Boolean) extends Functor{

  override def functorName = "Convolution1D"

  override final def convert(data: DenseMatrix[Float]): DenseMatrix[Float] = {
    val work = im2col(data) * w
    for (i <- 0 until work.rows)
      work(i, ::) :+= b.t

    work
  }

  private val w = DenseMatrix.zeros[Float](width * inputDim, outCh)
  private val b = DenseVector.zeros[Float](outCh)

  private val paddingRow: Int = if (padding) {
    (width - 1) / 2
  } else {
    0
  }

  private def im2col(x: DenseMatrix[Float]): DenseMatrix[Float] = {
    val inputSize = width * inputDim
    val work = DenseMatrix.zeros[Float](x.rows, inputSize)
    val x1 = x.rows

    for(k1 <- 0 until x1)
        for(d2 <- 0 until width)
          for(d1 <- 0 until inputDim) {
            val i1 = k1 - paddingRow + d2
            val j1 = d1 + d2 * inputDim
            if (i1 >= 0 & i1 < x1)
              work(k1, j1) = x(i1, d1)
            else
              work(k1, j1) = 0.0.toFloat
          }

    work
  }

  private def h5load(weight: Variable, bias: Variable): Unit = {
    val weightData = weight.read
    val weightIndex = weightData.getIndex
    val biasData = bias.read
    val biasIndex = biasData.getIndex
    for(i <- 0 until width)
      for(j <- 0 until inputDim)
        for(x <- 0 until outCh){
          val y = i * inputDim + j
          w(y, x) = weightData.getFloat(weightIndex.set(i, 0, j, x))
          if(y == 0)
            b(x) = biasData.getFloat(biasIndex.set(x))
        }
  }

  override def toString: String = "Convolution1D: {outCh: " + outCh + ", width: " + width + ", inputDim: " + inputDim + ", padding" + padding + "}"

}

object Convolution1D{
  def apply(outCh: Int, width: Int, inputDim: Int, padding: Boolean) = new Convolution1D(outCh, width, inputDim, padding)

  def apply(configs: Map[String, Any], weightGroups: Group): Convolution1D = {
    val layerName = configs("name").toString
    val params = weightGroups.findGroup(layerName)
    val weightNames = params.findAttribute("weight_names")
    val borderMode = configs("border_mode").toString match {
      case "same" => true
      case _ => false
    }
    val weight = params.findVariable(weightNames.getStringValue(0))
    val bias = params.findVariable(weightNames.getStringValue(1))
    val dims = weight.getDimensions
    if(dims.size != 4){
      throw new IllegalArgumentException("invalid dimension for Convolution1D class")
    }

    val c = new Convolution1D(dims.get(3).getLength, dims.get(0).getLength,
      dims.get(2).getLength, borderMode)
    c.h5load(weight,bias)
    c
  }
}
