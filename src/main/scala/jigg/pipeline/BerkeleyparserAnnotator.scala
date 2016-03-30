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


import jigg.util.PropertiesUtil
import jigg.util.XMLUtil
import java.util.ArrayList

import java.util.Properties
import scala.xml._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

import edu.berkeley.nlp.PCFGLA
import edu.berkeley.nlp.PCFGLA.BerkeleyParser
import edu.berkeley.nlp.PCFGLA.BerkeleyParser.Options

import javax.imageio.ImageIO
import javax.swing.JFrame

import edu.berkeley.nlp.PCFGLA._
import edu.berkeley.nlp.io.PTBLineLexer
import edu.berkeley.nlp.syntax.Tree
import edu.berkeley.nlp.ui.TreeJPanel
import edu.berkeley.nlp.util.Numberer

import jigg.util.SecpressionUtil


class BerkeleyparserAnnotator(override val name: String, override val props: Properties) extends SentencesAnnotator{

  //override val nThread = 1

  val name_data = name.split(":")
  var gr_file:String = ""
  if( name_data.size>1) gr_file = name_data(1)
    else gr_file = "eng_sm6.gr"

  var bp = new BerkeleyParser()
    var bpo = new BerkeleyParser.Options()
    bpo.grFileName = gr_file
    bpo.goldPOS = false;
    //bpo.goldPOS = true

    var pData = ParserData.Load(bpo.grFileName )
    if( pData == null){
      argumentError("annotators", s"Failed to load grammar from file")
    }
    var grammar = pData.getGrammar();
    var lexicon = pData.getLexicon();
    Numberer.setNumberers(pData.getNumbs());
    val threshold = 1.0
    val parser = new CoarseToFineMaxRuleParser(grammar, lexicon, threshold, -1, bpo.viterbi, bpo.substates, bpo.scores, bpo.accurate, bpo.variational, true, true)
    var tokenizer = new PTBLineLexer()

  override def newSentenceAnnotation(sentence: Node) = {

    /*val name_data = name.split(":")
    var gr_file:String = ""

    if( name_data.size>1) gr_file = name_data(1)
    else gr_file = "eng_sm6.gr"

    var bp = new BerkeleyParser()
    var bpo = new BerkeleyParser.Options()
    bpo.grFileName = gr_file
    bpo.goldPOS = false;
    //bpo.goldPOS = true

    var pData = ParserData.Load(bpo.grFileName )
    if( pData == null){
      argumentError("annotators", s"Failed to load grammar from file")
    }
    var grammar = pData.getGrammar();
    var lexicon = pData.getLexicon();
    Numberer.setNumberers(pData.getNumbs());
    val threshold = 1.0
    val parser = new CoarseToFineMaxRuleParser(grammar, lexicon, threshold, -1, bpo.viterbi, bpo.substates, bpo.scores, bpo.accurate, bpo.variational, true, true)
    var tokenizer = new PTBLineLexer()*/
    var tags = new ArrayList[String]
    var posTags = new ArrayList[String]


    val sentenceID = (sentence \ "@id").toString
    val tokens = sentence \ "tokens"
    val tokenSeq = tokens \ "token"

    for( tok <- tokenSeq){
       var tid = tok \ "@id"
       var tword = (tok \ "@form").toString
       var tpos = (tok \ "@pos").toString
       tags.add(tword)
       posTags.add(tpos)

    }

    var parsedTrees = new ArrayList[Tree[String]]
 // var parsedTree = parser.getBestConstrainedParse(tags, posTags,null)   //use POS data in tokenize
    var parsedTree = parser.getBestConstrainedParse(tags, null,null)      //Not Use POS data in tokenize

    parsedTrees.add(parsedTree);
    //outputTrees(parsedTrees, outputData, parser, opts, line,sentenceID);

    //println(parsedTrees)
    var TreeStr : String = ""

    for(i <- 0 until parsedTrees.size)
    {
      TreeStr = TreeStr + parsedTrees.get(i)
    }
    var dummy = <dummy>dummy</dummy>
    var tmp:Elem = <parse annotators="Berkeleyparser"></parse>
    var parse_node = SecpressionUtil.exportXML(TreeStr,tokens,tmp,sentenceID)
    XMLUtil.addChild(sentence, parse_node)
  }

  override def requires = Set(Requirement.Tokenize)
  override def requirementsSatisfied = Set(Requirement.Parse)
}

object BerkeleyparserAnnotator extends AnnotatorCompanion[BerkeleyparserAnnotator]
