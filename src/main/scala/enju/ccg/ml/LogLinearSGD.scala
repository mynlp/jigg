package enju.ccg.ml

class LogLinearSGD[L](override val weights: NumericBuffer[Double], val a: Double)
    extends OnlineLogLinearTrainer[L] {
  def stepSize: Double = Math.pow(time + 1, -a) // avoid the overflow
  def updateExampleWeights(e: Example[L], gold: L, derivative: Double): Unit = {
    val dw = stepSize * derivative
    val feats = e.featVec
    var i = 0
    while (i < feats.size) {
      weights(feats(i)) += dw
      i += 1
    }
  }
}
