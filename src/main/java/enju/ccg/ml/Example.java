package enju.ccg.ml;

import java.util.List;
import java.util.ArrayList;
import enju.ccg.util.Util;

/**
 * A pair of feature and label. Feature contains the label information itself.
 * 
 * @author Hiroshi Noji
 */
public class Example<L> {
  private int[] feature;
  private L label;

  public Example(L label) {
    this.label = label;
  }

  public void setFeature(List<Integer> f) {
    feature = Util.toArrayInt(f);
  }

  public void setFeatureQuick(int[] feature) {
    this.feature = feature;
  }
  
  public int[] getFeature() {
    return feature;
  }
  public L getLabel() {
    return label;
  }
}