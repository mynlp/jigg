package jigg.ml

trait FeatureBase

// Unlabeld feature, but not limited to: user may want to create features always with label (e.g., in structured classification exam). In such case, please include label to this class and ignore LabeldFeature.
trait Feature extends FeatureBase {
  type LabelType
  type DictionaryType
  def assignLabel(label:LabelType): LabeledFeature[LabelType]
  def concat(items:Any*): String = items.mkString("_###_")
}

trait LabeledFeature[L] extends FeatureBase {
  def unlabeled: Feature
  def label: L
}
