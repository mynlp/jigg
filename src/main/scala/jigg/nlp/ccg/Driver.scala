package jigg.nlp.ccg

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

import fig.exec.Execution

object Driver {
  def addQuiet(args:Array[String]) = args ++ Array("-startMainTrack", "false")
  def main(args:Array[String]) = {
    val runner = new Runner
    Execution.run(addQuiet(args), runner, "driver", DriverOptions,
                  "input", InputOptions,
                  "output", OutputOptions,
                  "train", TrainingOptions,
                  "dict", DictionaryOptions,
                  "tagger", TaggerOptions,
                  "parser", ParserOptions)
  }
}

class Runner extends Runnable {
  import OptionEnumTypes._

  def run = {
    val problem = DriverOptions.modelType match {
      case ModelType.tagger => DriverOptions.language match {
        case Language.japanese => new JapaneseSuperTagging
        case Language.english => new EnglishSuperTagging
      }
      case ModelType.parser => DriverOptions.language match {
        case Language.japanese => new JapaneseShiftReduceParsing
        case Language.english => new EnglishShiftReduceParsing
      }
    }

    DriverOptions.actionType match {
      case ActionType.train => problem.train
      case ActionType.evaluate => problem.evaluate
      case ActionType.predict => problem.predict
    }
    // if (OutputOptions.saveModelPath != "") problem.save // now, save is tied to train for safety
  }
}
