package jigg.pipeline

import java.util.Properties
import scala.xml.{Node, Elem}
import scala.reflect.ClassTag
import jigg.util.XMLUtil

trait Annotator extends PropsHolder {

  def name: String = this.getClass.getSimpleName
  val props: Properties = new Properties

  def options = Array[String]()

  final def prop(key: String): Option[String] = jigg.util.PropertiesUtil.findProperty(name + "." + key, props)

  def annotate(annotation: Node): Node

  final def init = { // Called before starting annotation
    initProps
    initSetting
  }

  protected def initSetting = {}

  def close = {} // Resource release etc; detault: do nothing

  def requires = Set.empty[Requirement]
  def requirementsSatisfied = Set.empty[Requirement]
}

/** This is the base trait for all companion object of each Annotator
  */
class AnnotatorCompanion[A<:Annotator](implicit m: ClassTag[A]) {

  /** User can customize how to instantiate an annotator instance from the pipeline if
    *  1) the annotator class has an companion object which inherets AnnotatorCompaion[ThatAnnotator]; and
    *  2) that object overrides fromProps;
    * are satisfied.
    *
    * See MecabAnnotator for example, where fromProps of object MecabAnnotator selects
    * which MecabAnnotator class to invoke, such as IPAMecabAnnotator or JumanMecabAnnotator based on
    * the current dictionary setting of mecab.
    *
    */
  def fromProps(name: String, props: Properties): A =
    m.runtimeClass.getConstructor(classOf[String], classOf[Properties]).newInstance(name, props).asInstanceOf[A]
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
