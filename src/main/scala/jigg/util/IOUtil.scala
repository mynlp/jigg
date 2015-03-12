package jigg.util

import java.io._
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

object IOUtil {
  def openBinIn(path: String): ObjectInputStream = oiStream(inStream(path))
  def openBinIn(in: InputStream): ObjectInputStream = oiStream(in)

  def openZipBinIn(in: InputStream) = openBinIn(new GZIPInputStream(in))

  private[this] def oiStream(in: InputStream) =
    new ObjectInputStream(new BufferedInputStream(in))

  def openBinOut(path: String): ObjectOutputStream =
    new ObjectOutputStream(new BufferedOutputStream(outStream(path)))

  def openIn(path: String): BufferedReader = bufReader(inStream(path))
  def openOut(path: String): BufferedWriter = bufWriter(outStream(path))

  def openStandardIn: BufferedReader = bufReader(System.in)
  def openStandardOut: BufferedWriter = bufWriter(System.out)

  def inStream(path: String) = path match {
    case gzipped if gzipped.endsWith(".gz") => new GZIPInputStream(new FileInputStream(gzipped))
    case file => new FileInputStream(file)
  }
  def outStream(path: String) = path match {
    case gzipped if gzipped.endsWith(".gz") => new GZIPOutputStream(new FileOutputStream(gzipped))
    case file => new FileOutputStream(file)
  }

  def bufReader(stream: InputStream) = new BufferedReader(new InputStreamReader(stream))
  def bufWriter(stream: OutputStream) = new BufferedWriter(new OutputStreamWriter(stream))

  def openIterator(path: String): Iterator[String] = inputIterator(openIn(path))
  def openStandardIterator: Iterator[String] = inputIterator(openStandardIn)
  def inputIterator(reader: BufferedReader) = Iterator.continually(reader.readLine()).takeWhile(_ != null)
}
