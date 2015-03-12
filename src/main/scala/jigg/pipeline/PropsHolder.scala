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

import java.lang.reflect.Method
import java.io.PrintStream
import scala.collection.mutable.HashMap
import annotation.meta.getter

trait PropsHolder { outer =>
  type Prop = jigg.util.Prop @getter

  def prop(key: String): Option[String]
  protected def prefix: String = ""

  def makeFullName(key: String) = prefix match { case "" => key; case x => x + "." + key }

  // this is OptInfo specialized for Scala var; TODO: implement for Java variable (with Field).
  private[this] case class OptVarInfo(name: String, p: Prop, getMethod: Method, setMethod: Method) {
    def get: Any = getMethod.invoke(outer)
    def set(value: String) = {
      val v = castValueStr(value)
      setMethod.invoke(outer, v.asInstanceOf[AnyRef])
    }

    val fullName = outer.makeFullName(name)

    def required = if (p.required) " (required)" else ""

    override def toString = "  %-30s <%4s>: %s%s [%s]".format(fullName, typeStr, p.gloss, required, this.get)

    lazy val typeStr = get match {
      case v: Int => "int"
      case v: Double => "dble"
      case v: Short => "shrt"
      case v: String => "str"
      case v: Boolean => "bool"
      case v => "unk"
    }

    def castValueStr(str: String) = try typeStr match {
      case "int" => str.toInt
      case "dble" => str.toDouble
      case "shrt" => str.toShort
      case "str" => str
      case "bool" => str.toBoolean
    } catch { case e: Throwable => argumentError(name) }
  }

  private[this] val nameToOptInfo = new HashMap[String, OptVarInfo]

  final def readProps() = {
    val nameToGetterSetter = getNameToGetterSetter
    fillInNameToOptInfo(nameToGetterSetter)

    nameToOptInfo.iterator foreach { case (key, optInfo) =>
      prop(key) foreach { value =>
        optInfo.set(value)
      }
    }
    checkRequirements
  }

  private[this] def getNameToGetterSetter = {
    val nameToGetterSetter = new HashMap[String, (Method, Method)]

    val methods = this.getClass.getMethods
    methods foreach { method =>
      method.getAnnotation(classOf[Prop]).asInstanceOf[Prop] match {
        case null =>
        case ann => nameToGetterSetter += (method.getName -> (method, null))
      }
    }
    methods foreach {
      case setter if setter.getName.endsWith("_$eq") =>
        val getterName = setter.getName.replace("_$eq", "")
        nameToGetterSetter get (getterName) foreach { case (getter, null) =>
          nameToGetterSetter += (getterName -> (getter, setter))
        }
      case _ =>
    }
    nameToGetterSetter
  }

  private[this] def fillInNameToOptInfo(nameToGetterSetter: HashMap[String, (Method, Method)]) = {
    nameToGetterSetter.iterator foreach { case (name, (getter, setter)) =>
      val p = getter.getAnnotation(classOf[Prop]).asInstanceOf[Prop]
      nameToOptInfo += (name -> (OptVarInfo(name, p, getter, setter)))
    }
  }

  private[this] def checkRequirements = {
    val missings = nameToOptInfo.values.filter { optInfo =>
      optInfo.p.required && prop(optInfo.name) == None
    }
    missings match {
      case Seq() =>
      case missings =>
        val comment = "Missing required option(s):"
        val usage = missings map(_ + "") mkString("\n")
        throw new ArgumentError(comment + "\n" + usage)
    }
  }

  def description: String = propertyMessage

  def propertyMessage() = {
    if (nameToOptInfo.isEmpty) readProps

    nameToOptInfo.values.map(_ + "").mkString("\n")
  }

  def argumentError(key: String, msg: String = "") = {
    val fullName = makeFullName(key)
    val comment = if (msg != "") msg else s"Some problem detected in $fullName."
    val usage = nameToOptInfo get(key) match {
      case Some(optInfo) => optInfo + ""
      case None => s"$fullName is not a valid parameter name. Maybe the implementation is corrupted."
    }
    throw new ArgumentError(comment + "\n" + usage)
  }
}
