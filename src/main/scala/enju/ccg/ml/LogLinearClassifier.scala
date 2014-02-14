package enju.ccg.ml

/** Augument LinearClassifier with a method to return label probabilities.
  * (implies loss function of log loss)
  */
trait LogLinearClassifier[L] extends LinearClassifier[L] {
  def weights: NumericBuffer[Float]

  def labelProbs(examples: Seq[Example[L]]): Array[Float] = {
    val unnormalized: Array[Float] = examples.map { e =>
      val p = Math.exp(featureScore(e.featVec)).toFloat
      if (p < 1e-100) 1e-100F else p
    }.toArray
    val z = unnormalized.sum
    unnormalized.map(_ / z)
  }
}

class ALogLinearClassifier[L](override val weights: NumericBuffer[Float]) extends LogLinearClassifier[L]

// class LogLinearClassifier[L](weights: NumericBuffer[Float]) extends LinearClassifier[L](weights) {
//   def labelProbs(examples: Seq[Example[L]]): Array[Float] = {
//     val unnormalized: Array[Float] = examples.map { e =>
//       val p = Math.exp(featureScore(e.featVec))
//       if (p < 1e-10) 1e-10 else p
//     }.toArray
//     val z = unnormalized.sum
//     unnormalized.map(_ / z)
//   }
// }
