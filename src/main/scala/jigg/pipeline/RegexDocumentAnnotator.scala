package jigg.pipeline

import java.util.Properties
import scala.xml.Node

class RegexDocumentAnnotator(override val name: String, override val props: Properties) extends Annotator {
  private[this] var documentID: Int = 0

  override def annotate(annotation: Node): Node = {

    def newDocumentID(): String = {
      val new_id = "d" + documentID
      documentID += 1
      new_id
    }

    val raw = annotation.text
    val reg = """\n{2,}"""
    val documents = raw.split(reg).map {
      str =>
      <document id={ newDocumentID() }>{ str }</document>
    }

    <root>{ documents }</root>
  }

  override def requires = Set()
  override def requirementsSatisfied = Set(Requirement.Document)
}
