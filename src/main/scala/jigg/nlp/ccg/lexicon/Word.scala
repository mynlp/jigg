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

trait Word extends Numbered[String] {
  // additional information is defined in function; may or may not be overridden in val by subclasses
  def classId:Int = throw new RuntimeException("classId is not defined in this Word class.")
  def assignClass(classId:Int):Word = this // default do nothing
  // some morphological information extracted from the surface form might be included ? (e.g., for morphological rich languages)
}

case class SimpleWord(override val id:Int, override val v:String) extends Word {
  override def assignClass(classId:Int) = ClassedWord(id, v, classId)
  override def toString = v
}
case class ClassedWord(override val id:Int,
                       override val v:String,
                       override val classId:Int) extends Word {
  override def assignClass(classId:Int) = ClassedWord(id, v, classId)
  override def toString = v + "[" + classId + "]"
}
