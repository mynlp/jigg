package jigg.util

import java.io._

import scala.xml._

import scala.io.Source
import scala.collection.mutable.{ArrayBuffer, StringBuilder}

import org.json4s._
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object JSONUtil {

  def toJSON(x: Node): String = toJSONFromNode(x)

  private def toJSONFromNode(node: Node): String = {
    val sb = new StringBuilder
    val escapedsb = new StringBuilder
    val returnsb = new StringBuilder
    sb.append('{')
    sb.append(List("\".tag\":\"",node.label,"\",").mkString)
    sb.append("\".child\":")
    sb.append("[")
    sb.append(serializing(node))
    sb.append("]")
    sb.append("}")
    // The "parse" method can't handle the string with a single backslash,
    // because the "JString" class can't accept such kind of string.
    // To escape this issue, we replace "\\" -> "\\\\" before throwing it to the "parse" method.
    pretty(render(parse(sb.toString.replace("\\","\\\\"))))
  }

  private def serializing[T <: Node](x: T): StringBuilder = {
    val subsb = new StringBuilder
    if(XMLUtil.hasChild(x)){
      val childNode = XMLUtil.getChildNode(x)
      var prefix = ""
      for (i <- childNode){
        val retsb = serializing(i)
        subsb.append(prefix)
        prefix = ","
        subsb.append(List("{\".tag\":\"", i.label, "\",").mkString)
        var prefix2 = ""
        if(!XMLUtil.text(i).isEmpty){
          subsb.append(prefix2)
          val text = new StringBuilder
          Utility.escape(XMLUtil.text(i), text)
          prefix2 = ","
          subsb.append(List("\"text\":\"", text, '"').mkString)
        }
        if (!i.attributes.isEmpty){
          for(elem <- XMLUtil.getAttributionList(i)){
            subsb.append(prefix2)
            prefix2 = ","
            subsb.append(List('"', elem._1, "\":\"", elem._2, "\"").mkString)
          }
        }

        if(retsb.length > 0){
          subsb.append(prefix2)
          subsb.append("\".child\":")
          subsb.append("[")
          subsb.append(retsb)
          subsb.append("]")
        }
        subsb.append('}')
      }
    }
    subsb
  }

  def loadJSONFile(name: String): JValue =
    parse(IOUtil.openIterator(name).mkString)

  def toXML(json: JValue): Node = {
    implicit val formats = DefaultFormats
    val jsonList = json.extract[Map[String, Any]]
    generateXML(jsonList)
  }

  private def generateXML(x:Map[String, Any]): Node = {

    val tagString = x get ".tag" // Option[Any], but this should always exist.
    val textString = x get "text" // This might be None.
    val children: List[Node] =
      x.get(".child").map { _.asInstanceOf[List[Map[String, Any]]]
        .map(generateXML) } getOrElse List()
    val attrs: Map[String, String] =
      x.filter { case (k, v) => k != ".tag" && k != "text" && k != ".child" }
         .map { case (k, v) => (k, v.toString) }

    val node = textString match {
      case Some(text) => <xml>{text}</xml>
      case None => <xml/>
    }

    val tagChanged = node.copy(label = tagString.get.toString)
    val childAdded = XMLUtil.addChild(tagChanged, children) // do nothing when child is empty

    XMLUtil.addAttributes(childAdded, attrs) // the same
  }
}
