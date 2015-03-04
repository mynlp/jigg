package jigg.pipeline

import java.util.Properties
import java.io.BufferedReader
import scala.annotation.tailrec
import scala.io.Source
import scala.xml.{XML, Node}
import jigg.util.LogUtil.{ track, multipleTrack }
import jigg.util.{PropertiesUtil => PU}
import jigg.util.IOUtil

class Pipeline(val props: Properties = new Properties) {

  // TODO: should document ID be given here?  Somewhere else?
  private[this] var documentID: Int = 0
  def newDocumentID(): String = {
    val new_id = "d" + documentID
    documentID += 1
    new_id
  }

  // TODO: sort by resolving dependencies
  val annotatorNames = PU.safeFind("annotators", props).split("""[,\s]+""")

  def createAnnotators: List[Annotator] = {
    val annotators = annotatorNames.map { getAnnotator(_) }.toList

    annotators.foldLeft(Set[Annotator.Requirement]()) { (satisfiedSofar, annotator) =>
      val requires = annotator.requires

      val lacked = requires &~ (requires & satisfiedSofar)
      if (!lacked.isEmpty) sys.error("annotator %s requires annotators %s" format(annotator.name, lacked.mkString(", ")))

      satisfiedSofar | annotator.requirementsSatisfied
    }
    annotators
  }

  def close(annotators: Seq[Annotator]) = {
    annotators foreach (_.close)
  }

  /** User may override this method in a subclass to add more own annotators?
    */
  def getAnnotator(name: String): Annotator = name match {
    case "ssplit" => new RegexSentenceAnnotator(name, props)
    case "kuromoji" => new KuromojiAnnotator(name, props)
    case "mecab" => new MecabAnnotator(name, props)
    case "cabocha" => new CabochaAnnotator(name, props)
    case "juman" => new JumanAnnotator(name, props)
    case "knp" => new KNPAnnotator(name, props)
    case "ccg" => new CCGParseAnnotator(name, props)
    case other =>
      // assuming other is class name?
      // TODO: determining how users can define thier own annotators
      Class.forName(other).getConstructor(classOf[String], classOf[Properties]).newInstance(other, props).asInstanceOf[Annotator]
  }

  def run = {
    val fn = PU.safeFind("file", props)
    val reader = IOUtil.openIn(fn)

    val xml = multipleTrack("Annotating %s with %s".format(fn, annotatorNames.mkString(", "))) {
      annotate(reader, true)
    }

    // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
    // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
    // XML.save(in + ".xml", xml, "UTF-8")
    val printer = new scala.xml.PrettyPrinter(500, 2)

    track("Writing to %s".format(fn + ".xml... ")) {
      XML.save(fn + ".xml", XML.loadString(printer.format(xml)), "UTF-8", true, null)
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
    val annotators = createAnnotators

    def readLine: String = {
      System.err.print("> ")
      reader.readLine match {
        case null => ""
        case l if l.trim().size == 0 => readLine
        case l => l
      }
    }
    var in = readLine
    try while (in != "") {
      val xml = annotate(rootXML(in), annotators, false)
      val printer = new scala.xml.PrettyPrinter(500, 2)
      println(printer.format(xml))
      in = readLine
    } finally close(annotators)
  }

  def annotate(reader: BufferedReader, verbose: Boolean = false): Node =
    annotateText(IOUtil.inputIterator(reader).mkString("\n"), verbose)

  def annotateText(text: String, verbose: Boolean = false): Node = {
    val annotators = createAnnotators
    val root = rootXML(text) // IOUtil.inputIterator(reader).mkString("\n"))
    try annotate(root, annotators, verbose)
    finally close(annotators)
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
  protected def rootXML(raw: String) = <root><document id={ newDocumentID() }>{ raw }</document></root>
}

object Pipeline {
  def main(args: Array[String]): Unit = {
    val props = jigg.util.ArgumentsParser.parse(args.toList)

    val pipeline = new Pipeline(props)
    PU.findProperty("file", props) match {
      case None => pipeline.runFromStdin
      case _ => pipeline.run
    }
  }
}
