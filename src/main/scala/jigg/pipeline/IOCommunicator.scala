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


import scala.collection.JavaConverters._
import scala.util.control
import java.lang.Process
import java.io._


/** IOCommunicator abstracts IO communication mechanism, and provides several utility
  * functions.
  *
  * An annotator, which relies on an external command, may have a member of this trait.
  * When unit testing such annotator, we may use an annotator class which overrides
  * a communicator variable to eliminate dependencies for external resource.
  *
  * See MecabAnnotator, for example.
  */
trait IOCommunicator {

  def write(line: String): Unit
  def writeln(line: String): Unit
  def flush(): Unit

  def isAlive: Boolean

  def readingIter: Iterator[String]

  def closeResource() = {}

  /** This is the basic method for writing.
    * If an error is detected, return that on the left of Either.
    */
  def safeWriteWithFlush(lines: TraversableOnce[String]): Either[Throwable, Unit] =
    control.Exception.allCatch either {
      for (line <- lines) writeln(line)
      flush()
    }

  final def safeWriteWithFlush(line: String): Either[Throwable, Unit] =
    safeWriteWithFlush(Seq(line))

  def safeWrite(lines: TraversableOnce[String]): Either[Throwable, Unit] =
    control.Exception.allCatch either { for (line <- lines) writeln(line) }

  /** Call `readUntil` if the first line matches to `firstLine`.
    * Otherwise, return the (unmatched) first line and the remaining input iterator
    * on the left of Either.
    */
  def readUntilIf(
    firstLine: String=>Boolean,
    lastLine: String=>Boolean,
    errorLine: String=>Boolean = _ == null):
      Either[(Seq[String], Iterator[String]), Seq[String]] = {

    val iter = readingIter
    iter.next match {
      case l if firstLine(l) =>
        readUntil(Iterator(l) ++ iter, lastLine, errorLine)
      case l => Left((Seq(l), iter))
    }
  }

  /** Read until a line matching `lastLine` or `errorLine` is detected.
    * Return the sequence of lines until the last line on the right of Either
    * if lastLine is found or stream is empty.
    * Return the sequence of lines and remaining input as an iterator on the
    * left of Either if errorLine is detected or all inputs are read.
    *
    * WARNING: the last element of returned seq is the line matching lastLine
    * or errorLine. This means, for example, if we set _==null to errorLine,
    * the last element is null. Use `dropRight(1)` appropriately if you want
    * to ignore the last element!
    */
  def readUntil(lastLine: String=>Boolean, errorLine: String=>Boolean = _ == null)
      : Either[(Seq[String], Iterator[String]), Seq[String]] =
    readUntil(readingIter, lastLine, errorLine)

  /** Read all output untill null is detected (no error check).
      */
  def readAll(): Seq[String] =
    readingIter.takeWhile(_ != null).toVector

  protected def readUntil(
    iter: Iterator[String],
    lastLine: String=>Boolean,
    errorLine: String=>Boolean): Either[(Seq[String], Iterator[String]), Seq[String]] = {

    def readIter(cond: String=>Boolean): Seq[String] = {
      var last = ""
      val ret = iter.takeWhile { l => last = l; !cond(l) }.toVector
      ret :+ last
    }

    if (iter.isEmpty) Right(Array[String]())
    else {
      val result =  readIter(l => lastLine(l) || errorLine(l)) // = iter.takeWhile { l => lastLine(l) || errorLine(l) }.toVector
      result.last match {
        case l if lastLine(l) => Right(result)
        case l if errorLine(l) => Left(result, iter)
        case _ => Left(result, iter)
      }
    }
  }
}

/** The basic IOCommunicator using java's Process.
  */
trait ProcessCommunicator extends IOCommunicator {

  def cmd: String
  def args: Seq[String]

  val process: Process = startProcess()

  val processIn = new BufferedReader(new InputStreamReader(process.getInputStream, "UTF-8"))
  val processOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream, "UTF-8"))

  checkStartError()

  override def closeResource() = {
    processIn.close()
    processOut.close()
    process.destroy()
  }

  def write(line: String) = processOut.write(line)
  def writeln(line: String) {
    processOut.write(line)
    processOut.newLine()
  }
  def flush() = processOut.flush()

  def isAlive: Boolean = !isExited

  def readingIter = Iterator.continually(processIn.readLine())

  protected def startProcess(): Process =
    control.Exception.allCatch either startWithRedirectError() match {
      case Right(process)
          if (!ProcessCommunicator.isExited(process)) => process
      case Right(deadProcess) => startError(new RuntimeException)
      case Left(error) => startError(error)
    }

  private def startWithRedirectError() = {
    val fullCmd = (cmd.split("\\s+") ++ args).toSeq.asJava
    val pb = new ProcessBuilder(fullCmd)
    pb.redirectErrorStream(true)
    pb.start
  }

  /** Called when failing to launch the software with given command
    */
  protected def startError(e: Throwable) = throw e

  protected def checkStartError() = {}

  protected def isExited = ProcessCommunicator.isExited(process)
}

object ProcessCommunicator {
  private def isExited(p: Process) =
    try { p.exitValue; true }
    catch { case e: IllegalThreadStateException => false }
}

/** An example class of IOCommunicator
  */
class CommonProcessCommunicator(val cmd: String, val args: Seq[String])
    extends ProcessCommunicator

/** A communicator, which may be used in a unit test.
  * Writing does nothing. By reading, it reads the given output lines.
  */
class StubExternalCommunicator(outputs: Seq[String]) extends IOCommunicator {

  def this(output: String) = this(Seq(output))

  def isAlive = true

  def write(line: String) = {}
  def writeln(line: String) = {}
  def flush() = {}

  var i = 0

  def readingIter = {
    val iter = if (i < outputs.size) outputs(i).split("\n").toIterator else Iterator[String]()
    i += 1
    iter
  }
}

class MapStubExternalCommunicator(responces: Map[String, String]) extends IOCommunicator {

  var currentIn = ""

  def isAlive = true

  def write(line: String) = currentIn = line.trim() // assuming line ends with `\n`, which is generally true
  def writeln(line: String) = currentIn = line
  def flush() = {}

  def readingIter = {
    val o = responces(currentIn)
    o.split("\n").toIterator
  }
}
