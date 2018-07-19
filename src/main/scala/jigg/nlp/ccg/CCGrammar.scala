package jigg.nlp.ccg

import jigg.nlp.ccg.lexicon._
import scala.collection.mutable.HashMap
import scala.language.existentials

/**
 * Implements a simplififed CCG Grammar. Contains a Set of terminals (i.e. tokens),
 * a Set of atomic Non-Terminals (both are retrieved from ccgbank-lexicon) and a Set of CCG Rules.
 */
case class CCGrammar(terminals: Set[String], atomicCategories: Set[AtomicCategory], rules: Set[_ <: CCGRules])