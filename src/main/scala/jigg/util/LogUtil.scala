package jigg.util

object LogUtil {
  /** A helper to measure time.
    * If multiple commands are nested, use multipleTrack.
    *
    * TODO: Integrate track and multipleTrack to automatically choose indent and appropriate format.
    * Currently track[A](beginMessage: String, ...) "manually" handles the indent level.
    */
  def track[A](message: String)(body: => A): A = {
    // System.out.print(message)
    // val (result, time) = recordTime { body }
    // System.out.println("done [%.1f sec]".format(time))
    // result
    track(message, "done", 0) { body }
  }

  def multipleTrack[A](message: String)(body: => A): A = {
    // System.out.println("{ " + message)
    // val (result, time) = recordTime { body }
    // System.out.println("} [%.1f sec]".format(time))
    // result
    track(message + " {\n", "}", 0) { body }
  }

  def track[A](beginMessage: String, endMessage: String, indent: Int)(body: => A): A = {
    def print(raw: String) = {
      (0 until indent) foreach { _ => System.out.print(" ") }
      System.out.print(raw)
    }
    print(beginMessage)
    val (result, time) = recordTime { body }
    System.out.println(endMessage + " [%.1f sec]".format(time))
    result
  }

  def recordTime[A](body: => A): (A, Double) = {
    val before = System.currentTimeMillis
    val result = body
    val time = (System.currentTimeMillis - before).toDouble / 1000.0
    (result, time)
  }
}
