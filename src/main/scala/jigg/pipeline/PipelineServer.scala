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
import java.io.{ByteArrayOutputStream, PrintStream}

import scala.xml.{XML, Node}
import scala.xml.dtd.DocType
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.io.StdIn

import jigg.util.LogUtil.{ track, multipleTrack }
import jigg.util.{PropertiesUtil => PU, IOUtil, XMLUtil, JSONUtil}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.coding.Deflate
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout

class PipelineActor extends Actor {
  import PipelineServer.Params

  val log = Logging(context.system, this)

  var lastParams: Params = null
  var lastPipeline: Pipeline = null

  def receive = {
    case params: Params => {
      val coreParams = removeNonCore(params)
      // If params are empty, use the same params as before.
      if (coreParams != lastParams && !coreParams.isEmpty) reset(coreParams)
      sender ! lastPipeline
    }
  }

  def removeNonCore(params: Params) = {
    // The server is agnostic to the changes of these properties
    // (not creating a new pipeline).
    val noncore = Seq("props", "file", "output", "help", "outputFormat",
      "checkRequirement", "inputFormat")
    Params(params.kvs filter { case (k, v) => !(noncore contains k) })
  }

  def reset(params: Params) = {
    lastParams = params

    val props = new Properties
    for ((k, v) <- params.kvs) props.setProperty(k, v)

    log.info("Pipeline is updated. New property: " + props)

    lastPipeline = new Pipeline(props)
  }
}

class PipelineServer(val properties: Properties = new Properties) extends PropsHolder {

  def prop(key: String) = PU.findProperty(key, properties)

  @Prop(gloss="Port to serve on (default: 8080)") var port = 8080
  @Prop(gloss="Host to serve on (default: localhost. Use 0.0.0.0 to make public)") var host = "localhost"

  readProps()

  def printHelp(os: PrintStream) = os.println(this.description)

  override def description: String = s"""Usage:
${super.description}

JiggServer can be used as an interface of Jigg to other languages such as Python.
See README in "python/pyjigg" for this usage.

Another usage via curl is that:

  > curl --data-urlencode 'annotators=corenlp[tokenize,ssplit]' \
         --data-urlencode 'q=Please annotate me!' \
         'http://localhost:8080/annotate?outputFormat=json'

The data with the key "q" is treated as an input text. Multiple "q"s in a query
are allowed and are concatenated.

Currently this server only supports POST method and the input text should be a raw text
(not XML or JSON, which will be supported in future). For each call, users must specify
the properties as the parameters.

The annotation for the first input may be very slow due to loading all annotator models,
which may take 30 ~ 60 secs if you use heavy components of Stanford CoreNLP (e.g., coref).

The annotation for the followed inputs should be reasonably fast, but note that if you
call the server with different parameters than the last call, the internal pipeline will
be reconstructed, and the loading time will be taken again.

To see the valid options, call "jigg.pipeline.Pipeline -help", or after starting the
server, access to e.g.,
  http://localhost:8080/help
which displays the help message of the internal pipeline. One can also see the specific
help for each annotator with:
  http://localhost:8080/help/<annotator name>
where <annotator name> may be specific name such as corenlp or kuromoji."""

  def run() = {

    case class OutputType(format: String)

    implicit val timeout = Timeout(5.seconds)

    implicit val system = ActorSystem("jigg-server")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val actor = system.actorOf(Props[PipelineActor])

    val route =
      path("annotate") {
        post {
          parameterSeq { _params =>
            formFieldSeq { _forms =>

              val (textSeq, formParamSeq) = _forms.partition(a => a._2 == "" || a._1 == "q")
              val text = textSeq map {
                case (a, "") => a
                case ("q", a) => a
              } mkString "\n"

              val params = _params.toMap ++ formParamSeq.toMap

              val maybePipeline = (actor ? PipelineServer.Params(params)).mapTo[Pipeline]

              val maybeResult = maybePipeline map { pipeline =>
                val annotation = pipeline.annotate(text)
                def outputBy(format: String): String = format match {
                  case "json" => JSONUtil.toJSON(annotation).toString
                  case _ =>
                    val w = new java.io.StringWriter
                    pipeline.writeTo(w, annotation)
                    w.toString
                }
                params get "outputFormat" match {
                  case Some(a) if a == "json" || a == "xml" => outputBy(a)
                  case _ => outputBy("xml")
                }
              }
              complete(maybeResult)
            }
          }
        }
      } ~ pathPrefix("help") {
        pathEnd {
          complete(mkHelp("true"))
        } ~
        pathPrefix(".+".r) { annotator =>
          pathEndOrSingleSlash {
            def normalize(c: Char) = c match {
              case '<' => '['
              case '>' => ']'
              case _ => c
            }
            complete(mkHelp(annotator.map(normalize)))
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

  def mkHelp(annotator: String): String = {
    val props = new Properties
    props.setProperty("help", annotator)
    val pipeline = new Pipeline(props)
    val s = new ByteArrayOutputStream
    pipeline.printHelp(new PrintStream(s))
    s.toString("UTF8")
  }

}

object PipelineServer {

  case class Params(kvs: Map[String, String]) {
    def isEmpty() = kvs.isEmpty
  }

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
