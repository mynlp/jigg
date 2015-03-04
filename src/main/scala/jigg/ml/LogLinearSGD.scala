package jigg.ml

abstract class LogLinearSGD[L](val a: Float) extends OnlineLogLinearTrainer[L] {

  def stepSize = Math.pow(time + 1, -a).toFloat // avoid the overflow
  def updateExampleWeights(e: Example[L], gold: L, derivative: Float): Unit = {
    val dw = stepSize * derivative
    val feats = e.featVec
    var i = 0
    while (i < feats.size) {
      weights(feats(i)) += dw
      i += 1
    }
  }
}

class FixedLogLinearSGD[L](val weightArray: Array[Float], a: Float) extends LogLinearSGD(a) {

  override val weights = new FixedWeightVector(weightArray)
}
