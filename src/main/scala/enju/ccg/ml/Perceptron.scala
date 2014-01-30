package enju.ccg.ml

class Perceptron[L](override val weights: NumericBuffer[Double]) extends LinearClassifier[L] with OnlineTrainer[L] {
  val averageWeights = new NumericBuffer[Double](weights.size)
  var c: Double = 1.0

  override def update(examples: Seq[Example[L]], gold: L): Unit = {
    val pred = predict(examples)._1
    if (pred != gold) {
      var i = 0
      while (i < examples.size) {
        val label = examples(i).label
        if (label == pred) updateFeatureWeighs(examples(i).featVec, -1.0)
        else if (label == gold) updateFeatureWeighs(examples(i).featVec, 1.0)
        i += 1
      }
    }
    c += 1.0
  }
  def updateFeatureWeighs(featVec: Array[Int], scale: Double): Unit = featVec.foreach { f =>
    weights(f) += scale
    averageWeights(f) += scale * c
  }
  def update(predFeatVec:Array[Int], goldFeatVec:Array[Int]): Unit = {
    updateFeatureWeighs(predFeatVec, -1.0)
    updateFeatureWeighs(goldFeatVec, 1.0)
    c += 1.0
  }
  def takeAverage: Unit = (0 until weights.size) foreach { i =>
    weights(i) -= averageWeights(i) / c
  }
}
