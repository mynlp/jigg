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
import jigg.util.PropertiesUtil
import jigg.util.XMLUtil.RichNode

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

  /** If this annotator can run, returns the set of requirements satisifed with all
    * annotators up to this. Throw requirementError if requirement check is failed.
    */
  def checkRequirements(satisfiedSoFar: RequirementSet): RequirementSet = {
    satisfiedSoFar.lackedIn(requires) match {
      case a if a.isEmpty => satisfiedSoFar | requirementsSatisfied
      case lacked =>
        throw new RequirementError(
          "annotator %s requires %s".format(name, lacked.mkString(", "))
        )
    }
  }

  def requires() = Set.empty[Requirement]
  def requirementsSatisfied() = Set.empty[Requirement]
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

  def annotateError(n: Node, name: String, e: Exception): Node =
    n addChild <error annotator={ name }>{ e + "" }</error>
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

    annotation.replaceAll("sentences") { e =>
      val newChild = Annotator.makePar(e.child, nThreads).map { c =>
        assert(c.label == "sentence") // assuming sentences node has only sentence nodes as children

        try newSentenceAnnotation(c) catch {
          case e: AnnotationError =>
            System.err.println(s"Failed to annotate a sentence by $name.")
            System.err.println(c.text)
            Annotator.annotateError(c, name, e)
        }
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

    annotation.replaceAll("root") { e =>
      val newChild = Annotator.makePar(e.child, nThreads).map { c =>
        c match {
          case c if c.label == "document" =>
            try newDocumentAnnotation(c) catch {
              case e: AnnotationError =>
                System.err.println(s"Failed to annotate a document by $name.")
                Annotator.annotateError(c, name, e)
            }
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
          // Failing to write means that process is dead, so we can safely read remaining
          // inputs. Is this always true?
          val remainingMsg = communicator readAll() mkString "\n"
          throw new ProcessError(remainingMsg)
        case Left(e) => throw e
        case _ =>
      }

    private def errorIfLeftOutput(
      output: Either[(Seq[String], Iterator[String]), Seq[String]]): Seq[String] =
      output match {
        case Right(results) => results
        case Left((partial, iter)) =>
          val remainingMsg =
            partial.dropRight(1).mkString("\n") + readRemaining(iter)
          throw new ProcessError(remainingMsg)
      }
  }

  /** Internal method.
    *
    * What to process the remaining input iterator when the erorr occur?
    * Default: do nothing, because it may not finish when input stream is still alive.
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

  def mkCommunicator(): IOCommunicator = new InstructiveProcessCommunicator

  class InstructiveProcessCommunicator extends ProcessCommunicator {
    def cmd = command
    def args = defaultArgs

    override def startError(e: Throwable) = {
      val commandName = makeFullName("command")
      val msg = s"""
  If the command is not installed, you can get it from ${softwareUrl}.
  You may also customize the way to launch the process by specifying a
  path to the command, e.g.:
    -${commandName} /path/to/$prefix
"""
      launchError(msg)
    }

    override def checkStartError() = for (LaunchTester(i, u, c) <- launchTesters) {
      safeWriteWithFlush(i) match {
        case Left(e) =>
          val msg = readAll() mkString "\n"
          launchError(s"output:\n$msg")
        case _ =>
      }
      readUntil(u) match {
        case Right(results) =>
        case Left((partial, iter)) =>
          val remainingMsg: String =
            partial.dropRight(1).mkString("\n") + readRemaining(iter)
          launchError(s"output:\n$remainingMsg")
      }
    }

    private def launchError(msg: String) = {
      val fullMsg = s"""ERROR: Failed to start $name.
  cmd: ${(cmd +: args) mkString " "}

$msg
"""
      argumentError("command", fullMsg)
    }
  }

  /** Each launch tester is used to check whether the process
    * causes no problem in launching.
    */
  def launchTesters: Seq[LaunchTester] = Seq()

  /** A tuple of:
    *
    * input = example input;
    * until = read until this line is satisfied;
    * okOutput = succeed to launch if the output satisfies this predicate.
    */
  case class LaunchTester(
    input: String,
    until: String=>Boolean,
    okOutput: String=>Boolean)
}

/** Supply `IOQueue` class, which enables thread-safe call of external softwares.
  *
  * The subclass must implement `mkIO()`; easy way is to mix-in IOCreator
  */
@deprecated(message="This class is unstable in some environment. Use ExternalProcessSentencesAnnotator for achieving similar parallelism.", "3.6.2")
trait ParallelIO extends EasyIO with ParallelAnnotator {

  def mkIO(): IO

  class IOQueue(size: Int) extends ResourceQueue(size, mkIO _) {
    /** Check if io is alive, and replace with new one if it is dead.
      *
      * This method may possibly be overwritten to close the current io regardless
      * of the condition. Such device is needed for an annotator, with which it is
      * difficult to judge whether the input stream is completely read when an error
      * occurs. In the currently supported annotators, such as KNP, the final line
      * is always EOS even when an error occurs, so it is guaranteed that no error
      * is remained in the input stream (for the next sentence).
      */
    def postProcess(io: IO, e: ProcessError): Nothing = {
      if (!io.communicator.isAlive) {
        io.close()
        val newIO = mkIO()
        queue.put(newIO)
      } else {
        queue.put(io)
      }
      throw e
    }

    def close() = queue.asScala foreach (_.close())
  }
}

trait ParallelAnnotator {

  abstract class ResourceQueue[A](size: Int, mkResource: ()=>A) {
    assert(size > 0)

    val queue = new LinkedBlockingQueue((0 until size).map(_=>mkResource()).asJava)

    def using[B](f: A=>B): B = {
      val resource = queue.poll
      try {
        val ret = f(resource)
        queue.put(resource)
        ret
      } catch {
        case e: ProcessError =>
          postProcess(resource, e)
      }
    }

    def postProcess(resource: A, e: ProcessError): Nothing
  }
}
