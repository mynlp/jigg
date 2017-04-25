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

import scala.annotation.tailrec

/** If you want to define your own Requirement, please override this.
  */
trait Requirement {
  def parent = Seq[Requirement]()

  def allAncestors(): Set[Requirement] = {
    @tailrec
    def collectAncestors(r: Seq[Requirement], current: Set[Requirement]): Set[Requirement] = {
      r.map(_.parent).flatten match {
        case Seq() => current ++ r
        case seq => collectAncestors(seq, current ++ r)
      }
    }
    collectAncestors(Seq(this), Set[Requirement]())
  }
}

object Requirement {

  case object Dsplit extends Requirement

  case object Ssplit extends Requirement

  case object Tokenize extends Requirement

  case object POS extends Requirement

  case object Lemma extends Requirement

  case object Dependencies extends Requirement

  case object NER extends Requirement

  case object Coreference extends Requirement

  // Now this corresponds to Mention in the CoreNLP, which is (probably) used internally
  // for saving mention candidates.
  case object Mention extends Requirement

  case object PredArg extends Requirement

  case object Parse extends Requirement

  case object Chunk extends Requirement

  // mainly prepared for Stanford CoreNLP
  case object NormalizedNER extends Requirement

  case object StanfordNER extends Requirement {
    override val parent = Seq(NER, NormalizedNER)
  }

  case object BasicDependencies extends Requirement {
    override val parent = Seq(Dependencies)
  }
  case object CollapsedDependencies extends Requirement {
    override val parent = Seq(Dependencies)
  }
  case object CollapsedCCProcessedDependencies extends Requirement {
    override val parent = Seq(Dependencies)
  }
}


object JaRequirement {

  trait JaTokenize extends Requirement {
    import Requirement._
    override def parent = Seq(Tokenize, POS, Lemma)
  }

  case object TokenizeWithIPA extends JaTokenize
  case object TokenizeWithJumandic extends JaTokenize
  case object TokenizeWithUnidic extends JaTokenize

  case object Juman extends Requirement {
    override def parent = Seq(TokenizeWithJumandic)
  }

  case object CabochaChunk extends Requirement {
    override val parent = Seq(Requirement.Chunk)
  }

  case object KNPChunk extends Requirement {
    override val parent = Seq(Requirement.Chunk)
  }

  case object KNPPredArg extends Requirement {
    override val parent = Seq(Requirement.PredArg)
  }

  case object BasicPhraseCoreference extends Requirement

  case object ChunkDependencies extends Requirement

  case object BasicPhrase extends Requirement
  case object BasicPhraseDependencies extends Requirement
  // case object Coreference extends Requirement
  // case object PredArg extends Requirement

  case object CCGDerivation extends Requirement
  case object CCGDependencies extends Requirement

  case object BunsetsuChunk extends Requirement {
    override val parent = Seq(Requirement.Chunk)
  }
}

/** This set is a specialized set to preserve satisfied requirements. If an element is
  * added to this collection, all its ancestor requirements are also added automatically.
  */
sealed trait RequirementSet { self =>

  protected val elems: Set[Requirement]

  def |(other: RequirementSet): RequirementSet =
    this | other.elems.map(_.allAncestors).flatten.toSet

  def |(otherElems: Set[Requirement]): RequirementSet = new RequirementSet {
    override val elems = self.elems | otherElems
  }

  /** Elements in requirements, which is not in this.
    */
  def lackedIn(requirements: RequirementSet): Set[Requirement] =
    lackedIn(requirements.elems)

  def lackedIn(requirements: Set[Requirement]): Set[Requirement] =
    requirements &~ (elems & requirements)
}

object RequirementSet {
  def apply(_elems: Requirement*) = new RequirementSet {
    override val elems = _elems.map(_.allAncestors).flatten.toSet
  }
}
