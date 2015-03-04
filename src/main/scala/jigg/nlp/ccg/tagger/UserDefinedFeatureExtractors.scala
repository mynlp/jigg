// package jigg.nlp.ccg.tagger

// import jigg.nlp.ccg.lexicon.{Dictionary, JapaneseDictionary}

// import scala.collection.mutable.ArrayBuffer

// // this is the example to define new features and the extractor that extracts that features

// object NewTemplate extends Enumeration {
//   type NewTemplate = Value
//   val w_p = Value
// }

// case class UnigramWordPoSFeature[T](word:Int, pos:Int, tmpl:T) extends FeatureOnDictionary {
//   override def mkString(dict:Dictionary) = concat(tmpl, dict.getWord(word))
// }

// class UnigramSecondLevelFineExtractor(val windowSize:Int) extends FeatureExtractor {
//   def addFeatures(c:Context, features:ArrayBuffer[UF]) = {
//     features += UnigramWordPoSFeature(c.word(0), c.pos(0), NewTemplate.w_p)
//   }
// }
