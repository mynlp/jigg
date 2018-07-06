package jigg.pipeline

/*
 Copyright 2013-2017 Hiroshi Noji

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
import java.io.{PrintStream, PrintWriter, Writer}
import scala.xml.{XML, Node}
import jigg.util.{CoNLLUtil, IOUtil, PropertiesUtil => PU, JSONUtil}
import jigg.util.XMLUtil.RichNode
import org.json4s.jackson.JsonMethods

class OutputConverter(val properties: Properties = new Properties) extends PropsHolder {

  def prop(key: String) = PU.findProperty(key, properties)

  @Prop(gloss="Input file; if omitted, read from stdin") var file = ""
  @Prop(gloss="Output file; if omitted, output to stdout.") var output = ""
  @Prop(gloss="Input format (xml|json). Default is xml.") var inputFormat = "xml"
  @Prop(gloss="Ouput format (xml|json|conllu)") var outputFormat = "conllu"
  @Prop(gloss="Unit of conllu dependencies (word|chunk)") var unit = "word"
  @Prop(gloss="Print this message") var help = ""

  readProps()

  def run() = help match {
    case "true" =>
      printHelp(System.out)
    case _ => {
      System.err.print(s"Loading $file...")
      val xml = loadInput()
      System.err.println("done.")
      outputFormat match {
        case "xml" => writeInXML(xml)
        case "json" => writeInJson(xml)
        case "conllu" => writeInCoNLLU(xml)
      }
    }
  }

  def loadInput(): Node = {
    val reader = file match {
      case "" => IOUtil.openStandardIn
      case _ => IOUtil.openIn(file)
    }
    val xml = inputFormat match {
      case "xml" => XML.load(reader).toUnformatted
      case "json" => JSONUtil.toXML(JsonMethods.parse(reader))
      case _ => argumentError("inputFormat")
    }
    reader.close
    xml
  }

  def mkWriter(): Writer = output match {
    case "" => IOUtil.openStandardOut
    case _ => IOUtil.openOut(output)
  }

  def writeInXML(xml: Node) = IOUtil.writing(mkWriter _) { w =>
    OutputConverter.writeInXML(w, xml)
  }

  def writeInCoNLLU(xml: Node) = {
    val conllu = unit match {
      case "word" => CoNLLUtil.toCoNLLUInWord(xml)
      case "chunk" => CoNLLUtil.toCoNLLUInChunk(xml)
      case _ => argumentError("unit")
    }
    IOUtil.writing(mkWriter _) { w =>
      val pw = new PrintWriter(w)
      for (line <- conllu) pw.println(line)
    }
  }

  def writeInJson(xml: Node) = IOUtil.writing(mkWriter _) { w =>
    val jsonstr = JSONUtil.toJSON(xml)
    w.write(jsonstr)
  }

  def printHelp(os: PrintStream) = {
    os.println("Usage:")
    os.println(this.description)
    os.println()
  }
}

object OutputConverter {

  // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
  // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
  def writeInXML(writer: Writer, xml: Node) = {
    val printer = new scala.xml.PrettyPrinter(500, 2)
    val size = (xml \\ "sentences").map(_.child.size).sum
    val outputXML = if (size > 100) xml else XML.loadString(printer.format(xml))
    XML.write(writer, outputXML, "UTF-8", true, null)
  }

  def main(args: Array[String]): Unit = {
    val props = jigg.util.ArgumentsParser.parse(args.toList)
    try {
      val conv = new OutputConverter(props)
      conv.run()
    } catch {
      case e: ArgumentError =>
        System.err.println(e.getMessage)
    }
  }
}
