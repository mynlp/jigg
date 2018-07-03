package jigg.pipeline

/*
 Copyright 2013-2017 Hiroshi Noji

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
import jigg.util.XMLUtil.RichNode

class DepCCGAnnotator(override val name: String, override val props: Properties)
    extends AnnotatingSentencesInParallel { self =>

  // @Prop(gloss = "Path to run.py", required = true) var path = ""
  @Prop(gloss = "Path to src/ dir of depccg, where depccg.so exists", required = true) var srcdir = ""
  @Prop(gloss = "Path to the model (e.g., tri_headfirst directory)", required = true) var model = ""
  @Prop(gloss = "Language (en|ja)") var lang = "en"
  @Prop(gloss = "Outputs k-best derivations if this value > 1") var kBest = 1
  @Prop(gloss = "Path to 'activate' script of the virtual environment that you wish to run on depccg") var venv = ""
  @Prop(gloss = "If true, launch multiple depccgs for parallel parsing. See -help depccg for more details.") var parallel = false
  readProps()

  override def nThreads = if (parallel) super.nThreads else 1

  override def description = s"""${super.description}

  A wrapper for depccg (https://github.com/masashi-y/depccg). -${name}.path (path to the
  main script) and ${name}.model (path to the model directory) are two necessary
  arguments.

  If your depccg should be run in specific virtualenv, you can specify that in
  -${name}.venv. For example, if your depccg-specific virtualenv is in
  `~/venvs/depccg`, supply `-${name}.venv ~/venvs/depccg/bin/activate`. Note that
  you should point to the path to `activate` script found on `bin`.

  One complication is that DepCCG originally supports parallel parsing in itself but it
  depends on how a user compiles the code. Speficially, DepCCG can be run in parallel
  only when it is compiled with OpenMP.

  The option -${name}.parallel is for alleviating this complication. If it is false
  (default), Jigg does launch only single instance of depccg, which means parallelism
  is completely delegated to depccg itself.

  With "-${name}.parallel true", parallelism is managed at Jigg side. This will launches
  many different depccg instances, and distribute the inputs into them. We assume this
  option will only be used when depccg is built without OpenMP. Since this option has
  many overheads in particular longer model loading and large memory consumption, we
  recommend to use "OpenMPed" depccg with "-${name}.parallel false".

"""

  override def init() = {
    checkArgument()
    System.err.println(s"Loading depccg... (${nThreads} instances)")
    localAnnotators
    System.err.println("done.")
  }

  def checkArgument() = {
    val src = new File(srcdir)
    if (!src.isDirectory || !src.listFiles.exists(_.getPath.endsWith(".so")))
      argumentError("srcdir",
        s"Something wrong with -${name}.srcdir, which should contains a *.so file.")

    if (!new File(model).isDirectory || !new File(model, "tagger_model").exists)
      argumentError("model", s"-${name}.model seems incorrect. That should points to a directory containing tagger_model, cat_dict.txt, etc.")
  }

  def mkLocalAnnotator = new LocalDepCCGAnnotator

  class LocalDepCCGAnnotator extends LocalAnnotator with IOCreator {

    // Avoid reading resource at test time.
    lazy val script: File = ResourceUtil.readPython("depccg.py")
    def command = {
      val venvcommand = if (venv == "") "" else s"source ${venv} && "
      venvcommand + s"python ${script.getPath} ${srcdir} ${model} ${kBest} ${lang}"
    }

    def mkScript(): File = {
      val script = File.createTempFile("depccg", ".py")
      script.deleteOnExit
      val stream = getClass.getResourceAsStream("/python/depccg.py")
      IOUtil.writing(script.getPath) { o =>
        scala.io.Source.fromInputStream(stream).getLines foreach { line =>
          o.write(line + "\n")
        }
      }
      script
    }

    override def launchTesters = Seq(
      LaunchTester("a\n####EOD####", _ == "END", _ == "END"))
    def softwareUrl = "https://github.com/masashi-y/depccg"

    val depccg = mkIO()
    override def close() = depccg.close()

    override def annotate(annotation: Node) = {
      assert(annotation.label == "sentences")

      val sentences = annotation.child

      val input = sentences.map(mkInput).mkString("\n") + "\n####EOD####"
      val result = runDepccg(input)

      // result is given by candc-style xml
      val resultNode = XML.loadString(result.mkString("\n"))

      val outputs = resultNode \\ "ccgs"
      assert(outputs.size == sentences.size)

      val newSentences = sentences zip outputs map {
        case (sentence, ccgs) =>
          val kbest = ccgs \ "ccg"
          kbest.foldLeft(sentence) { (current, ccg) =>
            CandCAnnotator.annotateCCGSpans(current, ccg, name)
          }
      }

      annotation.asInstanceOf[Elem].copy(child = newSentences)
    }

    // Input looks like "This|X|X is|X|X ..."
    def mkInput(sentence: Node): String = {
      val forms = (sentence \\ "token") map (_ \@ "form")
      forms mkString " "
    }

    def runDepccg(input: String): Seq[String] = {
      depccg.safeWriteWithFlush(input)
      depccg.readUntil(_ == "END").dropRight(1)
    }
  }

  override def requires = lang match {
    case "en" => Set(Requirement.Ssplit, Requirement.Tokenize)
    case "ja" => Set(Requirement.Ssplit, JaRequirement.TokenizeWithIPA)
  }

  override def requirementsSatisfied = Set(Requirement.CCGDerivation)
}
