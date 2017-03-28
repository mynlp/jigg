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

class KerasParserTest extends FunSuite{

  def findPath(localPath: String): String = getClass.getClassLoader.getResource(localPath).getPath

  val modelPath: String = findPath("./data/keras/ssplit_model.h5")
  val tablePath: String = findPath("data/keras/jpnLookupCharacter.json")

  val parser = KerasParser(modelPath, tablePath)

  test("get an offset list from pattern1") {
    val pattern = List[Int](0,1,1,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (List[(Int, Int)]((0,3),(3,6)))
  }

  test("get an offset list from pattern2") {
    val pattern = List[Int](0,1,1,2,2,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (List[(Int, Int)]((0,3),(5,8)))
  }

  test("get an offset list from pattern3") {
    val pattern = List[Int](0,1,1,2,0,1,1,2)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (List[(Int, Int)]((0,3),(4,7)))

  }
  
  test("get an offset list from pattern4") {
    val pattern = List[Int](2,2,0,1,1,2,0,1,1,2)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (List[(Int, Int)]((2,5),(6,9)))
  }

  test("get an offset list from pattern5") {
    val pattern = List[Int](1,1,1,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (List[(Int, Int)]((0,3),(3,6)))
  }

  test("get an offset list from pattern6") {
    val pattern = List[Int](2,2,1,1,1,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (List[(Int, Int)]((2,5),(5,8)))
  }

  test("get an offset list from pattern7") {
    val pattern = List[Int](0,1,1,0,0,1,1)
    val ranges  = parser.getOffsets(pattern)
    ranges should be (List[(Int, Int)]((0,3),(3,4),(4,7)))
  }
}
