package jigg.util

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

import org.scalatest._
import scala.xml._

class CoNLLUtilSpec extends FlatSpec with Matchers {

  "toCoNLLUInWord" should "output in CoNLLU format" in {
    val input = <root>
    <document id="d0">
    <sentences>
    <sentence id="s0">
    a dog
    <tokens>
    <token id="t0" form="a" pos="DET" upos="DET" lemma="a"/>
    <token id="t1" form="dog" pos="NOUN" upos="NOUN" lemma="dog"/>
    </tokens>
    <dependencies>
    <dependency id="d0" head="t1" dependent="t0" deprel="det"/>
    <dependency id="d1" head="root" dependent="t1" deprel="root"/>
    </dependencies>
    </sentence>
    <sentence id="s1">
    a cat
    <tokens>
    <token id="t2" form="a" pos="DET" upos="DET" lemma="a"/>
    <token id="t3" form="cat" pos="NOUN" upos="NOUN" lemma="cat"/>
    </tokens>
    <dependencies>
    <dependency id="d2" head="t3" dependent="t2" deprel="det"/>
    <dependency id="d3" head="root" dependent="t3" deprel="root"/>
    </dependencies>
    </sentence>
    </sentences>
    </document>
    </root>

    val iter = CoNLLUtil.toCoNLLUInWord(input)
    val s = iter.toArray.toSeq

    val expected = Seq(
      "# newdoc id = d0",
      "# sent_id = s0",
      "# text = a dog",
      "1\ta\ta\tDET\tDET\t_\t2\tdet\t_\t_",
      "2\tdog\tdog\tNOUN\tNOUN\t_\t0\troot\t_\t_",
      "",
      "# sent_id = s1",
      "# text = a cat",
      "1\ta\ta\tDET\tDET\t_\t2\tdet\t_\t_",
      "2\tcat\tcat\tNOUN\tNOUN\t_\t0\troot\t_\t_",
      "",
      "")
    s should equal(expected)
  }

  it should "not output heads when deps are not annotated" in {
    val input = <root>
    <document id="d0">
    <sentences>
    <sentence id="s0">
    a dog
    <tokens>
    <token id="t0" form="a" pos="DET" upos="DET" lemma="a"/>
    <token id="t1" form="dog" pos="NOUN" upos="NOUN" lemma="dog"/>
    </tokens>
    </sentence>
    </sentences>
    </document>
    </root>

    val iter = CoNLLUtil.toCoNLLUInWord(input)
    val s = iter.toArray.toSeq

    val expected = Seq(
      "# newdoc id = d0",
      "# sent_id = s0",
      "# text = a dog",
      "1\ta\ta\tDET\tDET\t_\t_\t_\t_\t_",
      "2\tdog\tdog\tNOUN\tNOUN\t_\t_\t_\t_\t_",
      "",
      "")
    s should equal(expected)
  }

  "toCoNLLUInChunk" should "output chunk dependencies" in {

    val input = <root>
    <document id="d0">
    <sentences>
    <sentence id="s0">
    太郎は食べた
    <tokens>
    <token id="t0" form="太郎" pos="_" upos="NOUN" lemma="太郎"/>
    <token id="t1" form="は" pos="_" upos="AUX" lemma="は"/>
    <token id="t2" form="食べ" pos="_" upos="VERB" lemma="食べ"/>
    <token id="t3" form="た" pos="_" upos="AUX" lemma="た"/>
    </tokens>
    <chunks>
    <chunk id="c0" tokens="t0 t1"/>
    <chunk id="c1" tokens="t2 t3"/>
    </chunks>
    <dependencies unit="chunk">
    <dependency id="d0" head="c1" dependent="c0" deprel="nsubj"/>
    <dependency id="d1" head="root" dependent="c1" deprel="root"/>
    </dependencies>
    </sentence>
    </sentences>
    </document>
    </root>

    val iter = CoNLLUtil.toCoNLLUInChunk(input)
    val s = iter.toArray.toSeq

    val expected = Seq(
      "# newdoc id = d0",
      "# sent_id = s0",
      "# text = 太郎は食べた",
      "1\t太郎|は\t太郎|は\tNOUN|AUX\t_\t_\t2\tnsubj\t_\t_",
      "2\t食べ|た\t食べ|た\tVERB|AUX\t_\t_\t0\troot\t_\t_",
      "",
      "")
    s should equal(expected)
  }
}
