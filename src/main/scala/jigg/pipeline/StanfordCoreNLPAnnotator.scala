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
import scala.collection.JavaConversions._
import scala.xml._
import scala.sys.process.Process
import scala.io.Source
import scala.xml.{Node, Elem, Text, Atom}
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.util.CoreMap


class StanfordCoreNLPAnnotator(override val name: String, override val props: Properties) extends Annotator {

  @Prop(gloss = "Regular expression to segment lines (if omitted, specified method is used)") var pattern = ""
  @Prop(gloss = "Use predefined segment pattern newLine|point|pointAndNewLine") var method = "pointAndNewLine"
  readProps()

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
    var name_option = name.split(":")
    if( name_option.length < 2){
      sf_option  = "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment"
    }else{
      sf_option = name_option(1).replace(";",", ")
    }
    my_node = node

    if( name_option.length < 2){
      sf_option  = "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment"
    }else{
      sf_option = name_option(1).replace(";",", ")
    }

    sf_option.split(", ")
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
    val token_maps = sentence_map.get(classOf[CoreAnnotations.TokensAnnotation])
    var sid = sentenceIDGen.next
      if( token_maps != null){
        var tokens = (0 until token_maps.size).sliding(1) flatMap { case Seq(x) =>
          mkTokenNode(token_maps.get(x),tid(sid,x))
        }
        Option(<sentence id={ sid }>{ sentence_text }<tokens>{tokens}</tokens></sentence>)
      }else{
          //token情報なし
        Option(<sentence id={ sid }>{ sentence_text }</sentence>)
      }
  }

  def mkTokenNode(token:CoreMap,tkid:String): Option[Elem] ={

    var word = token.get(classOf[CoreAnnotations.TextAnnotation])
    var lemma= token.get(classOf[CoreAnnotations.LemmaAnnotation])

    var pos =  token.get(classOf[CoreAnnotations.PartOfSpeechAnnotation])
    var ner =  token.get(classOf[CoreAnnotations.NamedEntityTagAnnotation])
    var Normalizedner = token.get(classOf[CoreAnnotations.NormalizedNamedEntityTagAnnotation])
    var Speaker =token.get(classOf[CoreAnnotations.SpeakerAnnotation])

    Option(<token id= {tkid} word = {word}  lemma = {lemma} pos = {pos} Normalizedner = {Normalizedner} Speaker={Speaker} dummy={"dummy"} />)


  }

  def tid(sindex: String, tindex: Int) = sindex + "_tok" + tindex

  //override def requires = Set()
  override def requirementsSatisfied = Set(Requirement.CoreNLP)

  var my_node: Node = null
  var min_option:Int = 9999
  var max_option:Int = -1
  var sf_option : String = ""

  var Operator2Id: (String)=> Int ={
    case "ssplit" => 1
    case "tokenize" => 2
    case "pos" | "ner" | "lemma" | "parse" | "dcoref" | "sentiment" => 3
    case _ => -1
  }

  def runCoreNLP1: Annotation = {
    var text = my_node.text

    var sf_props = new Properties()
    sf_props.put("annotators", sf_option)
    var sf_pipline = new StanfordCoreNLP(sf_props)
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

}

object StanfordCoreNLPAnnotator extends AnnotatorCompanion[StanfordCoreNLPAnnotator]

