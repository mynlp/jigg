package jigg.pipeline

import scala.xml.{XML, Node}
import java.util.Properties

import jp.ac.tohoku.ecei.cl.www.chapas._

import jp.ac.tohoku.ecei.cl.www.base._
import jp.ac.tohoku.ecei.cl.www.io._
import jp.ac.tohoku.ecei.cl.www.util._
import jp.ac.tohoku.ecei.cl.www.kucf._
import jp.ac.tohoku.ecei.cl.www.mapdb._
import jp.ac.tohoku.ecei.cl.www.db._
import jp.ac.tohoku.ecei.cl.www.liblinear._
import jp.ac.tohoku.ecei.cl.www.coord._
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.ByteArrayInputStream
import java.io.BufferedWriter
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ListBuffer
import jigg.util.XMLUtil

class ChaPASAnnotator(override val name: String, override val props: Properties) extends SentencesAnnotator{


    var options:ChaPASOptions = new ChaPASOptions()
    var chapas:ChaPAS  = new ChaPAS(options)
    var modelPrepared:Boolean = false
    var train:Boolean = false
    var cl = chapas.getClass().getClassLoader()
    chapas.loadModel(cl.getResourceAsStream(options.modelFile))
    modelPrepared = true;

    override def newSentenceAnnotation(sentence: Node) = {
      val tokens = sentence \ "tokens"
      val chunks = sentence \ "chunks"
      val NEs = sentence \ "NEs"
      val dependencies = sentence \ "dependencies"
      val NeSeq = NEs \ "NE"
      val tokenSeq = tokens \ "token"
      val depSeq = dependencies \ "dependency"
      val chuSeq = chunks \ "chunk"
      val tok_item = (0 until tokenSeq.size) map { i=>
         val item = tokenSeq(i)
         val id = (item \ "@id").toString
         val form = (item \ "@form").toString
         val pos = (item \ "@pos").toString
         val pos1 = (item \ "@pos1").toString
         val pos2 = (item \ "@pos2").toString
         val pos3 = (item \ "@pos3").toString
         val str = form+"\t"+pos+","+pos1+","+pos2+","+pos3+",*,*,*,*,*"
         Map("ID"->id,"str"->str,"form"->form)
      }

      val chun_item = (0 until chuSeq.size) map { i=>
         val chu_item = chuSeq(i)
         val dep_item = depSeq(i)
         val id = (chu_item \ "@id").toString.split("_")(1).drop(3)
         val chu_head = (chu_item \"@id").toString
         val rel = (dep_item \ "@deprel").toString
         val fun = (chu_item \ "@tokens").toString.split(" ").size -1
         val tok_id = (chu_item \ "@head").toString
         val he= {
           val dp_head = (dep_item \ "@head").toString
           if( dp_head == "root"){ "-1"}
           else{ dp_head.split("_")(1).drop(3) }
         }
         val str = "* "+ i + " " + he + rel  + " 0/"+fun.toString + " 0.0"
         Map("ID"->tok_id, "str"->str ,"tok"->chu_head)
           
       }
         
       var Ner_item = {
         var ner_list= Map.empty[String, String]

         for (item <- NeSeq){
           val tid = (item \ "@id").toString
           val toks = (item \ "@tokens").toString.split(" ")
           val label = (item \ "@label").toString
           ner_list = ner_list + (tid->label)
           for( tok_id <- toks){
             ner_list = ner_list + (tok_id->"O")
           }
         }
         ner_list
       }


      var toCabString = ""

      var p = 0
      var ch = chun_item(p)
      for (tok <-tok_item)
      {
        var tid = tok("ID")
        if( tid == ch("ID"))
        {
           toCabString = toCabString + ch("str") + "\n"
           p = p+1
           if( p < chun_item.size){ch = chun_item(p)}
        }
        var label = Ner_item(tid)
        toCabString = toCabString + tok("str")+ "\t"+label+"\n"
      }
      toCabString = toCabString +"EOS\n"



      var is:InputStream = new ByteArrayInputStream(toCabString.getBytes(StandardCharsets.UTF_8));
      var pipe:CaboCha2Dep = new CaboCha2Dep(is)
      var caboChaOutPipe:JapaneseDependencyTree2CaboCha = new JapaneseDependencyTree2CaboCha()
      caboChaOutPipe.setPrintSentenceID(options.printSentenceID)
      caboChaOutPipe.setOutputPASNBest(options.numOfPASNBest > 1)


     var writer:BufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out, "utf-8"))

      var cnt:Int = 0;
      var pred_xml = ""
      while (!pipe.eof()) {
        var tree:DependencyTree  = pipe.pipePerSentence()
        if (tree != null) { 
          var trees:Array[DependencyTree] = Array(tree)
          chapas.analyze(trees)
          if (options.outputFormat == CaboChaFormat.UNK) {
            options.outputFormat = pipe.getFormat()
            caboChaOutPipe.setFormat(options.outputFormat)
          }
          var resultStr:StringBuilder = new StringBuilder()

          for(i <- 0 until trees.size)
          {
            resultStr.append(caboChaOutPipe.pipePerSentence(trees(i)))
          }
          var res_buffer = resultStr.toString().trim
          var pred_item =  mkpredItem(res_buffer ,tok_item)
          pred_xml = pred_xml+mkpredXml(pred_item)
          
        }
      }
      pred_xml = "<predargs annotators=\"chapas\">"+pred_xml+"</predargs>"
      var pred_node = XML.loadString(pred_xml)
      XMLUtil.addChild(sentence, pred_node)
   }

   def mkpredXml(pred_items:ListBuffer[Map[String,String]]) :String ={

     var ans:String = ""
     for( item <- pred_items){
       var local_str = ""
       if( item.contains("ga")){
            var arg_id =item("ga")
            var arg = ""
            for( item2<-pred_items){
              if(item2.contains("ID")){
                   if( arg_id.trim == item2("ID")){
                      arg = item2("tid")
                   }
              } 

            }
            local_str += "<predarg pred=\""+item("tid") +"\" type = "+item("type")+" deprel=\"ga\" arg=\""+arg+"\"/>"
        }
        if( item.contains("o")){
            var arg_id = item("o")
            var arg = ""
            for( item2<-pred_items){
              if(item2.contains("ID")){
                   if( arg_id.trim == item2("ID")){
                      arg = item2("tid")
                   }
              } 

            }
            local_str += "<predarg pred=\""+item("tid") +"\" type = "+item("type")+" deprel=\"o\" arg=\""+arg+"\"/>"
        }
        ans += local_str

       }
       ans
     }
   

   def mkpredItem(res_buffer:String ,tok_items:IndexedSeq[Map[String,String]]):ListBuffer[Map[String,String]] = {
     var res_lines = res_buffer.split("\n")
     var predItems = ListBuffer.empty[Map[String,String]]

     for (res_line <- res_lines){
       var res_items = res_line.split("\t")
       if( res_items.size == 4){
         var tok = res_items(0).trim
         var preg_parms = res_items(3).split(" ")
         var predItem = Map.empty[String,String]
         for( param_d <- preg_parms){
           var param = param_d.split("=")
           predItem = predItem ++ Map(param(0)->param(1))
         }
         var p = -1
         var tid = ""
         for( i <- 0 until tok_items.size){
           var tok_item = tok_items(i)
           if(tok_item("form")  == tok){
             p = i
             tid = tok_item("ID")
           }
         }
         if( p >= 0){
           predItem = predItem ++ Map("tid"->tid)
           predItems += predItem
         }
       }
     }
     predItems

   }
}


object ChaPASAnnotator extends AnnotatorCompanion[ChaPASAnnotator]

