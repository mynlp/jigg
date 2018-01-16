package jigg.pipeline

/** A singleton managing the collection of `UnmanagedAnnotator`.
  *
  * See the document of `UnmanagedAnnotator` for its role. `list` is an essential object,
  * which preserves mapping from the annotator name to an `UnmanagedAnnotator`. If you
  * want to support a new annotator that depends on an unmanaged library, add it to the
  * `list`.
  */
object UnmanagedAnnotators {

  /** Information about the annotator that wraps a software, which is in JVM while not
    * included as a managed library via maven.
    *
    * When assembling, such external unmanaged jars are not included, so a user has to
    * explicitly add them to the class path. Each UnmanagedAnnotator object helps to
    * describe how to use it. For example, its default message, implemented in
    * `DefaultUnmanagedannotator` tells the url of the library jar file.
    */
  trait UnmanagedAnnotator[A] {
    def name: String
    def clazz: Class[A]

    def msg: String
  }

  case class DefaultUnmanagedAnnotator[A](
    val name: String, val clazz: Class[A], url: String) extends UnmanagedAnnotator[A] {

    def msg = s"""Failed to launch $name. Maybe the necessary jar file is not included in
the current class path. This might be solved by adding jar/* into your class path,
e.g., call the jigg like like:

> java cp "jigg-xxx.jar:jar/*" jigg.pipeline.Pipeline ...

If the error still remains, the necessary jar file is missing. You can download it
from ${url}. Try e.g.,

> wget $url jar/

and do the above command.
"""
  }

  val list = Map(
    "easyccg" -> DefaultUnmanagedAnnotator(
      "easyccg",
      classOf[EasyCCGAnnotator],
      "https://github.com/mikelewis0/easyccg/raw/master/easyccg.jar"))
}
