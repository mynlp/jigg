package jigg.pipeline

import java.util.Properties
import java.io.{BufferedReader, PrintStream}
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
  @Prop(gloss="Print this message and descriptions of specified annotators, e.g., -help ssplit,mecab") var help = ""
  @Prop(gloss="You can add an abbreviation for a custom annotator class with \"-customAnnotatorClass.xxx path.package\"") var customAnnotatorClass = ""

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
      try annotatorNames.map { getAnnotator(_) }.toList
      catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }

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
  def getAnnotator(name: String): Annotator = getAnnotatorCompanion(name) map {
    _.fromProps(name, properties)
  } getOrElse {
    getAnnotatorClass(name) map { clazz =>
      clazz.getConstructor(classOf[String], classOf[Properties]).newInstance(name, properties).asInstanceOf[Annotator]
    } getOrElse { argumentError("annotators", s"Failed to search for custom annotator class: $name") }
  }

  def run = {
    val reader = IOUtil.openIn(file)

    val xml = multipleTrack("Annotating %s with %s".format(file, annotatorNames.mkString(", "))) {
      annotate(reader, true)
    }

    // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
    // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
    // XML.save(in + ".xml", xml, "UTF-8")
    val printer = new scala.xml.PrettyPrinter(500, 2)

    track("Writing to %s".format(file + ".xml... ")) {
      XML.save(file + ".xml", XML.loadString(printer.format(xml)), "UTF-8", true, null)
    }
  }

  def runFromStdin = {
    val reader = IOUtil.openStandardIn
    reader.ready() match {

      case false => shell(reader)

      case true =>
        val xml = annotate(reader, false)
        val printer = new scala.xml.PrettyPrinter(500, 2)

        val writer = IOUtil.openStandardOut
        XML.write(writer, XML.loadString(printer.format(xml)), "UTF-8", true, null)
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
          os.println(s"$name:")
          os.println(getAnnotator(name).description)
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
