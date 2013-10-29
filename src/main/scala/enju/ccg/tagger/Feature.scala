package enju.ccg.tagger

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import enju.ccg.lexicon.{Word, PoS, Category, Dictionary}

sealed trait Feature

// these case classes are for getting autmatically defined classes
case class F1[T1,Template](t1:T1,tmpl:Template) extends Feature
case class F2[T1,T2,Template](t1:T1,t2:T2,tmpl:Template) extends Feature
case class F3[T1,T2,T3,Template](t1:T1,t2:T2,t3:T3,tmpl:Template) extends Feature
case class F4[T1,T2,T3,T4,Template](t1:T1,t2:T2,t3:T3,t4:T4,tmpl:Template) extends Feature

class FUnigramWord[Template](w:Word,c:Category,tmpl:Template) extends F2(w.id,c.id,tmpl) {
  def mkString(lexicon:Dictionary) =
    tmpl + "###" + lexicon.getWord(t1) + "=>" + lexicon.getCategory(t2)
}

class FW(w:Word,c:Category) extends FUnigramWord(w,c,"w")
class FWPrev1(w:Word,c:Category) extends FUnigramWord(w,c,"wPrev1")
class FWPrev2(w:Word,c:Category) extends FUnigramWord(w,c,"wPrev2")

class FBigramWord[Template](w0:Word,w1:Word,c:Category,tmpl:Template) extends F3(
  w0.id,w1.id,c.id,tmpl) {
  def mkString(lexicon:Dictionary) =
    tmpl + "###" + lexicon.getWord(t1) + "###" + lexicon.getWord(t2) + "=>" + lexicon.getCategory(t3)
}

class FWPrev2wPrev1(w0:Word,w1:Word,c:Category) extends FBigramWord(w0,w1,c,"wPrev2_w")
class FWPrev1w(w0:Word,w1:Word,c:Category) extends FBigramWord(w0,w1,c,"wPrev1_w")
class FWWNext1(w0:Word,w1:Word,c:Category) extends FBigramWord(w0,w1,c,"w_wNext1")
class FWNext1WNext2(w0:Word,w1:Word,c:Category) extends FBigramWord(w0,w1,c,"wNext1_wNext2")
