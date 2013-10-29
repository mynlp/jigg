package enju.ccg.lexicon
import scala.collection.mutable.HashMap

@SerialVersionUID(1L)
sealed trait CategoryDictionary extends Serializable {
  type Key
  type UnkKey
  val categoryMap = new HashMap[Key, Array[Category]]
  val unkCategoryMap = new HashMap[UnkKey, Array[Category]]

  def key(word:Word, pos:PoS):Key
  def unkKey(pos:PoS):UnkKey
  
  def getCandidates(word:Word, pos:PoS):Array[Category] = categoryMap.get(key(word, pos)) match {
    case Some(categories) => categories
    case None => unkCategoryMap.get(unkKey(pos)) match {
      case Some(categories) => categories
      case None => Array[Category]()
    }
  }
  def registCandidates(word:Word, pos:PoS, candidates:Array[Category]) = key(word, pos) match {
    case k => categoryMap += (k -> (categoryMap.get(k) match {
      case Some(alreadyExist) => (candidates ++ alreadyExist).distinct
      case None => candidates.distinct
    }))
  }
  def registUnkCandiates(pos:PoS, candidates:Array[Category]) = unkKey(pos) match {
    case k => unkCategoryMap += (k -> (unkCategoryMap.get(k) match {
      case Some(alreadyExist) => (candidates ++ alreadyExist).distinct
      case None => candidates.distinct
    }))
  }
}

class Word2CategoryDictionary extends CategoryDictionary {
  type Key = Int
  type UnkKey = Int
  override def key(word:Word, pos:PoS) = word.id
  override def unkKey(pos:PoS) = pos.id  
}

class WordPoS2CategoryDictionary extends CategoryDictionary {
  type Key = (Int, Int)
  type UnkKey = Int
  override def key(word:Word, pos:PoS) = (word.id, pos.id)
  override def unkKey(pos:PoS) = pos.id
}

class WordSecondFineTag2CategoryDictionary extends CategoryDictionary {
  override type Key = (Int, Int)
  override type UnkKey = Int
  override def key(word:Word, pos:PoS) = (word.id, pos.second.id)
  override def unkKey(pos:PoS) = pos.second.id
}
