package jp.jigg.nlp.pipeline

import java.util.Properties
import scala.annotation.tailrec
import scala.io.Source
import scala.xml.{XML, Node}
import jp.jigg.util.LogUtil.{ track, multipleTrack }

class Pipeline(val props: Properties) {

  // TODO: sort by resolving dependencies
  val annotatorNames = props.getProperty("annotators").split("""[,\s]+""")

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
    case "ccg" => new CCGParseAnnotator(name, props)
    case other =>
      // assuming other is class name?
      // TODO: determining how users can define thier own annotators
      Class.forName(other).getConstructor(classOf[String], classOf[Properties]).newInstance(other, props).asInstanceOf[Annotator]
  }

  def run = {
    val fn = props.getProperty("file")
    val root = fn match {
      case null => sys.error("file property should be given.")
      case fn => initializeXML(jp.jigg.util.IOUtil.openIterator(fn).mkString("\n"))
    }

    val xml = multipleTrack("Annotating %s with %s".format(fn, annotatorNames.mkString(", "))) {
      val annotators = createAnnotators
      try annotate(root, annotators, true)
      finally close(annotators)
    }
    // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
    // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
    // XML.save(in + ".xml", xml, "UTF-8")
    val printer = new scala.xml.PrettyPrinter(500, 2)

    track("Writing to %s".format(fn + ".xml... ")) {
      XML.save(fn + ".xml", XML.loadString(printer.format(xml)), "UTF-8", true, null)
    }
  }

  def shell = {
    val annotators = createAnnotators

    val inputReader = jp.jigg.util.IOUtil.openStandardIn
    def readLine: String = {
      System.err.print("> ")
      inputReader.readLine match {
        case null => ""
        case l if l.trim().size == 0 => readLine
        case l => l
      }
    }
    var in = readLine
    try while (in != "") {
      val xml = annotate(initializeXML(in), annotators)
      val printer = new scala.xml.PrettyPrinter(500, 2)
      println(printer.format(xml))
      in = readLine
    } finally close(annotators)
  }

  def initializeXML(raw: String) = <root><document>{ raw }</document></root>

  def annotate(root: Node, annotators: List[Annotator], verbose: Boolean = false): Node = {
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
}

object Pipeline {
  def main(args: Array[String]): Unit = {
    val props = jp.jigg.util.ArgumentsParser.parse(args.toList)

    val pipeline = new Pipeline(props)
    props.getProperty("file") match {
      case null => pipeline.shell
      case _ => pipeline.run
    }
  }
}
