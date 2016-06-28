package jigg.util

import java.io._

import scala.xml._

import scala.io.Source
import scala.collection.mutable.{StringBuilder, ArrayBuffer}

import org.json4s._
import org.json4s.DefaultFormats
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object JSONUtil {

  object XMLParser {

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

    def isTextNode(x: Node): Boolean = !XMLUtil.text(x).trim.isEmpty

    def getAttributionList(x: Node): Seq[(String, String)] = (
      for{
        elem <- x.attributes.seq
        n = elem.key -> elem.value.toString
      } yield n
    ).toList

    def hasAttribution(x: Node): Boolean = !x.attributes.isEmpty
  }

  def readJSON(file: String): String = toJSONFromNode(XML.loadFile(file))

  def toJSON(x: Node): String = toJSONFromNode(x)

  private def toJSONFromNode(node: Node): String = {
    val xml = XML.loadString(node.toString.split("\n").mkString)
    val sb = new StringBuilder

    sb.append('{')
    sb.append(List("\".tag\":\"",node.label,"\",\n").mkString)
    sb.append("\".child\":")
    sb.append("[\n")
    
    sb.append(serializing(xml))

    sb.append("]\n")
    sb.append("}\n")

    pretty(render(parse(sb.toString)))
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
        subsb.append(List("{\".tag\":\"", i.label, "\",\n").mkString)
        var prefix2 = ""
        if(XMLParser.isTextNode(i)){
          subsb.append(prefix2)
          val text = new StringBuilder
          Utility.escape(XMLUtil.text(i).trim, text)
          prefix2 = ",\n"
          subsb.append(List("\"text\":\"", text, '"').mkString)
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
          subsb.append("\".child\":")
          subsb.append("[\n")
          subsb.append(retsb)
          subsb.append("]\n")
        }
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
      writer.write(toJSON(x))
      writer.close()
    } catch {
      case e: FileNotFoundException => 
        System.err.println(e.getMessage)
      case e: IOException =>
        System.err.println(e.getMessage)
    }
  }

  def writeToJSON[T <: Node](x: T, os: BufferedWriter): Unit = {
    os.write(toJSON(x))
  }

  def parseJSON(json: String): JValue = parse(json)

  def loadJSONFile(name: String): JValue = {
    val sb = new StringBuilder
    for (line <- Source.fromFile(name).getLines){
      sb.append(line)
    }
    parse(sb.toString)
  }

  def toXML(json: JValue): Node = {
    implicit val formats = DefaultFormats
    val jsonList = json.extract[Map[String, Any]].toList
    generateXML(jsonList)
  }

  private def generateXML(x:List[(String, Any)]): Node = {
    var tagString = ""
    var textString = ""
    val attributeList = new ArrayBuffer[(String, String)]
    val nodeSeq = new ArrayBuffer[Node]
    var hasChildFlag = false
    var hasTextFlag = false

    for (i <- x){
      if (i._1 == ".tag")
        tagString = i._2.toString
      else if (i._1 == "text"){
        hasTextFlag = true
        textString = i._2.toString
      }
      else if (i._1 == ".child"){
        hasChildFlag = true
        for (j <- i._2.asInstanceOf[List[Map[String, Any]]])
          nodeSeq += generateXML(j.toList)
      }
      else
        attributeList += (i._1.toString -> i._2.toString)
    }

    val node = hasChildFlag match {
      case true => {
        hasTextFlag match {
          case true => <xml>{textString}</xml>.copy(label = tagString)
          case false => <xml></xml>.copy(label = tagString)
        }
      }
      case false => <xml/>.copy(label = tagString)
    }

    val fixnode = XMLUtil.addAttributes(node, attributeList.toMap)

    if (hasChildFlag)
      XMLUtil.addChild(fixnode, scala.xml.NodeSeq.fromSeq(nodeSeq))
    else
      fixnode
  }
}
