package enju.util

import java.io._
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

object IOUtil {
  def openBinIn(path: String) =
    new ObjectInputStream(new BufferedInputStream(inStream(path)))

  def openIn(path: String) = new BufferedReader(new InputStreamReader(inStream(path)))

  def inStream(path: String) = path match {
    case gzipped if gzipped.endsWith(".gz") => new GZIPInputStream(new FileInputStream(gzipped))
    case file => new FileInputStream(file)
  }
}
