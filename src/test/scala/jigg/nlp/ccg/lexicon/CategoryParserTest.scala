package jigg.nlp.ccg.lexicon

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
import org.scalatest.FunSuite
import org.scalatest.Matchers._

class CategoryParserTest extends FunSuite {
  test("extractCategoryFeature") {
    val reader = new JapaneseCategoryParser.JapaneseReader
    val ni_nm = reader.extractCategoryFeature("ni,nm")
    ni_nm.toString should equal ("mod=nm,case=ni")
    //assert(ni_nm.toString == "mod=nm,case=ni")
  }

  test("createAomicCategory") {
    val cat1Str = "NP[case=nc,mod=nm]{I1}"
    val cat1 = JapaneseCategoryParser.parse(cat1Str)
    cat1.toString should equal ("NP[mod=nm,case=nc]")

    val cat2Str = "(((S[mod=adn,form=base]{I1}\\NP[case=ni,mod=nm]{I2}){I1})\\NP[case=o,mod=nm]{I3}){I1}_I1(unk,I3,I2,_)"
    val cat2 = JapaneseCategoryParser.parse(cat2Str)
    cat2.toString should equal ("(S[mod=adn,form=base]\\NP[mod=nm,case=ni])\\NP[mod=nm,case=o]")


    val cat3Str = "(NP[case=X1,mod=X2,fin=f]{I1}/NP[case=X1,mod=X2,fin=f]{I1}){I2}_none"
    val cat3 = JapaneseCategoryParser.parse(cat3Str)
    cat3.toString should equal ("NP[fin=f]/NP[fin=f]")
  }

  // These are obsolute tests for previous version
  // test("createComplexCategory") {
  //   JapaneseCategoryParser.parse("NP[nc,nm]1//NP[nc,nm]1").toString should equal("NP[mod=nm,case=nc]/NP[mod=nm,case=nc]")
  //   JapaneseCategoryParser.parse("(S[nm,stem,nm]＼NP[nc,nm])／NP[nc,nm]").toString should equal(
  //     """(S[mod=nm,form=stem]\NP[mod=nm,case=nc])/NP[mod=nm,case=nc]""")
  //   JapaneseCategoryParser.parse("(((S＼NP)／NP[nc,nm])＼(S[nm,stem]1／NP[o,nm]sem))／NP[nc,nm]1").toString should equal(
  //     """(((S\NP)/NP[mod=nm,case=nc])\(S[mod=nm,form=stem]/NP[mod=nm,case=o]))/NP[mod=nm,case=nc]""")
  //   JapaneseCategoryParser.parse("S1／S1").toString should equal("S/S")
  //   JapaneseCategoryParser.parse("(S2／S2)1／(S3／S3)1").toString should equal("(S/S)/(S/S)")
  // }
}
