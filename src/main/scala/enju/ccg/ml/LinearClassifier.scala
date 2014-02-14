package enju.ccg.ml

trait Classifier[L] {
  def weights: NumericBuffer[Float]
  def predict(examples: Seq[Example[L]]): (L, Float)
}

trait LinearClassifier[L] extends Classifier[L] {
  override def predict(examples: Seq[Example[L]]): (L, Float) =
    examples.foldLeft[(L, Float)]((null.asInstanceOf[L], Float.NegativeInfinity)) {
      case ((argMax, max), e) =>
        val eScore = featureScore(e.featVec)
        if (eScore > max) (e.label, eScore) else (argMax, max)
    }
  def featureScore(feature: Array[Int]): Float = {
    var a = 0.0F
    var i = 0
    while (i < feature.size) {
      a += weight(feature(i))
      i += 1
    }
    a
  }
  /** Controll the behavior of the access to weight.
    * You *MUST* use this method to access weight inside the classifier, and *NEVER* call like weights(i) directly (except updating the value)
    * This is because in some classifiers, such as AdaGradL1, the values must be preprocessed (e.g., lazy update) before used.
    * You can add such a preprocessing by overriding this method in a subclass.
    */
  protected def weight(idx: Int): Float = weights(idx)
}

class ALinearClassifier[L](override val weights: NumericBuffer[Float]) extends LinearClassifier[L]
