package enju.pipeline

import java.util.Properties
import scala.annotation.tailrec
import scala.io.Source
import scala.xml.{XML, Node}
import enju.util.LogUtil.{ track, multipleTrack }

class Pipeline(val props: Properties) {

  // TODO: sort by resolving dependencies
  val annotatorNames = props.getProperty("annotators").split("""[,\s]+""")
  val annotators = annotatorNames.map { getAnnotator(_) }.toList

  annotators.foldLeft(Set[Annotator.Requirement]()) { (satisfiedSofar, annotator) =>
    val requires = annotator.requires

    val lacked = requires &~ (requires & satisfiedSofar)
    if (!lacked.isEmpty) sys.error("annotator %s requires annotators %s" format(annotator.name, lacked.mkString(", ")))

    satisfiedSofar | annotator.requirementsSatisfied
  }

  /** User may override this method in a subclass to add more own annotators?
    */
  def getAnnotator(name: String): Annotator[_, _] = name match {
    case "ssplit" => new RegexSentenceAnnotator(name, props)
    case "kuromoji" => new KuromojiAnnotator(name, props)
    case "ccg" => new CCGParseAnnotator(name, props)
    case other =>
      // assuming other is class name?
      // TODO: determining how users can define thier own annotators
      Class.forName(other).getConstructor(classOf[String], classOf[Properties]).newInstance(other, props).asInstanceOf[Annotator[_, _]]
  }

  def run = {
    //val in = props.getProperty("file")
    val fn = props.getProperty("file")
    val in = Source.fromFile(fn).getLines().toStream  // TODO: consider re-design of input of StringAnnotator
    val xml = multipleTrack("Annotating %s with %s".format(fn, annotatorNames.mkString(", "))) {
      annotate(in, true)
    }
    // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
    // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
    // XML.save(in + ".xml", xml, "UTF-8")
    val printer = new scala.xml.PrettyPrinter(500, 2)

    track("Writing to %s".format(fn + ".xml... ")) {
      XML.save(fn + ".xml", XML.loadString(printer.format(xml)), "UTF-8", true, null)
    }
    //println(printer.format(xml))
  }

  def shell = {
    val inputReader = enju.util.IOUtil.openStandardIn
    def readLine: Stream[String] = {
      System.err.print("> ")
      inputReader.readLine match {
        case null => Stream()
        case l if l.size == 0 => readLine
        case l => Stream(l)
      }
    }
    var in = readLine
    while (in != Stream()) {
      val xml = annotate(in)
      val printer = new scala.xml.PrettyPrinter(500, 2)
      println(printer.format(xml))
      in = readLine
    }
  }

  def annotate(in: Stream[String], verbose: Boolean = false): Node = {
    // Currently following things are supposed, which should be relaxed:
    //  - The first annotator is a kind of tokenizer (StringAnnotator) which reads input files and converts it into an XML node
    //  - It means that we cannot start with existing partially annotated file as input
    def annotateRecur(input: Node, unprocessed: List[Annotator[_, _]]): Node = unprocessed match {
      case annotator :: tail =>
        val newNode = verbose match {
          case true => track(s"${annotator.name}: ", "", 2) { annotateWith(annotator, input) }
          case false => annotateWith(annotator, input)
        }
        annotateRecur(newNode, tail)
      case Nil => input
    }

    def annotateWith(annotator: Annotator[_, _], input: Node): Node = annotator match {
      case annotator: StringAnnotator =>
        assert(input.label == "Root")
        annotator.annotate(in)
      case annotator: XMLAnnotator =>
        annotator.annotate(input)
    }

    annotateRecur(<Root />, annotators)
  }
}

object Pipeline {
  def main(args: Array[String]): Unit = {
    val props = enju.util.ArgumentsParser.parse(args.toList)

    val pipeline = new Pipeline(props)
    props.getProperty("file") match {
      case null => pipeline.shell
      case _ => pipeline.run
    }
  }
}
