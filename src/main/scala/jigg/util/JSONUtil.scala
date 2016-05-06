package jigg.util

import java.io._
import scala.xml._
import scala.io.Source
import scala.collection.mutable.StringBuilder

object JSONUtil {

  object XMLParser {
    // If text node has newline character, Node.child method doesn't work collectly.
    def trimNode(x: Node): Node = 
      XML.loadString(x.toString.split("\n").mkString)

    def hasChild(x: Any): Boolean = x match {
      case x: Elem => if (x.child.length > 0) true else false
      case x: List[_] if x forall (_.isInstanceOf[Node]) =>
        x forall (hasChild(_))
      case _ => false
    }

    def getChildNode(x: Any): List[Node] = x match {
      case x: Elem => (x.child filter (_.label != "#PCDATA")).toList
      case x: List[_] if x forall (_.isInstanceOf[Node]) =>
        (x.map(getChildNode(_))).flatten
      case _ => Nil
    }

    def getText(x: Node): String = {
      (
        this.trimNode(x).child.collect {
          case Text(t) => t
        }
      ).map(_.trim).filterNot(_.isEmpty).mkString
    }

    def isTextNode(x: Node): Boolean = !getText(x).isEmpty

    def getAttributionList(x: Node): Seq[(String, String)] = (
      for{
        elem <- x.attributes.seq
        n = elem.key -> elem.value.toString
      } yield n
    ).toList

    def hasAttribution(x: Node): Boolean = !x.attributes.isEmpty
  }

  def toJSON(x: Any): StringBuilder = x match {
    case x: String if x.endsWith(".xml")    => toJSONFromNode(XML.loadFile(x))
    case x: Elem      => toJSONFromNode(x)
    case _            => new StringBuilder("")
  }

  private def toJSONFromNode(node: Elem): StringBuilder = {
    val xml = XML.loadString(node.toString.split("\n").mkString)
    val sb = new StringBuilder

    sb.append('{')
    sb.append(List('"',node.label,"\":").mkString)
    sb.append("{ \"_\":")
    sb.append("[\n")
    
    sb.append(serializing(xml))

    sb.append("]\n")
    sb.append('}')
    sb.append("}\n")

    sb
  }

  private def serializing[T <: Node](x: T): StringBuilder = {
    val subsb = new StringBuilder
    if(XMLParser.hasChild(x)){
      val childNode = XMLParser.getChildNode(x)
      var prefix = ""
      for (i <- childNode){
        val retsb = serializing(i)
        subsb.append(prefix)
        prefix = ",\n"
        subsb.append(List("{ \"", i.label, "\":").mkString)
        subsb.append("{\n")
        var prefix2 = ""
        if(XMLParser.isTextNode(i)){
          subsb.append(prefix2)
          prefix2 = ",\n"
          subsb.append(List("\"text\":\"", XMLParser.getText(i),'"').mkString)
        }
        if (XMLParser.hasAttribution(i)){
          for(elem <- XMLParser.getAttributionList(i)){
            subsb.append(prefix2)
            prefix2 = ",\n"
            subsb.append(List('"', elem._1, "\":\"", elem._2, "\"").mkString)
          }
        }

        if(retsb.length > 0){
          subsb.append(prefix2)
          subsb.append("\"_\":")
          subsb.append("[\n")
          subsb.append(retsb)
          subsb.append("]\n")
        }
        subsb.append('}')
        subsb.append('}')
      }
    }
    subsb
  }

  def writeToJSON[T <: Node](x: T, outputfile: String): Unit = {
    val filePath = 
      if (outputfile.endsWith(".json"))
        outputfile
      else
        List(outputfile,".json").mkString
    try {
      val writer = IOUtil.openOut(outputfile)
      writer.write(toJSON(x).toString)
      writer.close()
    } catch {
      case e: FileNotFoundException => 
        System.err.println(e.getMessage)
      case e: IOException =>
        System.err.println(e.getMessage)
    }
  }

  def writeToJSON[T <: Node](x: T, os: BufferedWriter): Unit = {
    os.write(toJSON(x).toString)
  }
}
