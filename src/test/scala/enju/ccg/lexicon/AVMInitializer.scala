package enju.ccg.lexicon

object AVMInitializer {
  val featuresPath = getClass.getClassLoader.getResource("data/features.txt").getPath
  var done = false
  def init = this.synchronized { if (!done) { AVM.readK2V(featuresPath); done = true } }
}
