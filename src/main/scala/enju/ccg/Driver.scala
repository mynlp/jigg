package enju.ccg

import fig.exec.Execution

object Driver {
  def addQuiet(args:Array[String]) = args ++ Array("-startMainTrack", "false")
  def main(args:Array[String]) = {
    val runner = new Runner
    Execution.run(addQuiet(args), runner, DriverOptions, InputOptions, OutputOptions, TrainingOptions, DictionaryOptions)
  }
}

class Runner extends Runnable {
  import OptionEnumTypes._

  def run = {
    val problem = DriverOptions.modelType match {
      case ModelType.tagger => DriverOptions.language match {
        case Language.japanese => new JapaneseSuperTagging
        case Language.english => throw new UnsupportedOperationException
      }
      case ModelType.parser => DriverOptions.language match {
        case Language.japanese => throw new UnsupportedOperationException
        case Language.english => throw new UnsupportedOperationException
      } 
    }

    DriverOptions.actionType match {
      case ActionType.train => problem.train
      case ActionType.evaluate => throw new UnsupportedOperationException
      case ActionType.predict => problem.predict
    }
    problem.save
  }
}
