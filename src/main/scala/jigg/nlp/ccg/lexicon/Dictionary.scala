package jigg.nlp.ccg.lexicon

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

/**
 * Handles all lexical information, such as Word (type specific information), PoS, Category,
 * and (Word,Pos) -> Category candidates looking up
 * Currently, this class abstract away the language specific information, such as PoS information;
 * A subclass JapaneseDictionary handles JapanesePoS (including Japanese PoS-hierarchy information) instead of SimplePoS, which is handled in SimpleDictionary (we assume it for use in English)
 *
 * @param catDictionary abstract away the way to look up the dictionary from word/pos to category candidates
 */

trait WordManager extends StringBaseNumberedManager[Word] with UnkObjectReturner[Word] {
  override val unknown = getOrCreate("UNK_TYPE")
  override type GetType = Word
}
trait PoSManager extends StringBaseNumberedManager[PoS] with UnkObjectReturner[PoS] {
  override val unknown = getOrCreate("UNK_POS")
  override type GetType = PoS
}

@SerialVersionUID(1L)
abstract class Dictionary(val categoryDictionary:CategoryDictionary,
                          protected val wordManager:WordManager = // you can override when you want to add information to each word type
  new WordManager {
    override def createWithId(original:Word) = SimpleWord(newId, original.v)
    override def createCanonicalInstance(str:String) = SimpleWord(0, str)
  }) extends Serializable {
  protected val categoryManager = new CategoryManager
  protected def posManager: PoSManager

  def getPoSOrCreate(str:String): PoS = posManager.getOrCreate(str)
  def getPoS(str:String): PoS = posManager.get(str)
  def getPoS(id:Int):PoS = posManager(id)

  def getWordOrCreate(str: String): Word = wordManager.getOrCreate(str)
  def getWord(str:String): Word = wordManager.get(str)
  def getWord(id:Int): Word = wordManager(id)

  def getCategoryOrCreate(str:String): Category = categoryManager.getOrCreate(str)
  def getCategory(str:String): Option[Category] = categoryManager.get(str)
  def getCategory(id:Int): Category = categoryManager(id)

  def getCategoryCandidates(word:Word, pos:PoS):Array[Category] =
    categoryDictionary.getCandidates(word, pos) match {
      case Array() => Array(categoryManager.unkCategory)
      case cands => cands
    }

  def unkType:Word = wordManager.unknown
  def unkPoS:PoS = posManager.unknown

  // def giveIdToWords(type2id:String => Int) = wordManager.transformValues({ word => word.assignClass(type2id(word.v)) })
}

object DictionaryTest {
  /**
   * (de)serialization test (because sbt cannot properly treat the class loader when deserialization);
   * see: http://www.scala-sbt.org/release/docs/Detailed-Topics/Running-Project-Code.html
   */
  def main(args:Array[String]):Unit = {
    testSimpleDictionary
    testJapaneseDictionary
  }
  def testSimpleDictionary = {
    val dict = new SimpleDictionary
    testDictionary(dict, "NN")
    println("testEnglishDictionary successfully passed.")
  }
  def testJapaneseDictionary = {
    val dict = new JapaneseDictionary
    testDictionary(dict, "動詞-自立/未然レル接続")
    println("testJapaneseDictionary successfully passed.")
  }
  def testDictionary[Dict<:Dictionary](dict:Dict, insertPoS:String) = {
    import java.io._
    val tmp = File.createTempFile("dictionary", "bin")
    val out = new ObjectOutputStream(new FileOutputStream(tmp))

    val originalPoS = dict.getPoS("NN")
    out.writeObject(dict)
    out.close

    val in = new ObjectInputStream(new FileInputStream(tmp))
    val decodedDict = in.readObject.asInstanceOf[Dictionary]
    assert(originalPoS == decodedDict.getPoS(originalPoS.id))
    tmp.deleteOnExit
  }
}
