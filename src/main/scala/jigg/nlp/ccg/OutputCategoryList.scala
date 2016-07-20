package jigg.nlp.ccg

/*
 Copyright 2013-2015 Hiroshi Noji

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import java.io.FileWriter

import scala.collection.mutable.ArrayBuffer
import scala.sys.process.Process
import scala.collection.mutable.HashMap

import lexicon._

import breeze.config.CommandLineParser

object OutputCategoryList {

  case class Params(
    bank: Opts.BankInfo,
    dict: Opts.DictParams
  )

  case class CategoryInfo(sentence: GoldSuperTaggedSentence, position: Int, num: Int = 1) {
    def increment(): CategoryInfo = this.copy(num = num + 1)
    def replace(_sentence: GoldSuperTaggedSentence, _p: Int) =
      CategoryInfo(_sentence, _p, num + 1)
  }

  def main(args:Array[String]) = {

    val params = CommandLineParser.readIn[Params](args)

    val dict = new JapaneseDictionary(params.dict.categoryDictinoary)
    val bank = CCGBank.select(params.bank, dict)

    val trainSentences: Array[GoldSuperTaggedSentence] = bank.trainSentences

    val stats = new HashMap[Category, CategoryInfo]

    trainSentences foreach { sentence =>
      (0 until sentence.size) foreach { i =>
        val cat = sentence.cat(i)
        stats.get(cat) match {
          case Some(info) =>
            if (sentence.size > info.sentence.size)
              stats += ((cat, info.replace(sentence, i)))
            else
              stats += ((cat, info.increment()))
          case None => stats += ((cat, CategoryInfo(sentence, i)))
          case _ =>
        }
      }
    }
    def highlight(sentence: Sentence, i: Int) = {
      val tokens = sentence.wordSeq
      // tokens.take(i).mkString("") + s"\\x1b[1;31m{${tokens(i)}}\\x1b[0m" + tokens.drop(i+1).mkString("")
      tokens.slice(i-5, i).mkString("") + s"[01;31m${tokens(i)}[00m" + tokens.slice(i+1, i+6).mkString("")
    }

    var fw = new FileWriter("./category.lst")
    stats.toSeq.sortBy(_._2.num).reverse.foreach {
      case (cat, CategoryInfo(sentence, i, num)) =>
        fw.write("%s\t%s\t%s\t%s\n"
          .format(num, cat, sentence.pos(i), highlight(sentence, i)))
    }
    fw.flush
    fw.close

    val noFeatureCategories = new HashMap[String, CategoryInfo]
    stats foreach { case (cat, CategoryInfo(sentence, i, numWithFeat)) =>
      val noFeature = cat.toStringNoFeature
      noFeatureCategories.get(noFeature) match {
        case Some(exist) =>
          val newNum = numWithFeat + exist.num
          val newInfo = exist.copy(num = newNum)
          noFeatureCategories += (noFeature -> newInfo)
        case None =>
          noFeatureCategories += (noFeature -> CategoryInfo(sentence, i, numWithFeat))
        case _ =>
      }
    }

    fw = new FileWriter("./category.nofeature.lst")
    noFeatureCategories.toSeq.sortBy(_._2.num).reverse.foreach {
      case (cat, CategoryInfo(sentence, i, num)) =>
        fw.write("%s\t%s\t%s\t%s\n"
          .format(num, cat, sentence.pos(i), highlight(sentence, i)))
    }
    fw.flush
    fw.close
  }
}
