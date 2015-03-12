package jigg.pipeline

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

import java.util.Properties
import scala.xml.{Node, Elem}
import scala.reflect.ClassTag
import scala.collection.JavaConverters._
import jigg.util.XMLUtil

trait Annotator extends PropsHolder {

  def name: String = this.getClass.getSimpleName

  override final def prefix = name // prefix is required in PropsHolder; for Annotators, it corresponds to its name.

  def props: Properties = new Properties

  final def prop(key: String): Option[String] = jigg.util.PropertiesUtil.findProperty(name + "." + key, props)

  def annotate(annotation: Node): Node

  def init = {} // Called before starting annotation

  def close = {} // Resource release etc; detault: do nothing

  def buildCommand(cmd: String, args: String*): java.util.List[String] = (cmd.split("\\s+") ++ args).toSeq.asJava

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
