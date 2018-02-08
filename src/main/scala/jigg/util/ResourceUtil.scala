package jigg.util

import java.io.File

object ResourceUtil {

  /** Read a python script found in `resources/python/xxx.py`. Since these files cannot
    * be executed directly we create a temporary file by copying the script first, and
    * return the resulting temp file.
    *
    * @param name script name, corresponding to `xxx.py`.
    */
  def readPython(name: String): File = {
    val script = File.createTempFile("jigg", ".py")
      script.deleteOnExit
      val stream = getClass.getResourceAsStream(s"/python/${name}")
      IOUtil.writing(script.getPath) { o =>
        scala.io.Source.fromInputStream(stream).getLines foreach { line =>
          o.write(line + "\n")
        }
      }
      script
  }

}


