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

import java.io._
import java.lang.{Process, ProcessBuilder}
import java.util.Properties
import java.util.concurrent.LinkedBlockingQueue
import scala.xml.{Node, Elem}
import scala.reflect.ClassTag
import scala.collection.GenSeq
import scala.collection.JavaConverters._
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.forkjoin.ForkJoinPool
import jigg.util.{XMLUtil, PropertiesUtil}

trait Annotator extends PropsHolder {

  def name: String = this.getClass.getSimpleName

  override final def prefix = name // prefix is required in PropsHolder; for Annotators, it corresponds to its name.

  def props: Properties = new Properties

  final def prop(key: String): Option[String] = PropertiesUtil.findProperty(name + "." + key, props)

  /** This method access to the global property of nThreads to get the number of threads.
    *
    * The value is used if an annotator is SentenceAnnotator or DocumentAnnotator.
    * One may customize this in an annotator subclass; for example, the following setting
    * prevents parallel annotation, which may be necessary for an annotator, which is not
    * thread-safe.
    *
    * {{{
    * override val nThreads = 1
    * }}}
    *
    */
  def nThreads: Int = PropertiesUtil.findProperty("nThreads", props).map(_.toInt) match {
    case Some(n) if n > 0 => n
    case _ => collection.parallel.availableProcessors
  }

  def annotate(annotation: Node): Node

  def init = {} // Called before starting annotation

  def close() = {} // Resource release etc; detault: do nothing

  def buildCommand(cmd: String, args: String*): java.util.List[String] = (cmd.split("\\s+") ++ args).toSeq.asJava

  def requires = Set.empty[Requirement]
  def requirementsSatisfied = Set.empty[Requirement]
}

object Annotator {

  def makePar[Datum](data: Seq[Datum], nThreads: Int): GenSeq[Datum] = {
    assert(nThreads > 0)
    nThreads match {
      case 1 => data
      case n =>
        val xx = data.par
        xx.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(n))
        xx
    }
  }
}

/** This is the base trait for all companion object of each Annotator
  */
class AnnotatorCompanion[A<:Annotator](implicit m: ClassTag[A]) {

  /** User can customize how to instantiate an annotator instance from the pipeline if
    *  1) the annotator class has an companion object which inherets AnnotatorCompaion[ThatAnnotator]; and
    *  2) that object overrides fromProps;
    * are satisfied.
    *
    * See MecabAnnotator for example, where fromProps of object MecabAnnotator selects
    * which MecabAnnotator class to invoke, such as IPAMecabAnnotator or JumanMecabAnnotator based on
    * the current dictionary setting of mecab.
    *
    */
  def fromProps(name: String, props: Properties): A =
    m.runtimeClass.getConstructor(classOf[String], classOf[Properties]).newInstance(name, props).asInstanceOf[A]
}

/** A trait for an annotator which modifies a sentence node. If an annotator is sentence-level
  * annotator such as a parser or pos tagger, it should extend this trait and usually what you
  * should do is only to implement newSentenceAnnotation method, which rewrites a sentence
  * node and returns new one.
  */
trait SentencesAnnotator extends Annotator {
  override def annotate(annotation: Node): Node = {

    XMLUtil.replaceAll(annotation, "sentences") { e =>
      val newChild = Annotator.makePar(e.child, nThreads).map { c =>
        assert(c.label == "sentence") // assuming sentences node has only sentence nodes as children
        newSentenceAnnotation(c)
      }.seq
      e.copy(child = newChild)
    }
  }

  def newSentenceAnnotation(sentence: Node): Node
}

/** A trait for an annotator which modifies a document node. Use this trait if an annotator
  * is a document-level annotator.
  */
trait DocumentAnnotator extends Annotator {
  override def annotate(annotation: Node): Node = {

    XMLUtil.replaceAll(annotation, "root") { e =>
      val newChild = Annotator.makePar(e.child, nThreads).map { c =>
        c match {
          case c if c.label == "document" => newDocumentAnnotation(c)
          case c => c
        }
      }.seq
      e.copy(child = newChild)
    }
  }

  def newDocumentAnnotation(sentence: Node): Node
}


/** Provides IO class, which wraps IOCommunicator, and handles errors during communication.
  *
  * In the class, for example, `safeWriteWithFlush(text: String)` wraps the same method in
  * IOCommunicator to throw an appropriate argumentError.
  *
  * TODO: add documentation describing the condition for the annotation under which this
  * trait should be mixed-in.
  * TODO: remove a dependency to `command` property in argumentError.
  *
  * Now it is designed to make it easy to communicate with Japanese NLP softwares such as
  * mecab, cabocha, and KNP.
  *
  */
trait EasyIO extends Annotator {
  // def name: String

  def mkIO(communicator: IOCommunicator): IO = new IO(communicator)

  class IO(val communicator: IOCommunicator) {

    def close() = communicator.closeResource()

    def safeWriteWithFlush(text: String) =
      errorIfFailWriting(communicator.safeWriteWithFlush(text))

    def safeWriteWithFlush(lines: TraversableOnce[String]) =
      errorIfFailWriting(communicator.safeWriteWithFlush(lines))

    def safeWrite(lines: TraversableOnce[String]) =
      errorIfFailWriting(communicator.safeWrite(lines))

    /** Similar to readUntil, but first check whether the first line matches
      * to the predicate in `firstLine`. If not, throw an argumentError.
      */
    def readUntilIf(firstLine: String=>Boolean, lastLine: String=>Boolean) =
      errorIfLeftOutput(communicator.readUntilIf(firstLine, lastLine, _==null))

    /** Reads until lastLine is detected. The matched line will be in in the last
      * index. Throw an argumentError if null line is detected.
      *
      * Assume that the successful last line is something except null, e.g., EOS.
      */
    def readUntil(lastLine: String=>Boolean) =
      errorIfLeftOutput(communicator.readUntil(lastLine, _==null))

    private def errorIfFailWriting(writeResult: Either[Throwable, Unit]): Unit =
      writeResult match {
        case Left(e: IOException) =>
          def remainingMessage = communicator.readAll()
          val errorMsg = s"""ERROR: Problem occurs in $name.
  ${remainingMessage}
"""
          argumentError("command", errorMsg)
        case Left(e) => throw e
        case _ =>
      }

    private def errorIfLeftOutput(
      output: Either[(Seq[String], Iterator[String]), Seq[String]]): Seq[String] =
      output match {
        case Right(results) => results
        case Left((partial, iter)) =>
          val remainingMsg = (partial.dropRight(1).mkString("\n") ++ readRemaining(iter))
          val errorMsg = s"""ERROR: Unexpected output in $name:\n
  ${remainingMsg}
"""
          argumentError("command", errorMsg)
      }
  }

  /** Internal method.
    *
    * What to process the remaining input iterator when the erorr occur?
    * Default: do nothing, because it may not finish when input stream is alive.
    */
  def readRemaining(iter: Iterator[String]): String = ""
}

/** This trait provides `mkIO()` and `mkCommunicator()`, an easy way to instantiate IO
  * object in EasyIO. `mkCommunicator()` is implemented so that it throws an error
  * message pointing to the software URL when failed to launch the process.
  *
  * An assumption for a subclass is that it defines `command` property with @Prop (which
  * overrides `command` in this trait). See MecabAnnotator for example.
  */
trait IOCreator extends EasyIO {

  def command: String

  def defaultArgs = Seq[String]() // these args are always added when calling

  def softwareUrl: String

  def mkIO(): IO = mkIO(mkCommunicator()) // new IO(mkCommunicator())

  def mkCommunicator(): IOCommunicator = new ProcessCommunicator {
    def cmd = command
    def args = defaultArgs
    override def startError(e: Throwable) = {
      val commandName = makeFullName("command")
      val errorMsg = s"""ERROR: Failed to start $name.
  cmd: ${cmd + args.mkString(" ")}

  If the command is not installed, you can get it from ${softwareUrl}.
  You may also customize the way to launch the process by specifying a
  path to the command, e.g.:
    -${commandName} /path/to/$prefix
"""
      argumentError("command", errorMsg)
    }
  }
}

/** Supply `IOQueue` class, which enables thread-safe call of external softwares.
  *
  * The subclass must implement `mkIO()`; easy way is to mix-in IOCreator
  */
trait ParallelIO extends EasyIO {

  def mkIO(): IO

  class IOQueue(size: Int) {
    assert(size > 0)
    val queue = new LinkedBlockingQueue((0 until size).map(_=>mkIO()).asJava)

    def using[A](f: IO=>A): A = {
      val io = queue.poll
      val ret = f(io)
      queue.put(io)
      ret
    }

    def close() = queue.asScala foreach (_.close())
  }
}
