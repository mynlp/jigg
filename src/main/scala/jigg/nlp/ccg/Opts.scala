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

import jigg.ml

import breeze.config.Help

import java.io.File

object Opts {

  @Help(text="About CCGBank")
  case class BankInfo(
    @Help(text="Language (ja|en)") lang: String = "ja",
    @Help(text="Path to CCGBank directory (if this is set, files in this dir are used as default values of train/dev and others)") dir: File = new File(""),
    @Help(text="# training instances, -1 for all") trainSize: Int = -1,
    @Help(text="# test instances, -1 for all") testSize: Int = -1,
    @Help(text="# dev instances, -1 for all") devSize: Int = -1
  )

  @Help(text="About category dictionary")
  case class DictParams(
    @Help(text="How to look up category candidates? (for Japanese only) (surfaceOnly|surfaceAndPoS|surfaceAndSecondFineTag|surfaceAndSecondWithConj)")
      lookupMethod: String = "surfaceAndSecondFineTag",
    @Help(text="Whether using lexicon files to create word -> category mappings")
      useLexiconFiles: Boolean = true,
    @Help(text="Minimum number of occurences for registering as lexicalized entry")
      unkThreathold: Int = 15
  ) {

    val categoryDictinoary = lookupMethod match {
      case "surfaceOnly" => new Word2CategoryDictionary
      case "surfaceAndPoS" => new WordPoS2CategoryDictionary
      case "surfaceAndSecondFineTag" => new WordSecondFineTag2CategoryDictionary
      case "surfaceAndSecondWithConj" => new WordSecondWithConj2CategoryDictionary
      case _ => sys.error("unknown lookUpMethod")
    }
  }
}
