package enju.ccg.ml

/** Augument LinearClassifier with a method to return label probabilities.
  * (implies loss function of log loss)
  */
trait LogLinearClassifier[L] extends LinearClassifier[L] {
  val weights: WeightVector[Float]

  def labelProbs(examples: Seq[Example[L]]): Array[Float] = {
    val unnormalized: Array[Float] = examples.map { e =>
      val p = Math.exp(featureScore(e.featVec)).toFloat
      if (p < 1e-100) 1e-100F else p
    }.toArray
    val z = unnormalized.sum
    unnormalized.map(_ / z)
  }
}

class FixedLogLinerClassifier[L](val weightArray: Array[Float]) extends LogLinearClassifier[L] {
  override val weights = new FixedWeightVector(weightArray)
}
