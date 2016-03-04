package jigg.util

import scala.collection.mutable.ListBuffer
import scala.xml._
import scala.util.control.Breaks

object SecpressionUtil{

  class StrStack{
      private var datalist:ListBuffer[String] = ListBuffer.empty[String]
      
      def init()
      {
        while(!datalist.isEmpty)datalist.remove(0)
        datalist
      }
      def push(data:String):Int ={
        data +=: datalist
        datalist.length
      }

      def pop():String ={
        var str = datalist.head
        datalist.remove(0)
        str
      }

      def print():Int = {
        for(elem<- datalist) println(elem)
        datalist.size
      }
      def size():Int = {
        datalist.size
      }
    }


  def exportXML(input:String,tokens:NodeSeq,node:Node,sid:String):Node ={

    var stk:StrStack = new StrStack
    var XmlStr:String = ""
    var elements = input.replace("\n","").split("[\\s]+")
    var local_id = 0
    var outNode = node
    var items:ListBuffer[Map[String,String]] = ListBuffer.empty[Map[String,String]]
    var token_items: ListBuffer[Map[String,String]]  = ListBuffer.empty[Map[String,String]]

    val tokenSeq = tokens \ "token"
    for( tok <- tokenSeq){
       var tid = (tok \ "@id").toString
       var tword = (tok \ "@form").toString
       var tokmap:Map[String,String] = Map("id" -> tid , "text" -> tword )
       token_items += tokmap
       
    }
    
    var tmpId = sid + "_p" + local_id
    for(elemnt <- elements) {
      if(elemnt startsWith "(" ) {
        tmpId = sid + "_p" + local_id
        var tag_name = elemnt.drop(1)

        if( tag_name != "ROOT"){
          var map:Map[String,String] = Map( "id"-> tmpId , "symbol" -> tag_name,"children" -> "")
          items += map
          stk.push(tmpId) 
          local_id+=1
        }else{
          stk.push("ROOT")
        }

      }else{
        var text = elemnt
        var num = 0
        while( text.endsWith(")") ){
          text = text.dropRight(1)
          num = num +1 
        }
        var searchID =""
        var InsID = ""
        var p = -1
        val b = new Breaks
        b.breakable {
          for( i <- 0 until token_items.size)
          {
            if( token_items(i)("text") == text)
            {
              p = i
            }
          }
        }
        if( p > -1){
          InsID = token_items(p)("id")
          token_items.remove(p)
          
        }else{
          InsID = text
        }
        
        for( i <- 0 to num){
          if ( stk.size > 0){
            searchID = stk.pop()
            if( i == num) stk.push(searchID)
            if( searchID != "ROOT"){
              var p = -1
              for( j <- 0 until items.size){
                if( items(j)("id") == searchID){
                  p = j
                }
              }
              if( p > -1){
                var tmp_map =items(p)
                var newmap:Map[String,String] = Map( "id"-> tmp_map("id") , "symbol" ->  tmp_map("symbol"), "children" -> (tmp_map("children")+ " " + InsID).trim)
                items.update(p,newmap)
                InsID = tmp_map("id")
              }
            }
          }
        }
      }
    }
    for( data <- items){
       var tmpNode = <span id={data("id")} symbol={data("symbol")} children={data("children")} />
       outNode = XMLUtil.addChild(outNode,tmpNode )
    }
    outNode
  }

}
