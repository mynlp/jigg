package enju.pipeline

import java.util.Properties
import scala.annotation.tailrec
import scala.xml._

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
    case "kuromoji" => new KuromojiAnnotator(name, props)
    case "ccg" => new CCGParseAnnotator(name, props)
    case other =>
      // assuming other is class name?
      // TODO: determining how users can define thier own annotators
      Class.forName(other).getConstructor(classOf[String], classOf[Properties]).newInstance(other, props).asInstanceOf[Annotator[_, _]]
  }

  def run = {
    import enju.util.LogUtil._

    val in = props.getProperty("file")

    // Currently following things are supposed, which should be relaxed:
    //  - The first annotator is a kind of tokenizer (StringAnnotator) which reads input files and converts it into an XML node
    //  - It means that we cannot start with existing partially annotated file as input
    def annotateRecur(input: Node, unprocessed: List[Annotator[_, _]]): Node = unprocessed match {
      case annotator :: tail =>
        val newNode = track(s"${annotator.name}: ", "", 2) {
          annotator match {
            case annotator: StringAnnotator =>
              assert(input.label == "Root")
              annotator.annotate(in)
            case annotator: XMLAnnotator =>
              annotator.annotate(input)
          }
        }
        annotateRecur(newNode, tail)
      case Nil => input
    }

    val xml = multipleTrack("Annotating %s with %s".format(in, annotatorNames.mkString(", "))) {
      annotateRecur(<Root />, annotators)
    }
    // The output of basic XML.save method is not formatted, so we instead use PrettyPrinter.
    // However, this method have to convert an entire XML into a String object, which would be problematic for huge dataset.
    // XML.save(in + ".xml", xml, "UTF-8")
    val printer = new scala.xml.PrettyPrinter(500, 2)

    track("Writing to %s".format(in + ".xml... ")) {
      XML.save(in + ".xml", XML.loadString(printer.format(xml)), "UTF-8", true, null)
    }
    //println(printer.format(xml))
  }
}

object Pipeline {
  def main(args: Array[String]): Unit = {
    val props = enju.util.ArgumentsParser.parse(args.toList)
    new Pipeline(props).run
  }
}
