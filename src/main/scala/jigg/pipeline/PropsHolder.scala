package jigg.pipeline

import java.lang.reflect.Method
import scala.collection.mutable.HashMap
import annotation.meta.getter

trait PropsHolder {
  type Prop = jigg.util.Prop @getter

  def prop(key: String): Option[String]

  // this is OptInfo specialized for Scala var; TODO: implement for Java variable (with Field).
  case class OptVarInfo(name: String, p: Prop, getMethod: Method, setMethod: Method)

  private[this] val nameToOptInfo = new HashMap[String, OptVarInfo]

  final def initProps = {
    val nameToGetterSetter = getNameToGetterSetter
    fillInNameToOptInfo(nameToGetterSetter)

    nameToOptInfo.iterator foreach { case (key, optInfo) =>
      prop(key) foreach { value =>
        optInfo.setMethod.invoke(this, value)
      }
    }
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
}
