package jigg.util

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
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, _}

class LookupTable(path: String) {

  private val rawTable = JsonMethods.parse(IOUtil.openIn(path))

  implicit private val formats = DefaultFormats
  private val tables = rawTable.extract[Map[String, Map[String, Map[String, String]]]]

  private val key2id = tables("_lookup")("_key2id")
  private val id2key = tables("_lookup")("_id2key")

  // For raw text
  def encodeCharacter(str: String): DenseMatrix[Float] = {
    val strArray = str.map{x =>
      // Note: For skipping unknown character, this encoder returns dummy id.
      key2id.getOrElse(x.toString, "3").toFloat
    }.toArray
    new DenseMatrix[Float](1, str.length, strArray)
  }

  // For list of words
  def encodeWords(words: List[String]): DenseMatrix[Float] = {
    val wordsArray = words.map{x =>
      // Note: For skipping unknown words, this encoder returns dummy id.
      key2id.getOrElse(x.toString, "3").toFloat
    }.toArray
    new DenseMatrix[Float](1, words.length, wordsArray)
  }

  def decode(data: DenseMatrix[Float]): List[String] = data.map{x => id2key.getOrElse(x.toInt.toString, "NONE")}.toArray.toList

  def getId(key: String): Int = key2id.getOrElse(key, "0").toInt
  def getId(key: Char): Int = getId(key.toString)

  def getKey(id: Int): String = id2key.getOrElse(id.toString, "UNKNOWN")

}

object LookupTable {
  def apply(path: String) = new LookupTable(path)
}