package enju.ccg.tagger
import org.scalatest.FunSuite
import enju.ccg.lexicon.{SimpleWord, AtomicCategory, SimpleDictionary, AVM}

// class FUnigramWordTest extends FunSuite {
//   test("different word instances with the same id should create the same FW instance.") {
//     val w0 = SimpleWord(0,"aaa")
//     val w0Dummy = SimpleWord(0,"bbb")
//     val c = AtomicCategory(0,"NP")
//     val fw1 = new FW(w0, c)
//     val fw2 = new FW(w0Dummy, c)
//     assert(fw1 == fw2)
//   }
//   test("FW and FWPrev1 with the same word id should not be equal.") {
//     val w = SimpleWord(0, "aaa")
//     val c = AtomicCategory(0, "NP")
//     assert(new FW(w,c) != new FWPrev1(w,c))
//   }
// }

// class FeatureIterationTest extends FunSuite {
//   class ADictionary extends SimpleDictionary { // a hard-coded lexicon
//     val w0 = SimpleWord(0, "a")
//     val w1 = SimpleWord(1, "b")
//     val w2 = SimpleWord(2, "c")
//     val c0 = AtomicCategory(0, "A")
//     val c1 = AtomicCategory(1, "B")
//     override def getWord(wId:Int) = wId match {
//       case 0 => w0; case 1 => w1; case 2 => w2
//     }
//     override def getCategory(cId:Int) = cId match {
//       case 0 => c0; case 1 => c1
//     }
//   }
//   class UncasedFeatureException extends Exception

//   val dict = new ADictionary
//   val features = List(new FUnigramWord(SimpleWord(0, "a"), AtomicCategory(0, "A"), "w"),
//                       new FBigramWord(SimpleWord(1, "b"), SimpleWord(2, "c"), AtomicCategory(1, "B"), "wPrev1_w"))

//   test("Uncased feature must be detected with an exception in a foreach loop.") {
//     intercept[UncasedFeatureException] {
//       val featureStrs = features.foreach {
//         feature => feature match {
//           case f:FUnigramWord[_] =>
//           case _ => throw new UncasedFeatureException
//           // case _ => throw new Exception("Uncased feature type " + feature.getClass.getName + " is detected; please implement the case for this type.")
//         }
//       }
//     }
//   }
//   test("Branching of mkString for each feature is correctly processed.") {
//     val featureStrs = features.map {
//       feature => feature match {
//         case f:FUnigramWord[_] => f.mkString(dict)
//         case f:FBigramWord[_] => f.mkString(dict)
//         case _ => throw new UncasedFeatureException
//       }
//     }
//     assert(featureStrs == List("w###a=>A", "wPrev1_w###b###c=>B"))
//   }
// }
