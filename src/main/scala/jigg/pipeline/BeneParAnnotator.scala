package jigg.pipeline

/*
 Copyright 2013-2018 Hiroshi Noji

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

import java.io.File
import java.util.Properties

import scala.xml._
import scala.sys.process.Process

import jigg.util.IOUtil
import jigg.util.PropertiesUtil
import jigg.util.ResourceUtil
import jigg.util.TreesUtil
import jigg.util.XMLUtil.RichNode

class BeneParAnnotator(override val name: String, override val props: Properties)
    extends AnnotatingSentencesInParallel {

  @Prop(gloss = "Path to 'activate' script of the virtual environment that you wish to run on depccg") var venv = ""
  @Prop(gloss = "Model name (should be installed)") var model = "benepar_en2"
  @Prop(gloss = "If true, launch multiple instances of the parsers for sentence-level parallel parsing") var parallel = false
  readProps()

  override def nThreads = if (parallel) super.nThreads else 1

  override def description = s"""${super.description}

  This is a state-of-the-art neural constituency parser with self-attention:
    https://github.com/nikitakit/self-attentive-parser,
  which achieves 95.07 F1 on the standard English PTB test set.

  To use this annotator, we assume the parser is installed on the system via pip
  (copy from the original README; please follow the latest original instructions):
    $$ pip install cython numpy
    $$ pip install benepar[cpu]

  You also need to download the model:
    $$ python
    >>> import benepar
    >>> benepar.download('benepar_en2')

  Although not tested, this annotator should also work with the GPU-version of the parser,
  "benepar[gpu]". If you prefer to use a different model, specify "-${name}.model", after
  downloading it.

  Is recommended to install "benepar" in some virtualenv. If so, please specify the path
  to that virtualenv, by setting "-${name}.venv" option. For example, if the virtual env
  is installed on `~/.venvs/benepar`, give "-${name}.venv ~/.venvs/benepar/bin/activate".

  This parser does not perform POS tagging internally. So "pos" annotation should be performed
  beforehand. An example of the pipeline is thus:
    "-annotators corenlp[tokenize,ssplit,pos],beneparser",
  which builds a parse based on the POS sequence given by the Stanford (CoreNLP) tagger.

  Parallelism:
    Since a model is relatively heavy, in default, benepar does not perform parallel parsing.
    Set "-${name}.parallel" to enable parallel processing, but it tries to launch more than
    one benepar instances, following the global setting of "-nThreads" option. You can also
    customize the number of instances for this annotator, by setting "-${name}.nThreads".
    For example, to use two instances, set "-${name} parallel -${name}.nThreads 2".
"""

  override def init() = {
    System.err.print(s"Loading benepar... (${nThreads} instances)")
    localAnnotators
    System.err.println(" done.")
  }

  def mkLocalAnnotator = new LocalBeneParAnnotator

  class LocalBeneParAnnotator extends SentencesAnnotator with LocalAnnotator with IOCreator {

    // Avoid reading resource at test time.
    lazy val script: File = ResourceUtil.readPython("bene_par.py")
    def command = {
      val venvcommand = if (venv == "") "" else s"source ${venv} && "
      venvcommand + s"python ${script.getPath} ${model}"
    }

    def mkScript(): File = {
      val script = File.createTempFile("bene_par", ".py")
      script.deleteOnExit
      val stream = getClass.getResourceAsStream("/python/bene_par.py")
      IOUtil.writing(script.getPath) { o =>
        scala.io.Source.fromInputStream(stream).getLines foreach { line =>
          o.write(line + "\n")
        }
      }
      script
    }

    override def launchTesters = Seq(
      LaunchTester("a\nNN", _ == "END", _ == "END"))

    def softwareUrl = "https://github.com/nikitakit/self-attentive-parser"

    val benepar = mkIO()
    override def close() = benepar.close()

    def newSentenceAnnotation(sentence: Node): Node = {
      val tokens: NodeSeq = (sentence \ "tokens").head \ ("token")
      val words = tokens map (_ \@ "form")
      val postags = tokens map (_ \@ "pos")

      val tree = runParser(mkInput(sentence)).mkString("")
      val node = TreesUtil.streeToNode(tree, sentence, name)

      sentence addChild node
    }

    def mkInput(sentence: Node): String = {
      val tokens = (sentence \ "tokens").head \ ("token")
      val words = tokens map (_ \@ "form")
      val postags = tokens map (_ \@ "pos")

      words.mkString(" ") + "\n" + postags.mkString(" ")
    }

    def runParser(input: String): Seq[String] = {
      benepar.safeWriteWithFlush(input)
      benepar.readUntil(_ == "END").dropRight(1)
    }
  }

  override def requires = Set(Requirement.Ssplit, Requirement.Tokenize, Requirement.POS)
  override def requirementsSatisfied = Set(Requirement.Parse)
}
