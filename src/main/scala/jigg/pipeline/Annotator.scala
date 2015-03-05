package jigg.pipeline

import java.util.Properties
import scala.xml.{Node, Elem}
import jigg.util.XMLUtil
import scala.reflect.ClassTag

trait Annotator {
  def name: String = this.getClass.toString
  val props: Properties = new Properties

  def prop(key: String): Option[String] = jigg.util.PropertiesUtil.findProperty(name + "." + key, props)

  def annotate(annotation: Node): Node

  def close = {} // Resource release etc; detault: do nothing

  def requires = Set.empty[Requirement]
  def requirementsSatisfied = Set.empty[Requirement]
}

/** This is the base trait for all companion object of each Annotator
  */
class AnnotatorObject[A<:Annotator](implicit m: ClassTag[A]) {

  /** An annotator should implement constructor of type (String, Properties) if it is used in pipeline.
    * In default, such constructor is found with reflection.
    * This behavior is although customized by overriding this method. See MecabAnnotator for example.
    */
  def fromProps(name: String, props: Properties): A =
    m.runtimeClass.getConstructor(classOf[String], classOf[Properties]).newInstance(name, props).asInstanceOf[A]

  // private[this] def fromPropsWithReflection(name: String, props: Properties)() = m.runtimeClass.getConstructor(classOf[String], classOf[Properties]).newInstance(name, props).asInstanceOf[A]

  def options = Array[String]()
}

/** A trait for an annotator which modifies a sentence node. If an annotator is sentence-level
  * annotator such as a parser or pos tagger, it should extend this trait and usually what you
  * should do is only to implement newSentenceAnnotation method, which rewrites a sentence
  * node and returns new one.
  */
trait SentencesAnnotator extends Annotator {
  override def annotate(annotation: Node): Node = {

    XMLUtil.replaceAll(annotation, "sentences") { e =>
      // TODO: sentence level parallelization should be handled here?
      val newChild = e.child map { c =>
        assert(c.label == "sentence") // assuming sentences node has only sentence nodes as children
        val a = newSentenceAnnotation(c)
        a
      }
      e.copy(child = newChild)
    }
  }

  def newSentenceAnnotation(sentence: Node): Node
}
