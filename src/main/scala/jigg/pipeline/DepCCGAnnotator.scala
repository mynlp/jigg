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

  @Prop(gloss = "Path to a model (e.g., lstm_parser_elmo_finetune.tar.gz)") var model = ""
  @Prop(gloss = "Additional args sent to depccg (e.g., --gpu 0 --max-length 200)") var args = ""
  @Prop(gloss = "Language (en|ja)") var lang = "en"
  @Prop(gloss = "Outputs k-best derivations if this value > 1") var kBest = 1
  @Prop(gloss = "Path to 'activate' script of the virtual environment that you wish to run on depccg") var venv = ""
  @Prop(gloss = "Name of conda enviroment that you wish to run on depccg (ignored when venv is non-empty.)") var conda = ""
  @Prop(gloss = "If true, launch multiple depccgs for parallel parsing. See -help depccg for more details.") var parallel = false
  readProps()

  override def nThreads = if (parallel) super.nThreads else 1

  override def description = s"""${super.description}

  A wrapper for depccg (https://github.com/masashi-y/depccg). Currently the supported
  version is v.1. If you use prior versions, please update to the latest one, which
  can be installed with `pip`:

    > pip install cython numpy depccg

  Then, download the models appropriately:

    > depccg_en download  # for English parsing
    > depccg_ja download  # for Japanese parsing

  These models are loaded in default. If you want to use another model, such as a model
  with ELMo, please specify it in -${name}.model option.

  Or, this wrapper supports most arguments defined for `depccg_en` and `depccg_ja`
  commands, such as `--gpu n` for gpu specification, `--root-cats`, etc. These
  arguments can be provided with -${name}.args. For example, by giving
  -${name}.args "--gpu 0 --root-cats S", depccg internally runs on GPU 0, and the model
  only search for a tree rooted by S. The following depccg arguments are supported:

    --config, --model, --weights, --gpu, --batchsize, --nbest, --root-cats
    --unary-penalty, --beta, --pruning-size, --disable-beta,
    --disable-category-dictionary, --disable-seen-rules, --max-length, --max-steps

  See `depccg_en --help` for descrption of each. Note that --model and --nbest can also
  be specified by -${name}.model and -${name}.kBest. If the latter options are provided
  --model and --nbest arguments are ignored internally (-${name}.model and
  -${name}.kBest are prioritized).

  virtualenv or conda
  --------------------
  If your depccg is installed in a specific virtualenv, you can specify that in
  -${name}.venv. For example, if your depccg-specific virtualenv is in
  `~/venvs/depccg`, supply `-${name}.venv ~/venvs/depccg/bin/activate`. Note that
  you should point to the path to `activate` script found on `bin`.

  Or if you use conda enviroment, please setup instead -${name}.conda option. For example,
  if install depccg on `depccg` enviroment, add `-${name}.conda depccg` option. Note
  that this option is ignored (overwitten) when you set ${name}.venv otpion.

  Note on concurrency
  --------------------
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
    System.err.println(s"Loading depccg... (${nThreads} instances)")
    localAnnotators
    System.err.println("done.")
  }

  def mkLocalAnnotator = new LocalDepCCGAnnotator

  class LocalDepCCGAnnotator extends LocalAnnotator with IOCreator {

    // Avoid reading resource at test time.
    lazy val script: File = ResourceUtil.readPython("_depccg.py")
    def command = {
      val venvcommand = (conda, venv) match {
        case (c, "") if c.size > 0 => s"conda activate ${c} && "
        case (_, v) if v.size > 0 => s"source ${venv} && "
        case _ => ""
      }
      val options = (s"--lang $lang "
        + (if (model != "") s"--internal-model $model " else "")
        + (if (kBest != 1) s"--internal-nbest $kBest " else ""))

      venvcommand + s"python ${script.getPath} ${args} ${options}"
    }

    def mkScript(): File = {
      val script = File.createTempFile("_depccg", ".py")
      script.deleteOnExit
      val stream = getClass.getResourceAsStream("/python/_depccg.py")
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
      println("input to depccg: " + input)
      val result = runDepccg(input)

      // result is given by candc-style xml
      // First line is parser-internal error msg (1..), which should be ignored.
      val resultNode = XML.loadString(result.drop(1).mkString("\n"))

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
