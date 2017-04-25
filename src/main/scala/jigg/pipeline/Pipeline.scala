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
import java.io.{BufferedReader, PrintStream, Writer}
import scala.annotation.tailrec
import scala.xml.{XML, Node}
import scala.collection.JavaConverters._
import jigg.util.LogUtil.{ track, multipleTrack }
import jigg.util.{PropertiesUtil => PU, IOUtil, JSONUtil}
import jigg.util.XMLUtil.RichNode
import org.json4s.jackson.JsonMethods

class Pipeline(val properties: Properties = new Properties) extends PropsHolder {

  def prop(key: String) = PU.findProperty(key, properties)

  @Prop(gloss="List of annotator names, e.g., corenlp[tokenize,ssplit],berkeleyparser", required=true) var annotators = ""
  @Prop(gloss="Property file") var props = ""
  @Prop(gloss="Input file; if omitted, read from stdin") var file = ""
  @Prop(gloss="Output file; if omitted, `file`.xml is used. Gzipped if suffix is .gz. If JSON mode is selected, suffix is .json") var output = ""
  @Prop(gloss="Print this message and descriptions of specified annotators, e.g., -help ssplit,mecab") var help = ""
  @Prop(gloss="You can add an abbreviation for a custom annotator class with \"-customAnnotatorClass.xxx path.package\"") var customAnnotatorClass = ""
  @Prop(gloss="Number of threads for parallel annotation (use all if <= 0)") var nThreads = -1
  @Prop(gloss="Output format, [xml/json]. Default value is 'xml'.") var outputFormat = "xml"
  @Prop(gloss="Check requirement, [true/false/warn]. Default value is 'true'.") var checkRequirement = "true"
  @Prop(gloss="Input format, [text/xml/json]. Default value is 'text'.") var inputFormat = "text"

  // A hack to prevent throwing an exception when -help is given but -annotators is not given.
  // annotators is required prop so it has to be non-empty, but it is difficult to tell that if -help is given it is not necessary.
  (prop("annotators"), prop("help")) match {
    case (None, Some(_)) => properties.put("annotators", "")
    case _ =>
  }

  readProps()

  def toSerialMode() = {
    nThreads = 1
    properties.setProperty("nThreads", "1")
  }

  /** These default annotators can be used without manual build.
    */
  protected val defaultAnnotatorClassMap: Map[String, Class[_]] = Map(
    "dsplit" -> classOf[RegexDocumentAnnotator],
    "ssplit" -> classOf[RegexSentenceAnnotator],
    "spaceTokenize" -> classOf[SpaceTokenizerAnnotator],
    "mecab" -> classOf[MecabAnnotator],
    "cabocha" -> classOf[CabochaAnnotator],
    "juman" -> classOf[JumanAnnotator],
    "knp" -> classOf[SimpleKNPAnnotator],
    "knpDoc" -> classOf[DocumentKNPAnnotator],
    "jaccg" -> classOf[CCGParseAnnotator],
    "corenlp" -> classOf[StanfordCoreNLPAnnotator],
    "berkeleyparser" -> classOf[BerkeleyParserAnnotator],
    "kuromoji" -> classOf[KuromojiAnnotator],
    "syntaxnetpos" -> classOf[SyntaxNetPOSAnnotator],
    "syntaxnetparse" -> classOf[SyntaxNetParseAnnotator],
    "syntaxnet" -> classOf[SyntaxNetFullAnnotator],
    "collapseddep" -> classOf[StanfordCollapsedDependenciesAnnotator],
    "ssplitKeras" -> classOf[SsplitKerasAnnotator],
    "bunsetsuKeras" -> classOf[BunsetsuKerasAnnotator]
  )

  // TODO: should document ID be given here?  Somewhere else?
  private[this] val documentIDGen = jigg.util.IDGenerator("d")

  val annotatorNames = {
    val p = """\"(.*)\"""".r
    val _annotators = annotators match {
      case p(a) => a
      case _ => annotators
    }
    val pattern = """[^,\s]+\[[^\[]*\]|[^,\[\]\s]+""".r
    pattern.findAllIn(_annotators).toIndexedSeq
  }

  // Some known annotators, e.g., corenlp, are found from here
  val knownAnnotatorNameToClassPath: Map[String, String] = {
    val buildInfo = annotator.BuildInfo
    import scala.reflect.runtime.universe._
    val rm = scala.reflect.runtime.currentMirror
    val accessors = rm.classSymbol(buildInfo.getClass).toType.decls.collect {
      case m: MethodSymbol if
        m.isGetter && m.isPublic && m.name.toString.startsWith("ann_") => m
    }
    val instanceMirror = rm.reflect(buildInfo)
    accessors.map { m =>
      m.name.toString.drop(4) -> instanceMirror.reflectMethod(m).apply().toString
    }.toMap
  }

  val customAnnotatorNameToClassPath: Map[String, String] = PU.filter(properties) {
    case (k, _) => k.startsWith("customAnnotatorClass.")
  }.map {
    case (k, v) => (k.drop(k.indexOf('.') + 1), v)
  }.toMap

  override def description = {

    val customAnnotatorsStr = customAnnotatorNameToClassPath.keys match {
      case Seq() => ""
      case annotators => annotators mkString ", "
    }

    s"""${super.description}

Currently the annotators listed below are installed. See the detail of each annotator with "-help annotator_name".

  ${defaultAnnotatorClassMap.keys mkString ", "}
  ${knownAnnotatorNameToClassPath.keys mkString ", "}
  ${customAnnotatorsStr}"""
  }

  /** To output help message even when annotators do not satisfy requirement dependencies,
    * we make the annotator list lazy.
    */
  lazy val annotatorList = createAnnotatorList()

  def close() = annotatorList foreach { _.close() }

  def createAnnotatorList(): List[Annotator] = {
    val annotatorList =
      annotatorNames.map { getAnnotator(_) }.toList

    def check(procError: (RequirementError, RequirementSet) => RequirementSet) =
      annotatorList.foldLeft(RequirementSet()) { (satisfiedSoFar, annotator) =>
        try annotator.checkRequirements(satisfiedSoFar)
        catch { case e: RequirementError => procError(e, satisfiedSoFar)}
      }

    checkRequirement match {
      case "true" => check { (e, satisfiedSoFar) =>
        argumentError("annotators", e.getMessage)
      }
      case "warn" => check { (e, satisfiedSoFar) =>
        System.out.println(s"[warn] %s".format(e.getMessage))
        satisfiedSoFar
      }
      case "false" => {
        System.err.println(s"[warn] SKIP `checkRequirements` for all selected annotators.")
      }
      case _ =>{
        argumentError("checkRequirement")
      }
    }
    annotatorList foreach (_.init)
    annotatorList
  }

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
    } orElse {
      getAnnotatorClass(name) map { clazz =>
        clazz.getConstructor(classOf[String], classOf[Properties])
          .newInstance(name, properties).asInstanceOf[Annotator] }
    } getOrElse {
      argumentError("annotators", s"Failed to search for custom annotator class: $name")
    }
  } catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }

  def getAnnotatorCompanion(name: String): Option[AnnotatorCompanion[Annotator]] = {
    import scala.reflect.runtime.{currentMirror => cm}

    getAnnotatorClass(name) flatMap { clazz =>
      val symbol = cm.classSymbol(clazz).companion
      try Some(cm.reflectModule(symbol.asModule).instance.asInstanceOf[AnnotatorCompanion[Annotator]])
      catch { case e: Throwable => None }
    }
  }

  def getAnnotatorClass(name: String): Option[Class[_]] = {
    val noOption = name.indexOf('[') match {
      case -1 => name
      case b => name.substring(0, b)
    }
    customAnnotatorNameToClassPath get(noOption) flatMap { path =>
      resolveAnnotatorClass(path, noOption)
    } orElse {
      defaultAnnotatorClassMap get(noOption)
    } orElse {
      knownAnnotatorNameToClassPath get(noOption) flatMap { path =>
        resolveAnnotatorClass(path, noOption)
      }
    } orElse {
      resolveAnnotatorClass(noOption, noOption)
    }
  }

  private[this] def resolveAnnotatorClass(path: String, name: String): Option[Class[_]] =
    try Some(Class.forName(path)) catch { case e: Throwable => None }

  def run = {
    val reader = IOUtil.openIn(file)

    val xml = multipleTrack("Annotating %s with %s".format(file, annotatorNames.mkString(", "))) {
      annotate(reader, true)
    }

    // If option of json is true, the output of this method is formatted JSON.
    outputFormat match {
      case "json" => {
        val outputPath = output match {
          case "" => file + ".json"
          case _ => output
        }

        track("Writing to %s".format(outputPath + "... ")) {
          val jsonString = JSONUtil.toJSON(xml)
          val os = IOUtil.openOut(outputPath)
          os.write(jsonString)
          os.close()
        }
      }
      case _ => { 
        // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
        // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
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
    }
  }

  def writeTo(os: Writer, xml: Node) = {
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
        outputFormat match {
          case "json"  =>
            val jsonString = JSONUtil.toJSON(xml)
            writer.write(jsonString)
          case _ =>
            writeTo(writer, xml)
        }
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

    // Shell mode does not perform prallel annotaiton.
    // The intention for this is that we want to avoid longer loading time by
    // making many instances of annotators, considering the main usage of shell mode
    // is for small annotations or debugging.
    toSerialMode()

    process { annotators =>
      var in = readLine
      while (in != "") {
        val xml = annotate(rootXML(in), annotators, false)
        outputFormat match {
          case "json" =>
            val jsonString = JSONUtil.toJSON(xml)
            println(jsonString)
          case _ =>
            val printer = new scala.xml.PrettyPrinter(500, 2)
            println(printer.format(xml))
        }
        in = readLine
      }
    }
  }

  private[this] def process[U](f: List[Annotator]=>U) = {
    f(annotatorList)
  }

  def annotate(reader: BufferedReader, verbose: Boolean = false): Node = inputFormat match {
    case "text" => annotateText (IOUtil.inputIterator (reader).mkString ("\n"), verbose)
    case "xml" =>
      process { annotators => annotate(XML.load(reader).toUnformatted, annotators, verbose) }
    case "json" =>
      process { annotators => annotate(JSONUtil.toXML(JsonMethods.parse(reader)), annotators, verbose) }
    case _ => argumentError("inputFormat")
  }

  def annotateText(text: String, verbose: Boolean = false): Node = process { annotators =>
    val root = rootXML(text) // IOUtil.inputIterator(reader).mkString("\n"))
    annotate(root, annotators, verbose)
  }

  def annotate(text: String) = annotateText(text)

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

    def removeTextInDoc(node: Node): Node =
      node.replaceAll ("document") { e => e.removeText() }

    removeTextInDoc(annotateRecur(root, annotators))
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
          pipeline.printHelp(System.out) // do not close with help mode
        case None =>
          try pipeline.file match {
            case "" => pipeline.runFromStdin
            case _ => pipeline.run
          } finally pipeline.close()
      }
    } catch {
      case e: ArgumentError =>
        System.err.println(e.getMessage)
      case e: UnsupportedClassVersionError =>
        System.err.println(s"Failed to start jigg due to incompatibility of Java version. Installing the latest version of Java would resolve the problem.\n${e.getMessage()}")
    }
  }

}
