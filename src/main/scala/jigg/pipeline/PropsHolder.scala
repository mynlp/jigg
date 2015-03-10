package jigg.pipeline

import java.lang.reflect.Method
import java.io.PrintStream
import scala.collection.mutable.HashMap
import annotation.meta.getter

trait PropsHolder { outer =>
  type Prop = jigg.util.Prop @getter

  def prop(key: String): Option[String]
  protected def prefix: String = ""

  // this is OptInfo specialized for Scala var; TODO: implement for Java variable (with Field).
  private[this] case class OptVarInfo(name: String, p: Prop, getMethod: Method, setMethod: Method) {
    def get = getMethod.invoke(outer)
    def set(value: String) = setMethod.invoke(outer, value)

    val fullName = outer.prefix match { case "" => name; case x => x + "." + name }

    def required = if (p.required) " (required)" else ""

    override def toString = "  %-30s: %s%s [%s]".format(fullName, p.gloss, required, this.get)
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
        System.err.println("Missing required option(s):")
        missings foreach { System.err.println(_) }
        throw MissingArgumentException
    }
  }

  def printPropertyMessage(os: PrintStream) = {
    if (nameToOptInfo.isEmpty) readProps

    nameToOptInfo.values foreach { os.println(_) }
  }
}

object MissingArgumentException extends RuntimeException("")
