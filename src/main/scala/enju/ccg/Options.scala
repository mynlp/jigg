package enju.ccg

import annotation.target.getter

trait Options {
  type Option = fig.basic.Option @getter
  type OptionSet = fig.basic.OptionSet @getter
}

object DriverOptions extends Options {
  import OptionEnumTypes.{ModelType, ActionType, Language}
  // type ModelType = OptionEnumTypes.ModelType
  // type ActionType = OptionEnumTypes.ActionType
  // type Language = OptionEnumTypes.Language
  
  @Option(gloss="Running model", required=true) var modelType:ModelType = _
  @Option(gloss="Running action", required=true) var actionType:ActionType = _
  @Option(gloss="Language") var language:Language = Language.japanese
}

object InputOptions extends Options {
  @Option(gloss = "Path to CCGBank directory (if this is set, files in this dir are used as default values of train/test and others)") var bankDirPath = ""
  @Option(gloss = "Path to training CCGBank") var trainPath = ""
  @Option(gloss = "Training instances, -1 for all") var trainSize = -1
  @Option(gloss = "Path to test CCGBank for evlauation") var testPath = ""
  @Option(gloss = "Test instances, -1 for all") var testSize = -1

  @Option(gloss = "Path to Japanese AVM settings (required when training Tagger)") var avmPath = "avm_settings.txt"
  @Option(gloss = "Path to Japanese category expantion definitions (required when training Tagger)") var templatePath = ""
  @Option(gloss = "Path to lexicon (word/pos -> category mappings)") var lexiconPath = ""

  @Option(gloss = "Path to trained model") var loadModelPath = ""
}

object OutputOptions extends Options {
  @Option(gloss = "Path to output of trained model after training") var saveModelPath = ""
  @Option(gloss = "Path to write trained tagger model in a readable form") var taggerFeaturePath = "features.tagger.txt"
  @Option(gloss = "Path to write trained parser model in a readable form") var parserFeaturePath = "features.parser.txt"
}

object TrainingOptions extends Options {
  import OptionEnumTypes.StepSizeFunction

  @Option(gloss="Number of iterations") var numIters:Int = 10
  
  // todo: this can be more simple as in the interface of vowpal wabbit
  @Option(gloss="Step size function of SGD") var stepSizeFunc:StepSizeFunction = StepSizeFunction.stepSize3
  @Option(gloss="Parameter a of step size function") var stepSizeA = 0.2
  @Option(gloss="Parameter b of step size function") var stepSizeB = 5.0
}

object DictionaryOptions extends Options {
  import OptionEnumTypes.CategoryLookUpMethod
  @Option(gloss="How to look up category candidates ?") var lookupMethod:CategoryLookUpMethod = CategoryLookUpMethod.surfaceAndSecondFineTag
}

object TaggerOptions extends Options {
  @Option(gloss="Beta for decising the threshold of k-best at prediction") var beta:Double = 0.001
  
}

// object OP extends Options
