package jigg.util


/*
 Copyright 2013-2018 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licencses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitation under the License.
*/

import scala.xml._

import XMLUtil.RichNode

object CoNLLUtil {

  trait SentenceConverter {
    def sentence: Node

    val tokens = sentence \ "tokens" \ "token"

    val tokenId2idx: Map[String, Int] = tokens.zipWithIndex.map { case (t, i) =>
      t \@ "id" -> i
    }.toMap

    def heads: Option[Seq[(Int, String)]]
    def head(i: Int) = heads map { a => (a(i)._1 + 1) + "" } getOrElse "_"
    def rel(i: Int) = heads map { a => a(i)._2 } getOrElse "_"

    def toCoNLLU(): Iterator[String] = {
      val sentid = sentence \@ "id"
      val sentBegin = s"# sent_id = ${sentid}"
      val text = s"# text = ${sentence.textElem}"
      Iterator(sentBegin, text) ++ mkIter() ++ Iterator("")
    }

    def mkIter(): Iterator[String]

    def mkHeadsArray(
      unitSeq: NodeSeq,
      depsNodes: NodeSeq,
      unitId2idx: Map[String, Int]): Option[Seq[(Int, String)]] = {

      depsNodes match {
        case Seq() => None
        case nodes =>
          val depseq = nodes.head \ "dependency"
          val heads = new Array[(Int, String)](tokens.size)
          depseq.map { arc =>
            val dep = unitId2idx(arc \@ "dependent")
            val head = (arc \@ "head") match {
              case "root"|"ROOT" => -1
              case id => unitId2idx(id)
            }
            val deprel = (arc \@ "deprel") match { case "" => "_"; case r => r }
            heads(dep) = (head, deprel)
          }
          Some(heads.toSeq)
      }
    }
  }

  class SentenceWordConverter(override val sentence: Node) extends SentenceConverter {

    val heads = mkHeadsArray(tokens, sentence \ "dependencies", tokenId2idx)

    override def mkIter(): Iterator[String] = {
      tokens.zipWithIndex.iterator map { case (token, i) =>
        def find(key: String) = token \@ key match {
          case "" => "_"
          case v => v
        }
        val id = i + 1
        val form = find("form")
        val lemma = find("lemma")
        val pos = find("pos")
        val upos = find("upos")
        val feats = find("feats")
        val h = head(i)
        val r = rel(i)
        s"${id}\t${form}\t${lemma}\t${upos}\t${pos}\t${feats}\t${h}\t${r}\t_\t_"
      }
    }
  }

  class SentenceChunkConverter(override val sentence: Node) extends SentenceConverter {

    val chunksNodes = sentence \ "chunks"
    assert(!chunksNodes.isEmpty)

    val chunks = chunksNodes.head \ "chunk"

    val chunkId2idx: Map[String, Int] = chunks.zipWithIndex.map { case (t, i) =>
      t \@ "id" -> i
    }.toMap

    val heads = mkHeadsArray(
      chunks,
      (sentence \ "dependencies").filter(_ \@ "unit" == "chunk"),
      chunkId2idx)

    override def mkIter(): Iterator[String] = {
      chunks.zipWithIndex.iterator.map { case (chunk, i) =>
        val ctokens = (chunk \@ "tokens").split(" ").map(tokenId2idx).map(tokens)
        def cat(key: String) = {
          val values = ctokens.map { _ \@ key match {
            case "" => "_"
            case v => v
          }}
          if (values.forall(_ == "_")) "_" else values.mkString("|")
        }
        val id = i + 1
        val form = cat("form")
        val lemma = cat("lemma")
        val pos = cat("pos")
        val upos = cat("upos")
        val h = head(i)
        val r = rel(i)
        s"${id}\t${form}\t${lemma}\t${upos}\t${pos}\t_\t${h}\t${r}\t_\t_"
      }
    }
  }

  def toCoNLLUInWord(input: Node): Iterator[String] =
    toCoNLLU(input, s => new SentenceWordConverter(s).toCoNLLU())

  def toCoNLLUInChunk(input: Node): Iterator[String] =
    toCoNLLU(input, s => new SentenceChunkConverter(s).toCoNLLU())

  private def toCoNLLU(
    input: Node,
    sentConv: Node=>Iterator[String]): Iterator[String] = {

    assert(input.label == "root")
    val documents = input \ "document"

    documents.iterator.flatMap { document =>
      val docid = document \@ "id"
      val docBegin = s"# newdoc id = ${docid}"

      val sentences = document \ "sentences" \ "sentence"
      val iter = sentences.iterator flatMap sentConv
      Iterator(docBegin) ++ iter ++ Iterator("")
    }
  }
}
