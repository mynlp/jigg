package enju.ccg.ml

/** Augument LinearClassifier with a method to return label probabilities.
  * (implies loss function of log loss)
  */
trait LogLinearClassifier[L] extends LinearClassifier[L] {
  def weights: NumericBuffer[Double]

  def labelProbs(examples: Seq[Example[L]]): Array[Double] = {
    val unnormalized: Array[Double] = examples.map { e =>
      val p = Math.exp(featureScore(e.featVec))
      if (p < 1e-10) 1e-10 else p
    }.toArray
    val z = unnormalized.sum
    unnormalized.map(_ / z)
  }
}

class ALogLinearClassifier[L](override val weights: NumericBuffer[Double]) extends LogLinearClassifier[L]

// class LogLinearClassifier[L](weights: NumericBuffer[Double]) extends LinearClassifier[L](weights) {
//   def labelProbs(examples: Seq[Example[L]]): Array[Double] = {
//     val unnormalized: Array[Double] = examples.map { e =>
//       val p = Math.exp(featureScore(e.featVec))
//       if (p < 1e-10) 1e-10 else p
//     }.toArray
//     val z = unnormalized.sum
//     unnormalized.map(_ / z)
//   }
// }
