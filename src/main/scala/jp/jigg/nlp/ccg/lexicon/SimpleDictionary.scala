package jp.jigg.nlp.ccg.lexicon

class SimpleDictionary extends Dictionary(new Word2CategoryDictionary) {
  override val posManager = new PoSManager {
    def createWithId(original: PoS) = SimplePoS(newId, original.v)
    def createCanonicalInstance(str:String) = SimplePoS(0, str)
  }
  override val categoryManager = new CategoryManager {
    override def createCanonicalInstance(str: String): Category = EnglishCategoryParser.parse(str)
  }
}
