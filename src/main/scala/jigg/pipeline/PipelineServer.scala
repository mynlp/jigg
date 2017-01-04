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

import java.util.Properties
import java.io.{BufferedReader, BufferedWriter, PrintStream}

import scala.xml.{XML, Node}
import scala.xml.dtd.DocType
import scala.collection.JavaConverters._
import scala.io.StdIn

import jigg.util.LogUtil.{ track, multipleTrack }
import jigg.util.{PropertiesUtil => PU, IOUtil, XMLUtil, JSONUtil}

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.coding.Deflate
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshaller
// import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.model._
// import akka.http.scaladsl.model.MediaTypes.`application/xml`
import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout


class PipelineServer(val properties: Properties = new Properties) extends PropsHolder {

  def prop(key: String) = PU.findProperty(key, properties)

  @Prop(gloss="Port to serve on (default: 8080)") var port = 8080
  @Prop(gloss="Host to serve on (default: localhost. Use 0.0.0.0 to make public)") var host = "localhost"
  @Prop(gloss="Property file for jigg pipeline") var props = ""

  readProps()

  override def description: String = s"""Usage:
${super.description}

In addition to using "-props" argument, a user can also specify options
(e.g., -annotators) by directly giving them in the command line. For example,

 $$ java -cp jigg.jar jigg.pipeline.PipelineServer -annotators "corenlp[ssplit,tokenize]"

launches the server with the two annotators (ssplit and tokenize). Other commands defined in
"jigg.pipeline.Pipeline" are also passed directly to Jigg if specified. See below.

====
Usage of Jigg pipeline (jigg.pipeline.Pipeline):

${pipeline.description}"""

  val pipeline = new Pipeline(properties)

  def printHelp(os: PrintStream) = os.println(this.description)

  def run() = {

    pipeline.annotatorList // initiate the lazy val (loading all models)

    case class OutputType(format: String)

    implicit val system = ActorSystem("jigg-server")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("annotate") {
        post {
          parameters('format ? "default").as(OutputType) { outputType =>
            entity(as[String]) { text =>
              val annotation = pipeline.annotate(text)
              def outputBy(format: String) = format match {
                case "json" => complete(JSONUtil.toJSON(annotation).toString)
                case _ =>
                  val w = new java.io.StringWriter
                  pipeline.writeTo(w, annotation)
                  complete(w.toString)

                  // complete(HttpEntity(`application/xml`, annotation.toString))
                  // case _ => complete(HttpEntity(ContentType(MediaTypes.`application/json`), annotation))
                  // val w = new java.io.StringWriter
                  // XML.write(w, annotation, "UTF-8", true, DocType("root"))
                  // w.toString
              }
              outputType.format match {
                case "json" | "xml" => outputBy(outputType.format)
                case _ => outputBy(pipeline.outputFormat)
              }
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, host, port)

    println(s"Server online at $host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

object PipelineServer {

  def main(args: Array[String]): Unit = {

    val props = jigg.util.ArgumentsParser.parse(args.toList)

    val server = new PipelineServer(props)
    PU.findProperty("help", props) match {
      case Some(help) =>
        server.printHelp(System.out)
      case None => server.run()
    }
  }
}
