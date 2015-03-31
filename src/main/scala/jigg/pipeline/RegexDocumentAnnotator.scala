package jigg.pipeline

import java.util.Properties
import scala.xml.Node

class RegexDocumentAnnotator(override val name: String, override val props: Properties) extends Annotator {
  private[this] val documentIDGen = jigg.util.IDGenerator("d")
  override def annotate(annotation: Node): Node = {
    val raw = annotation.text
    val reg = """\n{2,}"""
    val documents = raw.split(reg).map {
      str =>
      <document id={ documentIDGen.next }>{ str }</document>
    }

    <root>{ documents }</root>
  }

  override def requires = Set()
  override def requirementsSatisfied = Set(Requirement.Document)
}
