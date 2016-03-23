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

class RequirementSpec extends FlatSpec with Matchers {

  "Tokenize" should "be satisfied when TokenizeWithIPA is satisfied" in {

    val satisfied = RequirementSet(JaRequirement.TokenizeWithIPA)
    val requires: Set[Requirement] = Set(Requirement.Tokenize)

    val lacked = satisfied.lackedIn(requires)
    lacked shouldBe empty
  }

  "TokenizedWithIPA" should "not be satisifed when Tokenize is satisfied" in {

    val satisfied = RequirementSet(Requirement.Tokenize)
    val requires: Set[Requirement] = Set(JaRequirement.TokenizeWithIPA)

    val lacked = satisfied.lackedIn(requires)
    lacked shouldBe Set(JaRequirement.TokenizeWithIPA)
  }
}
