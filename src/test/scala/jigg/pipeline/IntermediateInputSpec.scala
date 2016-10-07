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

import java.util.Properties
import org.scalatest._
import scala.xml._
import jigg.util.{XMLUtil, JSONUtil}

class IntermediateInputSpec extends FlatSpec with Matchers {

  def findPath(localPath: String) = getClass.getClassLoader.getResource(localPath).getPath

  "Pipeline" should "accept a XML file and handle it in English" in {
    val p = new Properties
    p.setProperty("annotators","spaceTokenize")
    p.setProperty("checkRequirement","false")
    p.setProperty("inputFormat","xml")
    p.setProperty("file", findPath("./data/xml/english.ssplit.test.xml"))
    p.setProperty("output", findPath("./data/xml/") + "english.ssplit.spaceTokenize.test.xml")
    val pipeline = new Pipeline(p)
    pipeline.run
    pipeline.close()

    val testXML = XML.load(findPath("./data/xml/english.ssplit.spaceTokenize.test.xml"))
    val goldXML = XML.load(findPath("./data/xml/english.ssplit.spaceTokenize.gold.xml"))

    testXML should be (goldXML)
  }

  "Pipeline" should "accept a XML file and handle it in Japanese" in {
    val p = new Properties
    p.setProperty("annotators","kuromoji")
    p.setProperty("checkRequirement","false")
    p.setProperty("inputFormat","xml")
    p.setProperty("file", findPath("./data/xml/japanese.ssplit.test.xml"))
    p.setProperty("output", findPath("./data/xml/") + "japanese.ssplit.kuromoji.test.xml")
    val pipeline = new Pipeline(p)
    pipeline.run
    pipeline.close()

    val testXML = XML.load(findPath("./data/xml/japanese.ssplit.kuromoji.test.xml"))
    val goldXML = XML.load(findPath("./data/xml/japanese.ssplit.kuromoji.gold.xml"))

    testXML should be (goldXML)
  }

  "Pipeline" should "accept a JSON file and handle it in English" in {
    val p = new Properties
    p.setProperty("annotators","spaceTokenize")
    p.setProperty("checkRequirement","false")
    p.setProperty("inputFormat","json")
    p.setProperty("file", findPath("./data/json/english.ssplit.test.json"))
    p.setProperty("output", findPath("./data/json/") + "english.ssplit.spaceTokenize.test.xml")
    val pipeline = new Pipeline(p)
    pipeline.run
    pipeline.close()

    val testXML = XML.load(findPath("./data/json/english.ssplit.spaceTokenize.test.xml"))
    val goldXML = XML.load(findPath("./data/xml/english.ssplit.spaceTokenize.gold.xml"))

    testXML should be (goldXML)
  }

  "Pipeline" should "accept a JSON file and handle it in Japanese" in {
    val p = new Properties
    p.setProperty("annotators","kuromoji")
    p.setProperty("checkRequirement","false")
    p.setProperty("inputFormat","json")
    p.setProperty("file", findPath("./data/json/japanese.ssplit.test.json"))
    p.setProperty("output", findPath("./data/json/") + "japanese.ssplit.kuromoji.test.xml")
    val pipeline = new Pipeline(p)
    pipeline.run
    pipeline.close()

    val testXML = XML.load(findPath("./data/json/japanese.ssplit.kuromoji.test.xml"))
    val goldXML = XML.load(findPath("./data/xml/japanese.ssplit.kuromoji.gold.xml"))

    testXML should be (goldXML)
  }
}
