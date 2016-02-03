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
import scala.xml.{Node, Elem}
import scala.reflect.ClassTag
import scala.collection.JavaConverters._
import jigg.util.XMLUtil

trait Annotator extends PropsHolder {

  def name: String = this.getClass.getSimpleName

  override final def prefix = name // prefix is required in PropsHolder; for Annotators, it corresponds to its name.

  def props: Properties = new Properties

  final def prop(key: String): Option[String] = jigg.util.PropertiesUtil.findProperty(name + "." + key, props)

  def annotate(annotation: Node): Node

  def init = {} // Called before starting annotation

  def close = {} // Resource release etc; detault: do nothing

  def buildCommand(cmd: String, args: String*): java.util.List[String] = (cmd.split("\\s+") ++ args).toSeq.asJava

  /** A useful method to start process of external command with an error messages (pointing to
    * the homepage of the software), which is thrown when the process is failed to be launched.
    */
  def startExternalProcess(cmd: String, args: Seq[String], software_url: String): Process =
    try new ProcessBuilder(buildCommand(cmd, args:_*)).start
    catch { case e: IOException =>
      val commandName = makeFullName("command")
      val errorMsg = s"""ERROR: Failed to start $name. Check environment variable PATH.
  You can get $prefix at ${software_url}.
  If you have $prefix out of your PATH, set ${commandName} option as follows:
    -${commandName} /PATH/TO/${prefix.toUpperCase}/$prefix
"""

      argumentError("command", errorMsg)
    }

  def requires = Set.empty[Requirement]
  def requirementsSatisfied = Set.empty[Requirement]
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
      // TODO: sentence level parallelization should be handled here?
      val newChild = e.child map { c =>
        assert(c.label == "sentence") // assuming sentences node has only sentence nodes as children
        val a = newSentenceAnnotation(c)
        a
      }
      e.copy(child = newChild)
    }
  }

  def newSentenceAnnotation(sentence: Node): Node
}

/** A useful trait for annotator communicating with an external software, e.g., mecab.
  *
  * This trait introduces variable `communicator`, which is ExternalCommunicator,
  * equipped with many utilities for processing with external software.
  *
  * When implementing this trait, make sure the following criteria are satisfied
  * (see MecabAnnotator, for example):
  *
  *  1) `command` property is defined with @Prop;
  *  2) When the software has a homepage, `softwareUrl` points to that URL;
  *  3) Most importantly, `communicator` is instantiated in constructor
  *     *after* readProps() is called.
  *
  * Some software may require appropriate preprocessing before starting communication.
  * For example, a software may output some messages first, which should be discarded
  * before sentence processing. `checkStartError` can be used for this preprocessing.
  * This may also be used for some error check to ensure that software is successfully
  * launched.
  */
trait AnnotatorWithExternalProcess extends Annotator {

  def command: String
  def defaultArgs: Seq[String] = Seq()
  def softwareUrl: String = ""

  // this is expected to be initialized in the annotator class
  def communicator: ExternalCommunicator

  class ExternalCommunicator {

    val process: Process = startExternalProcess()

    val processIn = new BufferedReader(new InputStreamReader(process.getInputStream, "UTF-8"))
    val processOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream, "UTF-8"))

    checkStartError()

    def write(line: String) = processOut.write(line)
    def writeln(line: String) = { processOut.write(line); processOut.newLine() }

    def closeResource() = {
      processIn.close()
      processOut.close()
      process.destroy()
    }

    /** Write to output stream safely.
      * Throw [[jigg.pipeline.PropsHolder.argumentError]] if IOException occurs.
      */
    def safeWrite(f: =>Unit): Unit = {
      try {
        f
        processOut.flush()
      } catch {
        case e: IOException =>
          // Failing to write to output stream means that the program caused some problem
          // (probably output stream is closed).
          //
          // When this occurs, throw argumentError. But before that,
          // we check whether the process has exited. If so, we read
          // the remaining output in the input buffer and output that.
          // If the process still exists, we may not be able to read
          // the remaining output, so we don't try.

          def remainingMessage(): String = if (isExited) readAll().mkString("\n") else ""
          val errorMsg = s"""ERROR: Problem occurs in $name.
${remainingMessage()}
"""
          argumentError("command", errorMsg)
      }
    }

    /** Read all output untill null is detected (no error check).
      */
    def readAll(): Iterator[String] =
      Iterator.continually(processIn.readLine()).takeWhile(_ != null)

    /** Read until a line matching `matchLast` is detected.
      * Throw [[jigg.pipeline.PropsHolder.argumentError]] if null is detected.
      */
    def readOrErrorForNull(matchLast: String=>Boolean): Iterator[String] =
      readUnlessNull(matchLast)(argumentErrorWithOutput)

    /** Check whether the first line matches `matchFirst`, and then
      * behave the same way as `readOrErrorForNull`
      */
    def readWithFirstLineCheck(
      matchFirst: String=>Boolean, matchLast: String=>Boolean): Iterator[String] = {
      val firstLine = processIn.readLine()
      val iter = Iterator(firstLine) ++ Iterator.continually(processIn.readLine())
      if (matchFirst(firstLine)) readUnlessNull(iter, matchLast)(argumentErrorWithOutput)
      else {
        argumentErrorWithOutput(iter)
      }
    }

    private val argumentErrorWithOutput: Iterator[String]=>Nothing = { iter =>
      val msg = iter.takeWhile(_ != null).mkString("\n")
      argumentError("command", s"Error: Something wrong in $name?\n" + msg)
    }

    /** Read until a line matching `matchLast` is detected.
      * Error handling for null line can be customized with `doForNull`.
      */
    def readUnlessNull(matchLast: String=>Boolean)
      (doForNull: Iterator[String]=>Nothing): Iterator[String] =
      readUnlessNull(Iterator.continually(processIn.readLine()), matchLast)(doForNull)

    /** Read iterator `iter` until a line matching `matchLast` is detected.
      * Error handling for null line can be customized with `doForNull`.
      */
    def readUnlessNull(iter: Iterator[String], matchLast: String=>Boolean)
      (doForNull: Iterator[String]=>Nothing): Iterator[String]= {

      iter.takeWhile {
        case null =>
          doForNull(iter)
        case l => !matchLast(l)
      }
    }

    def isExited =
      try { process.exitValue; true }
      catch { case e: IllegalThreadStateException => false }

    protected def checkStartError() = {}

    /** A useful method to start process of external command with an error messages (pointing to
      * the homepage of the software), which is thrown when the process is failed to be launched.
      */
    private def startExternalProcess(): Process = {
      val commandName = makeFullName("command")
      val _process =
        try new ProcessBuilder(buildCommand(command, defaultArgs:_*)).start
        catch { case e: IOException =>
          val errorMsg = s"""ERROR: Failed to start $name. Check environment variable PATH.
  You can get $prefix at ${softwareUrl}.
  If you have $prefix out of your PATH, set ${commandName} option as follows:
    -${commandName} /path/to/$prefix
"""
          argumentError("command", errorMsg)
        }
      _process
    }

    private def buildCommand(cmd: String, args: String*): java.util.List[String] =
      (cmd.split("\\s+") ++ args).toSeq.asJava
  }
}
