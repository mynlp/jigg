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
  def resetWithSentences(sentences: Seq[GoldSuperTaggedSentence], unkThreathold: Int) = {
    val counts = new HashMap[Key, Int]
    sentences foreach { sentence => (0 until sentence.size) foreach { i =>
      val k = key(sentence.base(i), sentence.pos(i))
      counts.getOrElseUpdate(k, 0)
      counts(k) += 1
    }}

    sentences foreach { sentence => (0 until sentence.size) foreach { i =>
      val k = key(sentence.base(i), sentence.pos(i))
      if (counts(k) >= unkThreathold) registCandidates(sentence.base(i), sentence.pos(i), Array(sentence.cat(i)))
      registUnkCandiates(sentence.pos(i), Array(sentence.cat(i)))
    }}
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

class WordSecondWithConj2CategoryDictionary extends CategoryDictionary {
  override type Key = (Int, Int)
  override type UnkKey = Int
  override def key(word:Word, pos:PoS) = (word.id, pos.secondWithConj.id)
  override def unkKey(pos:PoS) = pos.secondWithConj.id
}
