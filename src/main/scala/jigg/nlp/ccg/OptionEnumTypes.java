package jigg.nlp.ccg;

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

public class OptionEnumTypes {
  public enum ModelType { tagger, parser };

  public enum ActionType { train, evaluate, predict };

  public enum Language { japanese, english };

  public enum StepSizeFunction { stepSize1, stepSize2, stepSize3 };

  public enum CategoryLookUpMethod { surfaceOnly, surfaceAndPoS, surfaceAndSecondFineTag, surfaceAndSecondWithConj };

  public enum TaggerTrainAlgorithm { sgd, adaGradL1 };
}
