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

import java.util.Properties

import org.scalatest.FunSuite
import org.scalatest.Matchers._

import jigg.util.{HDF5Object, LookupTable}

class KerasParserTest extends FunSuite{

  val model = new KerasModel(HDF5Object.fromResource("./data/keras/ssplit_model.h5"))
  val table = LookupTable.fromResource("data/keras/jpnLookupCharacter.json")

  val parser = new KerasParser(model, table)

  test("get an offset list from pattern1") {
    val pattern = Array[Int](0,1,1,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (Array[(Int, Int)]((0,3),(3,6)))
  }

  test("get an offset list from pattern2") {
    val pattern = Array[Int](0,1,1,2,2,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (Array[(Int, Int)]((0,3),(5,8)))
  }

  test("get an offset list from pattern3") {
    val pattern = Array[Int](0,1,1,2,0,1,1,2)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (Array[(Int, Int)]((0,3),(4,7)))

  }

  test("get an offset list from pattern4") {
    val pattern = Array[Int](2,2,0,1,1,2,0,1,1,2)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (Array[(Int, Int)]((2,5),(6,9)))
  }

  test("get an offset list from pattern5") {
    val pattern = Array[Int](1,1,1,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (Array[(Int, Int)]((0,3),(3,6)))
  }

  test("get an offset list from pattern6") {
    val pattern = Array[Int](2,2,1,1,1,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (Array[(Int, Int)]((2,5),(5,8)))
  }

  test("get an offset list from pattern7") {
    val pattern = Array[Int](0,1,1,0,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (Array[(Int, Int)]((0,3),(3,4),(4,7)))
  }
}
