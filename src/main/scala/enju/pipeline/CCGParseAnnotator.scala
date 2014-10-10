package enju.pipeline

import enju.ccg.JapaneseShiftReduceParsing
import enju.ccg.lexicon.{ PoSTaggedSentence, Derivation, Point }

import java.util.Properties
import scala.xml._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

/** Currently this class is ugly; it largely depends on global variables defined in enju.ccg.Options.
  * TODO: revise this class and ShiftReduceParsing class.
  */
class CCGParseAnnotator(val name: String, val props: Properties) extends SentencesAnnotator {

  val parsing = new JapaneseShiftReduceParsing
  configParsing

  val tagger = parsing.tagging.getTagger
  val decoder = parsing.getPredDecoder

  def configParsing = {
    import enju.util.PropertiesUtil.findProperty
    findProperty(name + ".model", props) foreach { enju.ccg.InputOptions.loadModelPath = _ }
    findProperty(name + ".beta", props) foreach { x => enju.ccg.TaggerOptions.beta = x.toDouble }
    findProperty(name + ".maxK", props) foreach { x => enju.ccg.TaggerOptions.maxK = x.toInt }
    findProperty(name + ".beam", props) foreach { x => enju.ccg.ParserOptions.beam = x.toInt }
    parsing.load
  }

  override def newSentenceAnnotation(sentence: Node) = {
    val sid = sentence \ "@id" toString() substring(1) // s12 -> 12
    val tokens = sentence \ "tokens"
    val tokenSeq = tokens \ "token"

    val posTaggedSentence = SentenceConverter.toTaggedSentence(tokenSeq)
    val deriv = getDerivation(posTaggedSentence)
    val point2id = getPoint2id(deriv)

    val spans = new ArrayBuffer[Node]

    def spanid(pointid: Int) ="sp" + sid + "-" + pointid

    deriv.roots foreach { root =>
      deriv foreachPoint({ point =>
        val pid = point2id(point)

        val rule = deriv.get(point).get
        val ruleSymbol = rule.ruleSymbol match {
          case "" => None
          case symbol => Some(Text(symbol))
        }
        val childIds = rule.childPoint.points map { p => spanid(point2id(p)) } match {
          case Seq() => None
          case ids => Some(Text(ids.mkString(" ")))
        }
        val terminalId = childIds match {
          case None => tokenSeq(point.x).attribute("id")
          case _ => None
        }

        spans += <span id={ spanid(pid) } begin={ point.x.toString } end={ point.y.toString } category={ point.category.toString } rule={ ruleSymbol } child={ childIds } terminal={ terminalId } />
      }, root)
    }

    val rootids = deriv.roots.map { p => spanid(point2id(p)) }.mkString(" ")

    val ccg = <ccg root={ rootids }>{ spans }</ccg>
    enju.util.XMLUtil.addChild(sentence, ccg)
  }

  object SentenceConverter {
    val dict = parsing.tagging.dict
    def toTaggedSentence(tokenSeq: NodeSeq) = {
      val terminalSeq = tokenSeq map { token =>
        val surf = dict.getWordOrCreate(token \ "@surf" toString())
        val base = dict.getWordOrCreate(token \ "@base" toString())
        val katsuyou = token \ "@inflectionForm" toString() match {
          case "*" => "_"; case x => x
        }
        val posSeq = Seq("@pos", "@pos1", "@pos2", "@pos3") map { token \ _ toString() }
        val pos = posSeq.indexOf("*") match {
          case -1 => posSeq.mkString("-")
          case idx => posSeq.take(idx).mkString("-")
        }

        val combinedPoS = dict.getPoSOrCreate(pos + "/" + katsuyou)

        (surf, base, combinedPoS)
      }
      new PoSTaggedSentence(terminalSeq.map(_._1), terminalSeq.map(_._2), terminalSeq.map(_._3))
    }
  }

  def getDerivation(sentence: PoSTaggedSentence): Derivation = {
    val beta = enju.ccg.TaggerOptions.beta
    val maxK = enju.ccg.TaggerOptions.maxK
    val beam = enju.ccg.ParserOptions.beam
    val superTaggedSentence = sentence.assignCands(tagger.candSeq(sentence, beta, maxK))
    decoder.predict(superTaggedSentence)
  }

  def getPoint2id(deriv: Derivation): Map[Point, Int] = {
    val map = new HashMap[Point, Int]
    var i = 0
    deriv.roots foreach { root =>
      deriv foreachPoint({ point =>
        map += point -> i
        i += 1
      }, root)
    }
    map.toMap
  }

  override def requires = Set(Annotator.JaTokenize)
  override def requirementsSatisfied = Set(Annotator.JaCCG)
}
