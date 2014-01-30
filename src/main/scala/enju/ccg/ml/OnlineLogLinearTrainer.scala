package enju.ccg.ml

/** This trait exploits the common procedure in trainers of log-linear models.
  */
trait OnlineLogLinearTrainer[L] extends OnlineTrainer[L] with LogLinearClassifier[L] {
  var time: Int = 0

  override def update(examples: Seq[Example[L]], gold:L): Unit = {
    val dist = labelProbs(examples)
    var i = 0
    while (i < examples.size) {
      val e = examples(i)
      val p = dist(i)
      val derivative = if (e.label == gold) (1 - p) else -p
      updateExampleWeights(e, gold, derivative)
      i += 1
    }
    reguralizeWeights(examples)
    time += 1
  }
  def updateExampleWeights(e: Example[L], gold: L, derivative: Double): Unit
  def reguralizeWeights(examples: Seq[Example[L]]): Unit = {} // Some algorithms reguralize weights after temporalily updating the values and this method defines that postprocessing. See LogLinearSGDCumulativeL1 for example.
}
