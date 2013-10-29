package enju.ccg.lexicon

/**
 * Handles all lexical information, such as Word (type specific information), PoS, Category,
 * and (Word,Pos) -> Category candidates looking up
 * Currently, this class abstract away the language specific information, such as PoS information;
 * A subclass JapaneseDictionary handles JapanesePoS (including Japanese PoS-hierarchy information) instead of SimplePoS, which is handled in SimpleDictionary (we assume it for use in English)
 * 
 * @param catDictionary abstract away the way to look up the dictionary from word/pos to category candidates
 */
@SerialVersionUID(1L)
abstract class Dictionary(private val categoryDictionary:CategoryDictionary) extends Serializable {
  type P <: PoS
  @transient private val categoryParser = new CategoryParser
  private val categoryManager = new CategoryManager
  protected def posManager:NumberedManager[P]
  protected val wordManager:NumberedManager[Word] = new NumberedManager[Word] {
    def createInstance(newId:Int, wordStr:String) = SimpleWord(newId, wordStr)
  }

  def getPoS(str:String):P = posManager.getOrCreate(str)
  def getPoS(id:Int):P = posManager(id)
  def getWord(str:String):Word = wordManager.getOrCreate(str)
  def getWord(id:Int):Word = wordManager(id)
  def getCategory(str:String):Category = categoryManager.assignID(categoryParser.parse(str)) // WARNING: computationaly heavy
  def getCategory(id:Int):Category = categoryManager(id)

  def getCategoryCandidates(word:Word, pos:P):Array[Category] =
    categoryDictionary.getCandidates(word, pos)
  
  def giveIdToWords(type2id:String => Int) = wordManager.transformValues({ word => word.assignClass(type2id(word.v)) })
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
