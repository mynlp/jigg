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
import scala.xml.Node
import org.scalatest._

import com.atilika.kuromoji.{TokenBase, TokenizerBase}
import com.atilika.kuromoji.ipadic.{Token=>IToken, Tokenizer=>ITokenizer}

class KuromojiAnnotatorSpec extends FlatSpec with Matchers {

  "Annotator" should "assign token id using sentence id" in {

    val annotator = new IPAKuromojiAnnotator("kuromoji", new Properties)

    val sentence = <sentence id="a">あ</sentence>
    val annotated = annotator newSentenceAnnotation sentence

    val tokenId = annotated \\ "token" \@ "id"
    tokenId should be ("a_0")
  }
}
