package jigg.nlp.ccg

import jigg.nlp.ccg.lexicon._
import jigg.nlp.ccg.tagger._
import jigg.ml._
import jigg.nlp.ccg.parser._
import java.io._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
 * Small demonstration of CKY Parser on ccgbank.
 * 
 * NOTE: It needs a pre-trained tagging-model which is loaded in the beginning.
 * For successful loading, the serialized data have to be pointed at in "Options.scala"
 * (e.g. @Option(gloss = "Path to trained model") var loadModelPath = "jar/ccg-models/parser/beam=64.ser.gz")
 */
object CKYTest{
  var tagging: SuperTagging = _
  def instantiateSuperTagging = new JapaneseSuperTagging
  def loadSuperTagging = {
    tagging = instantiateSuperTagging
    tagging.load
  }
  
  def main( args: Array[String] ): Unit = {
    val startTime = System.nanoTime()
    
    loadSuperTagging

    val bankreader = tagging.newCCGBankReader
    val simplifier = new SimplifyCCGBank
    
    val x = tagging.featureExtractors
    /*
     * Latest ccgbank must be simplified in order to work with JapaneseParseTreeConverter.
     * Otherwise: java.util.NoSuchElementException: key not found: f
     */
    val pathToSimpleBank = simplifier.simplifyAndGetNewPath("res/ccgbank-20150216/test.ccgbank")    // Get the ccgbank from where ever it is.
    val res = bankreader.readParseTrees(pathToSimpleBank, 50, false)
    val converter = tagging.parseTreeConverter
    
    /* Map each sentence to its gold annotation. */
    type Tree = ParseTree[String]
    val sentenceToAnnotation = new HashMap[String, Tree]()
    val goldSentences = new ArrayBuffer[TaggedSentence]
    
    val c = res.toList
    for(el <- c){
      val x = converter.toSentenceFromStringTree(el)
      sentenceToAnnotation += (x.wordSeq.mkString("") -> el)
      goldSentences += x
    } 
    
    val filesHere = (new java.io.File("res/lexicon/")).listFiles()
    
    /* Since lexicon has to be obtained only once, make this check. */
    if(!filesHere.exists(_.getName().startsWith("lexicon_all"))){
      println("Save lexicon...")
      val reader = new LexiconReader
      
      // TAKES ABOUT FIVE MINUTES!
      val res = reader.getLexicon("res/ccgbank-20150216/Japanese.lexicon")    // Get the lexicon from where ever it is.

      // Save the resulting lexicon in a sensible directory!
      val fout = new FileOutputStream("res/lexicon/lexicon_all.ser")
      val out = new ObjectOutputStream(fout)
      out.writeObject(res)
      out.close
      fout.close  
      println("Lexicon saved!")
    }
    else{
      /* Get the serialized lexicon. */
      println("Load lexicon...")
      
      // Load the resulting lexicon from a sensible directory!
      val fin = new FileInputStream("res/lexicon/lexicon_all.ser")
      val oin = new ObjectInputStream(fin)
      val lex = oin.readObject.asInstanceOf[Tuple3[Set[String], Set[AtomicCategory], HashMap[String, HashMap[String, Set[Category]]]]]
      oin.close
      fin.close
      println("Lexicon loaded!")
      
      val terminalsLoad = lex._1
      val categoriesLoad = lex._2
      //val mappingLoad = lex._3
      
      /* Create set of ccg rules. */
      val rules = Set(TypeChangingRule1, TypeChangingRule2, TypeChangingRule3,
          BackwardApplication, BackwardCompositionNested, BackwardCrossedComposition, ForwardApplication, ForwardComposition, ForwardCrossedComposition)
      
      /* Create grammar. */
      val jg = new CCGrammar(terminalsLoad, categoriesLoad, rules)
      
      val jcky = new CKYParser(jg)  

      
      val tagger = tagging.getTagger
      
      /* Supertag the sentences. */
      val anno:Seq[CandAssignedSentence] = tagging.superTagToSentences(goldSentences.toArray)
      
      for(goldSentence <- anno){     
        val sentenceInfo = tagger.unigramCategoryDistributions(goldSentence)
        
        val goldAnno = sentenceToAnnotation(goldSentence.wordSeq.mkString(""))
        
        println("Start parsing sentence: " + goldSentence.wordSeq.mkString(""))      
          
        val jchart: Array[Array[ChartCellWithBackpointers]] = jcky.parseSentence(goldSentence, sentenceInfo)
        //println("Print contents of chart:")
        //jcky.printChart(jchart)
                
        println("\nGetting parses...")
        val xy = jcky.getMostProbableParseTrees(jchart) 
        println("Got " + xy.size + " parse trees!")   
        
        /* If there are valid output parses, evaluate them and only print the best value of each precision, recall and f-score. */
        if(!xy.isEmpty){          
          println("\nEvaluate " + xy.size + " output parses:")
          val evaluator = new CKYEvaluator
          val listOfFScores = evaluator.calculateFScores(xy, goldAnno)
          val listOfPrecisions  = evaluator.calculatePrecisions(xy, goldAnno)
          val listOfRecalls = evaluator.calculateRecalls(goldAnno, xy)
            
          println("Evaluation complete!\n")

          println("Best precision: " + listOfPrecisions.max)
          println("Best recall: " + listOfRecalls.max)
          println("Best f-score: " + listOfFScores.max)
        }
        
      }
      
      val endTime = System.nanoTime()
      val resultTime: Double = (endTime - startTime)/1000000000d
      println("\nTook "+ resultTime + " s")
    }
  }
}