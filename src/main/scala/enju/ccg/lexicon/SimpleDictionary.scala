package enju.ccg.lexicon

class SimpleDictionary(categoryDictionary:CategoryDictionary = new Word2CategoryDictionary) extends Dictionary(categoryDictionary) {
  override val posManager = new NumberedManager[PoS] {
    def createInstance(newId:Int, posStr:String) = SimplePoS(newId, posStr)
  }
}
