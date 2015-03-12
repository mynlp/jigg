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
import Slash._

sealed trait Category extends Numbered[Unit] {
  override def v:Unit = {}
  def toStringNoFeature: String
}

@SerialVersionUID(6748884927580538343L)
case class AtomicCategory(override val id:Int, base:String, feature:CategoryFeature) extends Category {
  override def toString = feature.toString match {
    case "" => base
    case s => base + "[" + s + "]"
  }

  override def toStringNoFeature = base
}
@SerialVersionUID(3754315949719248198L)
case class ComplexCategory(override val id:Int,
                           left:Category, right:Category,
                           slash:Slash) extends Category {
  def toStringChild(child:Category) = child match {
    case AtomicCategory(_,_,_) => child.toString
    case ComplexCategory(_,_,_,_) => "(" + child.toString + ")"
  }
  override def toString = toStringChild(left) + slash + toStringChild(right)

  def toStringChildNoFeature(child:Category) = child match {
    case AtomicCategory(_,_,_) => child.toStringNoFeature
    case ComplexCategory(_,_,_,_) => "(" + child.toStringNoFeature + ")"
  }
  override def toStringNoFeature = toStringChildNoFeature(left) + slash + toStringChildNoFeature(right)
}
