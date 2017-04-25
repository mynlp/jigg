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

object Relu extends Functor{

  override def functorName = "Relu"

  override final def convert(data: DenseMatrix[Float]): DenseMatrix[Float] = data.map(x =>
    if(x > 0.0.toFloat) x else 0.0.toFloat
  )

  def apply(x: DenseMatrix[Float]): DenseMatrix[Float] = this.convert(x)

}
