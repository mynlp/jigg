package jigg.pipeline

/** If you want to define your own Requirement, please override this.
  */
trait Requirement {
  def parent: Option[Requirement] = None
}

object Requirement {

  case object Sentence extends Requirement

  case object Tokenize extends Requirement

  trait TokenizeChild extends Requirement {
    override def parent = Some(Tokenize)
  }
  case object TokenizeWithIPA extends TokenizeChild
  case object TokenizeWithJuman extends TokenizeChild
  case object TokenizeWithUnidic extends TokenizeChild

  case object Chunk extends Requirement
  case object Dependency extends Requirement // dependency between chunks

  case object BasicPhrase extends Requirement
  case object BasicPhraseDependency extends Requirement
  case object Coreference extends Requirement
  case object PredArg extends Requirement
  case object NamedEntity extends Requirement

  case object CCG extends Requirement

  /** All all elements in newElem and all descendants of elements in newElems
    */
  def add(original: Set[Requirement], newElems: Set[Requirement]) =
    newElems.foldLeft(original) { (currentSet, elem) =>
      currentSet | allDescendants(elem)
    }

  private[this] def allDescendants(requirement: Requirement, current: Set[Requirement] = Set.empty): Set[Requirement] =
    requirement.parent map { p => allDescendants(p, current + requirement) } getOrElse (current + requirement)
}
