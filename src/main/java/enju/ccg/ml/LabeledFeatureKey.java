package enju.ccg.ml;

import java.io.Serializable;
import enju.ccg.util.Pair;

/**
 * pair of feature and label
 */
public class LabeledFeatureKey<F, L> extends Pair<F, L> {
  protected static final long serialVersionUID = 42L;
  
  public LabeledFeatureKey(F feature, L label) {
    super(feature, label);
  }
  public F getFeature() { return getP1(); }
  public L getLabel() { return getP2(); }
}