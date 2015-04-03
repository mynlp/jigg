package jigg.pipeline

import java.util.Properties
import scala.xml.Node

class RegexDocumentAnnotator(override val name: String, override val props: Properties) extends Annotator {

  @Prop(gloss = "Regular expression to segment documents") var pattern = """\n{2,}"""
  readProps()

  private[this] val documentIDGen = jigg.util.IDGenerator("d")
  override def annotate(annotation: Node): Node = {
    val raw = annotation.text
    val documents = raw.split(pattern).map {
      str =>
      <document id={ documentIDGen.next }>{ str }</document>
    }

    <root>{ documents }</root>
  }

  override def requires = Set()
  override def requirementsSatisfied = Set(Requirement.Document)
}
