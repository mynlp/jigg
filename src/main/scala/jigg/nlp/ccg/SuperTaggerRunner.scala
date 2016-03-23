package jigg.nlp.ccg

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

import lexicon._

import breeze.config.{CommandLineParser, Help}

import scala.collection.mutable.{ArraySeq}

import java.io.File


class SuperTaggerRunner(model: SuperTaggerModel, params: SuperTaggerRunner.Params) {

  val tagger = model.mkMultiTagger()

  def assignKBests[S<:TaggedSentence](sentences: Array[S]): ArraySeq[S#AssignedSentence] =
    sentences map (assignKBest)

  def assignKBest[S<:TaggedSentence](s: S): S#AssignedSentence =
    s assignCands (tagger candSeq(s, params.beta, params.maxK))
}

object SuperTaggerRunner {

  @Help(text="Params for testing/evaluating super tagger")
  case class Params(
    // @Help(text="Load model path") model: SuperTaggerModel: SuperTaggerModel,
    @Help(text="Beta for decising the threshold of k-best at prediction") beta: Double = 0.001,
    @Help(text="Maximum number of k, -1 for no limit") maxK: Int = -1
  )
}
