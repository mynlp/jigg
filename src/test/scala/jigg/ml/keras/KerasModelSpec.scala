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

import java.io._
import org.scalatest._

import jigg.util.HDF5Object

import breeze.linalg.csvread
import breeze.numerics.abs

class KerasModelSpec extends FlatSpec with Matchers{

  def findPath(localPath: String): String = getClass.getClassLoader.getResource(localPath).getPath

  "convert" should "load model and convert input matrix" in {
    val hdf5 = HDF5Object.fromResource("./data/ml/keras/kerasModel/kerasModel_model.h5")
    val model = new KerasModel(hdf5)
    val inputData = csvread(new File(findPath("./data/ml/keras/kerasModel/kerasModel_input.csv")),separator = ',').map{x => x.toFloat}
    val goldData = csvread(new File(findPath("./data/ml/keras/kerasModel/kerasModel_gold.csv")),separator = ',').map{x => x.toFloat}

    val output = model.convert(inputData)

    val diff = abs(output - goldData).forall(x => x < 1e-6.toFloat)

    diff should be (true)
  }

}
