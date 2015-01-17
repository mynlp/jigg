package jp.jigg.nlp.ccg.lexicon

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
