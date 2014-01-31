package enju.ccg.ml

/** A trait which support parameter update, and the interface of Classifier.
  * Currently two subclasses exists: OnlineLoglinearTrainer is used for log-linear models, while Perceptron is used to train the perceptron including structured perceptron with beam-search.
  */
trait OnlineTrainer[L] extends Classifier[L] {
  def update(examples: Seq[Example[L]], gold:L): Unit
  def postProcess: Unit = Unit
}
