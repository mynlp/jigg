package enju.ccg.ml

/** An implementation of Stochastic gradient descent with cumulative L1 penalty:
  *   Tsuruoka, Tsujii and Ananiadou (2009)
  *   Stochastic Gradient Descent Training for L1-regularized Log-linear
  *   Models with Cumulative Penalty, IJCNLP
  */
class LogLinearSGDCumulativeL1[L](
  weights: NumericBuffer[Float],
  a: Float,
  val c: Float,
  val numInstances: Int) extends LogLinearSGD[L](weights, a) {

  val q = new NumericBuffer[Float](weights.size)
  var u = 0.0F

  override def reguralizeWeights(examples: Seq[Example[L]]): Unit = {
    val eta: Float = (c / numInstances) * stepSize
    u += eta

    var j = 0
    while (j < examples.size) {
      val e = examples(j)
      val feats = e.featVec
      var k = 0

      while (k < feats.size) {
        val i = feats(k)
        val w = weight(i)
        val z = w
        val qi = q(i)
        val newW = if (w > 0) Math.max(0, w - (u + qi)) else Math.max(0, w + (u - qi))
        weights(i) = newW
        q(i) += (newW - z)

        k += 1
      }
      j += 1
    }
  }
}
