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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
//import scala.collection.mutable.ListBuffer
//import scala.collection.immutable.Lis
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.xml._
import scala.sys.process.Process
import scala.io.Source
import scala.xml.{Node, Elem, Text, Atom}
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.TreeCoreAnnotations
import edu.stanford.nlp.trees.TreePrint
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations
import edu.stanford.nlp.semgraph.SemanticGraph
import edu.stanford.nlp.trees.GrammaticalRelation

import edu.stanford.nlp.{pipeline => core}

import jigg.util.SecpressionUtil

class StanfordCoreNLPAnnotator(
  override val name: String,
  override val props: Properties) extends Annotator {

  @Prop(gloss = "Regular expression to segment lines (if omitted, specified method is used)") var pattern = ""
  @Prop(gloss = "Use predefined segment pattern newLine|point|pointAndNewLine") var method = "pointAndNewLine"
  readProps()

  val annotators: Seq[core.Annotator] = Seq()
  val annotatorNames: Seq[String] = Seq()

  var Operator2Id: (String)=> Int ={
    case "ssplit" => 1
    case "tokenize" => 2
    case "pos" | "ner" | "lemma" | "parse" | "dcoref" | "sentiment" => 3
    case _ => -1
  }

  var name_option = name.split(":")
  if( name_option.length < 2){
    sf_option  = "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment"
  }else{
    sf_option = name_option(1).replace(";",", ")
  }

  for( op <- sf_option.split(", ")){
      var id = Operator2Id(op)
      if (id < 0){
        var class_name = name_option(0)
        argumentError("annotators", s"Unnow opiton $op in class: $class_name")
      }else{
        if( id > max_option) max_option = id
        if( id < min_option) min_option = id
      }
    }

  val annotator_name = "corenlp"
  val splitRegex = pattern match {
    case "" =>
      method match {
        case "newLine" => RegexSentenceAnnotator.newLine
        case "point" => RegexSentenceAnnotator.point
        case "pointAndNewLine" => RegexSentenceAnnotator.pointAndNewLine
        case other => argumentError("method")
      }

    case pattern =>
      pattern.r
  }

  private[this] val sentenceIDGen = jigg.util.IDGenerator("s")


  override def annotate(node: Node): Node = {
    my_node = node

    XMLUtil.replaceAll(node, "document") { e =>
      // val sentence_node
      var nlp_res = runCoreNLPAnnotation(min_option)
      var sf_nodes = nlp_res.get(classOf[CoreAnnotations.SentencesAnnotation])
      val sentences =(0 until sf_nodes.size ).sliding(1) flatMap { case Seq(x) =>
        mkSentenceNode(sf_nodes.get(x))
      }
      val textRemoved = XMLUtil.removeText(e)
      XMLUtil.addChild(textRemoved, <sentences>{ sentences }</sentences>)
    }
 }

  def mkSentenceNode(sentence_map:CoreMap): Option[Elem] ={
    val sentence_text = sentence_map.get(classOf[CoreAnnotations.TextAnnotation])
    var sid = sentenceIDGen.next
    var token_nord: Elem = null
    var parse_nord: Node = null
    var dependencies_nord1: Node = null
    var dependencies_nord2: Node = null
    var dependencies_nord3: Node = null

    val token_maps = sentence_map.get(classOf[CoreAnnotations.TokensAnnotation])
    if( token_maps != null){
      var tokens = (0 until token_maps.size).sliding(1) flatMap { case Seq(x) =>
        mkTokenNode(token_maps.get(x),tid(sid,x))
      }
      token_nord = <tokens>{tokens}</tokens>
    }

    val parse_tree = sentence_map.get(classOf[TreeCoreAnnotations.TreeAnnotation])
    if( parse_tree != null && token_nord != null ){
       parse_nord = mkParseNode(parse_tree,token_nord,sid)
    }

    val dependencies_maps = sentence_map.get(classOf[SemanticGraphCoreAnnotations.BasicDependenciesAnnotation])
    if(dependencies_maps != null && token_maps != null){
      dependencies_nord1 = mkDependenciesNode(sentence_map.get(classOf[SemanticGraphCoreAnnotations.BasicDependenciesAnnotation]),"basic-dependencies",token_maps)
      dependencies_nord2 = mkDependenciesNode(sentence_map.get(classOf[SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation]),"collapsed-dependencies",token_maps)
      dependencies_nord3 = mkDependenciesNode(sentence_map.get(classOf[SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation]),"collapsed-ccprocessed-dependenciess",token_maps)

    }

    Option(<sentence id={ sid }>{ sentence_text }{token_nord}{parse_nord}{dependencies_nord1}{dependencies_nord2 }{dependencies_nord3 }</sentence>)
  }

  def mkDependenciesNode(graph:SemanticGraph ,type_name:String, tokens:java.util.List[CoreLabel]): Node = {
    val ans = ""
    var depInfo:Node = <dependencies type={type_name} annotators={annotator_name}></dependencies>
    for( root <-graph.getRoots()){
      val rel:String  = GrammaticalRelation.ROOT.getLongName().replaceAll("\\s+", "")
      var source:Int = 0
      var target:Int = root.index()
      val sourceWord:String = "ROOT"
      val targetWord:String = tokens.get(target - 1).word();
      val isExtra:Boolean = false;
      depInfo = addDependencyInfo(depInfo, rel, isExtra, source, sourceWord,-1, target, targetWord, -1)
    }
    for( edge <- graph.edgeListSorted()){
      val rel:String = edge.getRelation().toString().replaceAll("\\s+", "")
      val source:Int = edge.getSource().index()
      val target:Int = edge.getTarget().index();
      val sourceWord:String = tokens.get(source - 1).word();
      val targetWord:String = tokens.get(target - 1).word();
      var sourceCopy:Int = edge.getSource().copyCount();
      var targetCopy:Int = edge.getTarget().copyCount();
      val isExtra:Boolean = edge.isExtra();
      depInfo = addDependencyInfo(depInfo, rel, isExtra, source, sourceWord,target, target, targetWord, -targetCopy)
    }
     depInfo
  }

  def addDependencyInfo(depInfo:Node , rel:String , isExtra:Boolean , source:Int , sourceWord:String,  sourceCopy:Int, target:Int, targetWord:String, targetCopy:Int):Node = {
    var depElem:Node = null

    if( isExtra) { depElem = <dep unit={rel} extra={"true"}></dep> }
    else { depElem = <dep type={rel}></dep>}

    var govElem:Elem = null
    var strNum:String = "dd"
    if( sourceCopy > 0) { govElem = <governor idx = {source.toString} copy = {sourceCopy.toString}> {sourceWord} </governor>}
    else {govElem = <governor idx={source.toString} >{sourceWord}</governor>}
    XMLUtil.addChild(depInfo, XMLUtil.addChild(depElem,govElem))
  }

  def mkParseNode(parse:Tree,tokens:Elem,sid:String): Node ={
    var treeStrWriter:StringWriter = new StringWriter()
    var constituentTreePrinter:TreePrint = sf_options.constituentTreePrinter
    constituentTreePrinter.printTree(parse, new PrintWriter(treeStrWriter, true))
    var parse_Sexp:String = treeStrWriter.toString()
    var tmp:Elem = <parse annotators={annotator_name}></parse>
    SecpressionUtil.exportXML(parse_Sexp,tokens,tmp,sid)

  }

  def mkTokenNode(token:CoreMap,tkid:String): Option[Elem] ={

    var word = token.get(classOf[CoreAnnotations.TextAnnotation])
    var lemma= token.get(classOf[CoreAnnotations.LemmaAnnotation])

    var pos =  token.get(classOf[CoreAnnotations.PartOfSpeechAnnotation])
    var ner =  token.get(classOf[CoreAnnotations.NamedEntityTagAnnotation])
    var Normalizedner = token.get(classOf[CoreAnnotations.NormalizedNamedEntityTagAnnotation])
    var Speaker =token.get(classOf[CoreAnnotations.SpeakerAnnotation])

    Option(<token id= {tkid} form = {word}  lemma = {lemma} pos = {pos} Normalizedner = {Normalizedner} Speaker={Speaker} />)


  }

  def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex

  var my_node: Node = null
  var min_option:Int = 9999
  var max_option:Int = -1
  var sf_option : String = ""
  var sf_pipline:StanfordCoreNLP = null
  var sf_options:AnnotationOutputter.Options = null

  def runCoreNLP1: Annotation = {
    var text = my_node.text

    var sf_props = new Properties()
    sf_props.put("annotators", sf_option)
    sf_pipline = new StanfordCoreNLP(sf_props)
    sf_options = AnnotationOutputter.getOptions(sf_pipline)
    var annotation= new Annotation(text)

    sf_pipline.annotate(annotation)

    /*coreNLPのxml出力 ここから*/
    //var out_byte = new ByteArrayOutputStream
    //var out_stream =  new PrintWriter(out_byte)
    //sf_pipline.xmlPrint(annotation,  out_stream )
    //rintln(out_byte.toString)
    /*coreNLPのxml出力 ここまで*/

    annotation
  }

  var runCoreNLPAnnotation: (Int)=> Annotation  = {
    case 1 => runCoreNLP1
    case 2 => runCoreNLP1
    case 3 => runCoreNLP1
  }

  override def checkRequirements(satisfiedSoFar: RequirementSet): RequirementSet =
    (0 until annotators.size).foldLeft(satisfiedSoFar) { (sofar, i) =>
      val requires = requiresByEach(i)
      sofar.lackedIn(requires) match {
        case a if a.isEmpty =>
          sofar | requirementsSatisfiedByEach(i)
        case lacked =>
          throw new RequirementError("annotator %s in %s requires %s"
            .format(annotatorNames(i), name, lacked.mkString(", ")))
      }
    }

  override def requires = requiresByEach(0)
  override def requirementsSatisfied =
    requirementsSatisfiedByEach.foldLeft(Set[Requirement]())(_ ++ _)

  private val requiresByEach: Seq[Set[Requirement]] =
    convRequirements(annotators map (_.requires.asScala.toSet))
  private val requirementsSatisfiedByEach: Seq[Set[Requirement]] =
    convRequirements(annotators map (_.requirementsSatisfied.asScala.toSet))

  private def convRequirements(seq: Seq[Set[core.Annotator.Requirement]]):
      Seq[Set[Requirement]] = {

    def conv(set: Set[core.Annotator.Requirement], name: String): Set[Requirement] =
      set map (StanfordCoreNLPAnnotator.requirementMap.getOrElse(_,
        throw new ArgumentError("$name in Stanford CoreNLP is unsupported in jigg.")))

    assert(seq.size == annotatorNames.size)
    (0 until seq.size) map { i => conv(seq(i), annotatorNames(i)) }
  }

}

object StanfordCoreNLPAnnotator extends AnnotatorCompanion[StanfordCoreNLPAnnotator] {

  val requirementMap: Map[core.Annotator.Requirement, Requirement] = Map(
    core.Annotator.TOKENIZE_REQUIREMENT -> Requirement.Tokenize,
    // core.Annotator.CLEAN_XML_REQUIREMENT -> Requirement. // unsupported
    core.Annotator.SSPLIT_REQUIREMENT -> Requirement.Ssplit,
    core.Annotator.POS_REQUIREMENT -> Requirement.POS,
    core.Annotator.LEMMA_REQUIREMENT -> Requirement.Lemma,
    core.Annotator.NER_REQUIREMENT -> Requirement.NER,
    // core.Annotator.GENDER_REQUIREMENT -> // unsupported
    // core.Annotator.TRUECASE_REQUIREMENT -> // unsupported
    core.Annotator.PARSE_REQUIREMENT -> Requirement.Parse,
    core.Annotator.DEPENDENCY_REQUIREMENT -> Requirement.Dependencies,
    // core.Annotator.MENTION_REQUIREMENT -> // unsupported
    // core.Annotator.ENTITY_MENTIONS_REQUIREMENT -> // unsupported
    core.Annotator.COREF_REQUIREMENT -> Requirement.Coreference // TODO: maybe we need CoreNLP specific Coreference? for e.g., representing Gender
  )

  println((new core.POSTaggerAnnotator()).requires.asScala.toSeq(0))
  println(requirementMap get (new core.POSTaggerAnnotator()).requires.asScala.toSeq(0))
}
