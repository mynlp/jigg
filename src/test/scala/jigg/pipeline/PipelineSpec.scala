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

import java.util.Properties
import org.scalatest._
import scala.xml._
import jigg.util.{XMLUtil, JSONUtil}

class PipelineSpec extends BaseAnnotatorSpec {

  class StubMecabAnnotator(n: String, p: Properties)
      extends IPAMecabAnnotator(n, p) {
    override def mkLocalAnnotator = new IPALocalMecabAnnotator {
      override def mkCommunicator = new StubExternalCommunicator("aaa")
    }
  }

  class DummyPipeline(p: Properties) extends Pipeline(p) {
    override def getAnnotator(name: String) = name match {
      case "dummy" => new StubMecabAnnotator(name, p)
      case _ => super.getAnnotator(name)
    }
  }

  "-Threads option" should "be able to customize each annotator's number of threads" in {
    val p = new Properties
    p.setProperty("annotators", "ssplit,dummy")
    p.setProperty("nThreads", "2")
    p.setProperty("dummy.nThreads", "4")

    val pipeline = new DummyPipeline(p)

    val annotators = pipeline.annotatorList
    annotators(0).name should equal("ssplit")
    annotators(0).nThreads should equal(2)
    annotators(1).name should equal("dummy")
    annotators(1).nThreads should equal(4)
  }
}
