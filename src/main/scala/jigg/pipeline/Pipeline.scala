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
import java.io.{BufferedReader, BufferedWriter, PrintStream}
import scala.annotation.tailrec
import scala.io.Source
import scala.xml.{XML, Node}
import jigg.util.LogUtil.{ track, multipleTrack }
import jigg.util.{PropertiesUtil => PU}
import jigg.util.IOUtil

class Pipeline(val properties: Properties = new Properties) extends PropsHolder {

  def prop(key: String) = PU.findProperty(key, properties)

  @Prop(gloss="List of annotator names, e.g., ssplit,mecab ssplit|kuromoji|mecab|cabocha|juman|knp|ccg", required=true) var annotators = ""
  @Prop(gloss="Property file") var props = ""
  @Prop(gloss="Input file; if omitted, read from stdin") var file = ""
  @Prop(gloss="Output file; if omitted, `file`.xml is used. Gzipped if suffix is .gz") var output = ""
  @Prop(gloss="Print this message and descriptions of specified annotators, e.g., -help ssplit,mecab") var help = ""
  @Prop(gloss="You can add an abbreviation for a custom annotator class with \"-customAnnotatorClass.xxx path.package\"") var customAnnotatorClass = ""

  // A hack to prevent throwing an exception when -help is given but -annotators is not given.
  // annotators is required prop so it has to be non-empty, but it is difficult to tell that if -help is given it is not necessary.
  (prop("annotators"), prop("help")) match {
    case (None, Some(_)) => properties.put("annotators", "")
    case _ =>
  }

  readProps()

  // TODO: should document ID be given here?  Somewhere else?
  private[this] val documentIDGen = jigg.util.IDGenerator("d")

  val annotatorNames = annotators.split("""[,\s]+""") // PU.safeFind("annotators", props).split("""[,\s]+""")

  val customAnnotatorNameToClassPath = PU.filter(properties) {
    case (k, _) => k.startsWith("customAnnotatorClass.")
  }.map {
    case (k, v) => (k.drop(k.indexOf('.') + 1), v)
  }.toMap

  def createAnnotators: List[Annotator] = {
    val annotators =
      annotatorNames.map { getAnnotator(_) }.toList

    annotators.foldLeft(Set[Requirement]()) { (satisfiedSofar, annotator) =>
      val requires = annotator.requires

      val lacked = requires &~ (requires & satisfiedSofar)
      if (!lacked.isEmpty) argumentError("annotators", "annotator %s requires annotators %s".format(annotator.name, lacked.mkString(", ")))

      Requirement.add(satisfiedSofar, annotator.requirementsSatisfied)
    }
    annotators
  }

  /** User may override this method in a subclass to add more own annotators.
    */
  protected val defaultAnnotatorClassMap: Map[String, Class[_]] = Map(
    "ssplit" -> classOf[RegexSentenceAnnotator],
    "kuromoji" -> classOf[KuromojiAnnotator],
    "mecab" -> classOf[MecabAnnotator],
    "cabocha" -> classOf[CabochaAnnotator],
    "juman" -> classOf[JumanAnnotator],
    "knp" -> classOf[KNPAnnotator],
    "ccg" -> classOf[CCGParseAnnotator]
  )

  /** Or also customizable by overriding this method directory, e.g.,
    *
    * {{{
    * val option = "option"
    * val pipeline = new Pipeline(props) {
    *   override def getAnnotator(name: String) = name match {
    *     case "myAnnotator" => new MyAnnotator(option)
    *     case _ => super.getAnnotator(name)
    *   }
    * }
    * }}}
    *
    */
  def getAnnotator(name: String): Annotator = try {
    getAnnotatorCompanion(name) map {
      _.fromProps(name, properties)
    } getOrElse {
      getAnnotatorClass(name) map { clazz =>
        clazz.getConstructor(classOf[String], classOf[Properties]).newInstance(name, properties).asInstanceOf[Annotator]
      } getOrElse { argumentError("annotators", s"Failed to search for custom annotator class: $name") }
    }
  } catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }

  def getAnnotatorCompanion(name: String): Option[AnnotatorCompanion[Annotator]] = {
    import scala.reflect.runtime.{currentMirror => cm}

    defaultAnnotatorClassMap get(name) flatMap { clazz =>
      val symbol = cm.classSymbol(clazz).companionSymbol
      try Some(cm.reflectModule(symbol.asModule).instance.asInstanceOf[AnnotatorCompanion[Annotator]])
      catch { case e: Throwable => None }
    }
  }

  def getAnnotatorClass(name: String): Option[Class[_]] = {
    defaultAnnotatorClassMap get(name) orElse {
      customAnnotatorNameToClassPath get(name) map { path =>
        resolveAnnotatorClass(path, name)
      } getOrElse {
        resolveAnnotatorClass(name, name)
      }
    }
  }

  private[this] def resolveAnnotatorClass(path: String, name: String): Option[Class[_]] =
    try Some(Class.forName(path)) catch { case e: Throwable => None }

  def run = {
    val reader = IOUtil.openIn(file)

    val xml = multipleTrack("Annotating %s with %s".format(file, annotatorNames.mkString(", "))) {
      annotate(reader, true)
    }

    // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
    // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
    // XML.save(in + ".xml", xml, "UTF-8")
    val printer = new scala.xml.PrettyPrinter(500, 2)

    val outputPath = output match {
      case "" => file + ".xml"
      case _ => output
    }

    track("Writing to %s".format(outputPath + "... ")) {
      val os = IOUtil.openOut(outputPath)
      writeTo(os, xml)
      os.close
    }
  }
  def writeTo(os: BufferedWriter, xml: Node) = {
    val printer = new scala.xml.PrettyPrinter(500, 2)
    val size = (xml \\ "sentences").map(_.child.size).sum
    val outputXML = if (size > 100) xml else XML.loadString(printer.format(xml))
    XML.write(os, outputXML, "UTF-8", true, null)
  }

  def runFromStdin = {
    val reader = IOUtil.openStandardIn
    reader.ready() match {

      case false => shell(reader)

      case true =>
        val xml = annotate(reader, false)
        val writer = output match {
          case "" => IOUtil.openStandardOut
          case _ => IOUtil.openOut(output)
        }
        writeTo(writer, xml)
        writer.close
    }
  }

  private[this] def shell(reader: BufferedReader) = {

    def readLine: String = {
      System.err.print("> ")
      reader.readLine match {
        case null => ""
        case l if l.trim().size == 0 => readLine
        case l => l
      }
    }
    process { annotators =>
      var in = readLine
      while (in != "") {
        val xml = annotate(rootXML(in), annotators, false)
        val printer = new scala.xml.PrettyPrinter(500, 2)
        println(printer.format(xml))
        in = readLine
      }
    }
  }

  private[this] def process[U](f: List[Annotator]=>U) = {
    val annotators = createAnnotators
    annotators foreach { _.init }
    try f(annotators)
    finally annotators foreach { _.close }
  }

  def annotate(reader: BufferedReader, verbose: Boolean = false): Node =
    annotateText(IOUtil.inputIterator(reader).mkString("\n"), verbose)

  def annotateText(text: String, verbose: Boolean = false): Node = process { annotators =>
    val root = rootXML(text) // IOUtil.inputIterator(reader).mkString("\n"))
    annotate(root, annotators, verbose)
  }

  protected def annotate(root: Node, annotators: List[Annotator], verbose: Boolean): Node = {
    def annotateRecur(input: Node, unprocessed: List[Annotator]): Node = unprocessed match {
      case annotator :: tail =>
        val newNode = verbose match {
          case true => track(s"${annotator.name}: ", "", 2) { annotator.annotate(input) }
          case false => annotator.annotate(input)
        }
        annotateRecur(newNode, tail)
      case Nil => input
    }
    annotateRecur(root, annotators)
  }
  protected def rootXML(raw: String) = <root><document id={ documentIDGen.next }>{ raw }</document></root>

  def printHelp(os: PrintStream) = {
    os.println("Usage:")
    os.println(this.description)
    os.println()

    help match {
      case "true" => // when "-help" is used without specified names
      case help =>
        val helpAnnotatorNames = help.split("""[,\s]+""")
        helpAnnotatorNames foreach { name =>
          val annotator = getAnnotator(name)
          os.println(s"$name:")
          os.println("  %-37s: [%s]".format("requires", annotator.requires.mkString(", ")))
          os.println("  %-37s: [%s]".format("requirementsSatisfied", annotator.requirementsSatisfied.mkString(", ")))
          os.println()
          os.println(annotator.description)
          os.println()
        }
    }
  }
}

object Pipeline {
  def main(args: Array[String]): Unit = {
    val props = jigg.util.ArgumentsParser.parse(args.toList)

    try {
      val pipeline = new Pipeline(props)
      PU.findProperty("help", props) match {
        case Some(help) =>
          pipeline.printHelp(System.out)
        case None =>
          pipeline.file match {
            case "" => pipeline.runFromStdin
            case _ => pipeline.run
          }
      }
    } catch {
      case e: ArgumentError =>
        System.err.println(e.getMessage)
    }
  }
}
