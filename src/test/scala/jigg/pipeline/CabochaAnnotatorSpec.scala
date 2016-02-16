package jigg.pipeline

/*
 Copyright 2013-2015 Takafumi Sakakibara and Hiroshi Noji

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

class CabochaAnnotatorSpec extends FlatSpec with Matchers {

  def newIPA(output: String, p: Properties = new Properties) = new IPACabochaAnnotator("", p) {
    override def mkCommunicator = new StubExternalCommunicator(output)
  }

  "Annotator" should "add root dependency to one word sentence" in {

    val s = "a"
    val annotator = newIPA(Sentences.cabocha(s))
    val result = annotator.newSentenceAnnotation(Sentences.xml(s))

    val chunks = result \\ "chunks"
    chunks.size should be (1)
    chunks.head should be (<chunks><chunk id="s0_chu0" tokens="s0_tok0" head="s0_tok0" func="s0_tok0"/></chunks>)

    val deps = result \\ "dependencies"
    deps.size should be (1)
    deps.head should be (<dependencies><dependency id="s0_dep0" head="root" dependent="s0_chu0" label="D"/></dependencies>)

    result.size should be (1)
    (result \ "_" \ "_").size should be (3)
  }

  "Annotator" should "replace the old annotation with new one" in {

    val s = "annotated"
    val annotator = newIPA(Sentences.cabocha(s))
    val result = annotator.newSentenceAnnotation(Sentences.xml(s))

    val chunks = result \\ "chunks"

    chunks.size should be (1)
    chunks.head should be (<chunks><chunk id="s0_chu0" tokens="s0_tok0" head="s0_tok0" func="s0_tok0"/></chunks>)

    val deps = result \\ "dependencies"
    deps.size should be (1)
    deps.head should be (<dependencies><dependency id="s0_dep0" head="root" dependent="s0_chu0" label="D"/></dependencies>)

    result.size should be (1)
    (result \ "_" \ "_").size should be (3)
  }

  object Sentences {

    val xml = Map(
      "a" -> <sentence id="s0">あ
        <tokens>
        <token id="s0_tok0" surf="あ" pos="フィラー" pos1="*" pos2="*" pos3="*" inflectionType="*" inflectionForm="*" base="あ" reading="ア" pronounce="ア"/>
        </tokens>
        </sentence>,

      "annotated" -> <sentence id="s0">あ
        <tokens>
        <token id="s0_tok0" surf="あ" pos="フィラー" pos1="*" pos2="*" pos3="*" inflectionType="*" inflectionForm="*" base="あ" reading="ア" pronounce="ア"/>
        </tokens>
        <chunks><chunk id="s0_chu0" tokens="s0_tok0" head="" func="s0_tok0"/></chunks>
        <dependencies><dependency id="s0_dep0" head="" dependent="s0_chu0" label="D"/></dependencies>
        </sentence>
    )

    val cabocha = Map(
      "a" -> """* 0 -1D 0/0 0.000000
あ	フィラー,*,*,*,*,*,あ,ア,ア
EOS""",

      "annotated" -> """* 0 -1D 0/0 0.000000
あ	フィラー,*,*,*,*,*,あ,ア,ア
EOS"""
    )
  }
}
