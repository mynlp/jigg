package jigg.nlp.ccg;

public class OptionEnumTypes {
  public enum ModelType { tagger, parser };
  
  public enum ActionType { train, evaluate, predict };
  
  public enum Language { japanese, english };
  
  public enum StepSizeFunction { stepSize1, stepSize2, stepSize3 };
  
  public enum CategoryLookUpMethod { surfaceOnly, surfaceAndPoS, surfaceAndSecondFineTag, surfaceAndSecondWithConj };

  public enum TaggerTrainAlgorithm { sgd, adaGradL1 };
}
