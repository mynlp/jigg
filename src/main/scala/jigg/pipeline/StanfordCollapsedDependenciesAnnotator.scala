package jigg.pipeline

/*
 Copyright 2013-2016 Hiroshi Noji

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
import scala.collection.JavaConverters._
import scala.xml._

import jigg.util.XMLUtil.RichNode

import edu.stanford.nlp.trees.{
  EnglishGrammaticalStructure,
  UniversalEnglishGrammaticalStructure,
  TypedDependency,
  TreeGraphNode}
import edu.stanford.nlp.ling.{CoreAnnotations, IndexedWord, Word}
import edu.stanford.nlp.semgraph.SemanticGraphFactory

class StanfordCollapsedDependenciesAnnotator(
  override val name: String,
  override val props: Properties) extends SentencesAnnotator {

  @Prop(gloss = "Annotation style for the uncollapsed dependencies (SD|UD). See below in detail.") var style = "SD"

  override def description = s"""${super.description}

  This annotator complements the gaps in the outputs by the Stanford families (e.g.,
  depparse of CoreNLP), which output three kinds of dependency representations including
  basic (tree-structured) as well as collapsed (non tree-structured) dependencies, and
  other third party parsers, which commonly support only the basic, tree-structured
  dependencies.

  Specifically, this annotator supplies Stanford collapsed dependencies, which are not
  usually a rooted tree, and CC-processed collapsed dependencies, based on the basic
  dependencies outputted by other dependency parsers.

  To correctly use this annotator, a user must take care with the dependency annotation
  style of the given basic dependencies, and set the appropriate value in `style`, which
  is `SD` in default.

  For instance, if one wants to supply collapsed dependencies for the output of SyntaxNet,
  in which the current pre-installed model (i.e., Parsey McParseface) outputs the
  Stanford dependencies (SD), the following command may accomplish the goal:

    jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit],syntaxnet,collapseddep"

  since the default value of `style` is SD. But if the parser outputs the dependencies
  in Universal dependencies (UD) format rather than SD, then, `style` must be changed
  as follows:

    jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit],udparser,collapseddep" -collapseddep.style UD

  where "udparser" is some dependency parser outputting UD style dependencies, e.g.,
  a re-trained SyntaxNet parser with the UD-style treebanks.

  Note that there is no way to recover the collapsed dependencies from trees other than
  SD and UD, such as the ordinary CoNLL-style dependencies.
"""

  // This is fixed now, but may be modifiable in future?
  val extraDependencies = false

  def newSentenceAnnotation(sentence: Node) = {
    val SC = StanfordCoreNLPAnnotator

    val typeddeps = SC.extractTypedDependencies(sentence, SC.basicDepType)

    val root = new IndexedWord(new Word("ROOT"))
    root.setIndex(0)
    val rootNode = new TreeGraphNode(root)

    val gs = makeGrammaticalStructure(typeddeps, rootNode)

    val collapsedDeps = SemanticGraphFactory.makeFromTree(
      gs, SemanticGraphFactory.Mode.COLLAPSED, extraDependencies, true, null)

    val ccDeps = SemanticGraphFactory.makeFromTree(
      gs, SemanticGraphFactory.Mode.CCPROCESSED, extraDependencies, true, null)

    val collapsedNode = SC.semanticGraphToDependenciesNode(
      sentence, collapsedDeps, SC.collapsedDepType, name)
    val ccNode = SC.semanticGraphToDependenciesNode(
      sentence, ccDeps, SC.ccCollapsedDepType, name)

    sentence addOrOverwriteChild (Seq(collapsedNode, ccNode), Some("type"))
  }

  private def makeGrammaticalStructure(
    dependencies: Seq[TypedDependency],
    rootNode: TreeGraphNode) = {

    style match {
      case "SD" => new EnglishGrammaticalStructure(dependencies.asJava, rootNode)
      case "UD" => new UniversalEnglishGrammaticalStructure(dependencies.asJava, rootNode)
      case _ => new UniversalEnglishGrammaticalStructure(dependencies.asJava, rootNode)
    }
  }

  override def requires = Set(Requirement.BasicDependencies)
  override def requirementsSatisfied =
    Set(Requirement.CollapsedDependencies, Requirement.CollapsedCCProcessedDependencies)
}

