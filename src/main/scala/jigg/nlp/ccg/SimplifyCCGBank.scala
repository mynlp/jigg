package jigg.nlp.ccg

import scala.io.Source
import java.io._

/**
 * This class is used so that latest ccgbank (ccgbank-20150216)
 * is compatible with CCGBankReader.
 */
class SimplifyCCGBank {
  
  def simplifyAndGetNewPath(path: String): String = {
    val source = Source.fromFile(path)
    val fname = path.substring(0, path.lastIndexOf("."))
    val npath = fname + ".simplified.ccgbank"
    val bw = new BufferedWriter(new FileWriter(new File(npath)))
    
    for(line <- source.getLines()){
      if(!line.isEmpty){
        val nLine = line.replaceAll("\\{I\\d\\}", "").replaceAll("(,)?fin=(t|f|X\\d)", "").replaceAll("(_\\w+)+", "")
        bw.write(nLine)
        bw.newLine()
      }
    }
    source.close()
    bw.flush()
    bw.close()
    npath
  }
}