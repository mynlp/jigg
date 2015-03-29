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

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Properties
import scala.util.matching.Regex
import scala.collection.mutable.ArrayBuffer
import scala.xml._
import jigg.util.XMLUtil

class SimpleKNPAnnotator(override val name: String, override val props: Properties) extends SentencesAnnotator with KNPAnnotator{
  @Prop(gloss = "Use this command to launch KNP (-tab and -anaphora are mandatory and automatically added). Version >= 4.12 is assumed.") var command = "knp"
  readProps()

  //for KNP 4.12 (-ne option is unneed)
  lazy private[this] val knpProcess = new java.lang.ProcessBuilder(command, "-tab", "-anaphora").start
  lazy private[this] val knpIn = new BufferedReader(new InputStreamReader(knpProcess.getInputStream, "UTF-8"))
  lazy private[this] val knpOut = new BufferedWriter(new OutputStreamWriter(knpProcess.getOutputStream, "UTF-8"))

  /**
    * Close the external process and the interface
    */
  override def close() {
    knpOut.close()
    knpIn.close()
    knpProcess.destroy()
  }

  override def newSentenceAnnotation(sentence: Node): Node = {
    def runKNP(jumanTokens:Node): Seq[String] = {
      knpOut.write(recovJumanOutput(jumanTokens).mkString)
      knpOut.flush()

      Stream.continually(knpIn.readLine()) match {
        case strm @ (begin #:: _) if begin.startsWith("# S-ID") => strm.takeWhile(_ != "EOS").toSeq :+ "EOS"
        case other #:: _ => argumentError("command", s"Something wrong in $name\n$other\n...")
      }
    }

    val sindex = (sentence \ "@id").toString
    val jumanTokens = (sentence \ "tokens").head
    val knpResult = runKNP(jumanTokens)

    makeXml(sentence, knpResult, sindex)
  }


  override def requires = Set(Requirement.TokenizeWithJuman)
  override def requirementsSatisfied = {
    import Requirement._
    Set(Chunk, Dependency, BasicPhrase, BasicPhraseDependency, Coreference, PredArg, NamedEntity)
  }
}
