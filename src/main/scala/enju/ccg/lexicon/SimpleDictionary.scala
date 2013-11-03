package enju.ccg.lexicon

class SimpleDictionary(categoryDictionary:CategoryDictionary = new Word2CategoryDictionary) extends Dictionary(categoryDictionary) {
  override val posManager = new PoSManager {
    def createWithId(original: PoS) = SimplePoS(0, original.v)
    def createCanonicalInstance(str:String) = SimplePoS(0, str)
  }
  
}
