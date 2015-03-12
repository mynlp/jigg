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

import scala.collection.mutable.{ArrayBuffer, HashMap, LinkedHashMap}

/**
 * Numbered objects are usually managed by this (and extended) class
 */
@SerialVersionUID(1L)
trait NumberedManager[T<:Numbered[_]] extends Serializable {
  val canonicalMap = new LinkedHashMap[T, T] // non-id object -> with-id object mapping
  protected val objects = new ArrayBuffer[T]   // id -> object mapping

  protected def newId = objects.size
  protected def addEntry(original:T, withId:T): Unit = {
    canonicalMap += original -> withId
    objects += withId
  }
  def apply(i:Int) = objects(i)
  def assignID(original:T): T = {
    require(original.id == 0, "Object given to assignID " + original + " have already have an id: " + original.id)
    canonicalMap.get(original) match {
      case Some(withId) => withId
      case None =>
        val withId = createWithId(original)
        addEntry(original, withId)
        withId
    }
  }
  protected def createWithId(original:T): T // this is a helper called from assignID; please implement this to receive non registered original object and then return the object with id assigned

  type Input
  type GetType

  def getOrCreate(input:Input): T // how to convert input -> object (with id assigned)
  def get(input:Input): GetType    // get without create. If not found, return special object for unknown object, which type can be defined in sub-classes (GetType); user may return Option[T] if not found, but in some cases (such as Word), one may want predefined unknown token.

  protected def getOrNone(input:Input): Option[T] // this defines what input is the un-registered object.

  def unknown: GetType // this return the value of get() when it failed.

  // def transformValues(f:T => T) = {
  //   canonicalMap.zipWithIndex.foreach {
  //     case ((k, v), i) => {
  //       val newV = f(v)
  //       str2v += k -> newV // this update does not change the order of LinkedHashMap
  //       values(i) = newV
  //     }
  //   }
  // }
}

// This is convenient when treating object which have 1-to-1 correspondens to input string -> object (usual case).
// To use this trait, please implement `createWithId` and `createCanonicalInstance` appropriately.
trait StringBaseNumberedManager[T<:Numbered[_]] extends NumberedManager[T] {
  protected val str2objIndex = new HashMap[String, Int]
  override type Input = String
  override def getOrCreate(str:String): T = str2objIndex.get(str) match {
    case Some(i) => objects(i)
    case None =>
      val obj = assignID(createCanonicalInstance(str))
      str2objIndex += str -> obj.id
      obj
  }
  override def getOrNone(str:String): Option[T] = str2objIndex.get(str) map(objects(_)) // please override this when one want to change the search behavior of objects; see CategoryManager for example.

  def createCanonicalInstance(str:String): T
}

// please use with StringBaseNumberedManager
trait UnkObjectReturner[T<:Numbered[_]] {
  type GetType = T
  val unknown: T
  def getOrNone(str:String): Option[T]
  def get(str:String): GetType = getOrNone(str) match {
    case Some(obj) => obj
    case None => unknown // unknown case
  }
}
// TODO: this is experimental; may be used when one want to treat rare-words with converted surface forms
trait UnkWithTemplateReturner[T<:Numbered[_]] extends UnkObjectReturner[T] {

  override def get(str:String): GetType = getOrNone(str) match {
    case Some(obj) => obj
    case None => {
      val convertedStr = extractTemplate(str)
      super.get(convertedStr) // prevent infinite recurse
    }
  }
  def extractTemplate(original:String): String
}
trait OptionReturner[T<:Numbered[_]] {
  type GetType = Option[T]
  val unknown = None
  def getOrNone(str:String): Option[T]
  def get(str:String): GetType = getOrNone(str)
}
