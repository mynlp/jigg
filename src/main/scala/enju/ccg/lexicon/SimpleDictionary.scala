package enju.ccg.lexicon

class SimpleDictionary(categoryDictionary:CategoryDictionary = new Word2CategoryDictionary) extends Dictionary(categoryDictionary) {
  override type P = SimplePoS
  override val posManager = new NumberedManager[P] {
    def createInstance(newId:Int, posStr:String) = SimplePoS(newId, posStr)
  }
}
