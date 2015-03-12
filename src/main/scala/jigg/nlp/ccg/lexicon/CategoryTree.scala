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

case class CategoryTree(var surface:String, slash:Slash, left:CategoryTree, right:CategoryTree) {
  def isLeaf = left == null && right == null
  def setSurface:CategoryTree = {
    def childSurface(child:CategoryTree) =
      if (child.isLeaf) child.surface else '(' + child.surface + ')'

    if (isLeaf) assert(surface != null)
    else surface = childSurface(left) + slash + childSurface(right)
    this
  }
  def foreachLeaf(f:CategoryTree=>Any):Unit = {
    if (isLeaf) f(this)
    else List(left,right).foreach(_.foreachLeaf(f))
  }
}

object CategoryTree {
  def createLeaf(surface:String) = CategoryTree(surface, null, null, null)
  def createInternal(slash:Slash, left:CategoryTree , right:CategoryTree) =
    CategoryTree(null, slash, left, right)
}
