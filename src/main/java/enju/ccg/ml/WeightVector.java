package enju.ccg.ml;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import gnu.trove.TDoubleArrayList;
import enju.ccg.util.Util;

/**
 * 
 * @author Hiroshi Noji
 */
public class WeightVector implements Serializable {
  protected static final long serialVersionUID = 42L;
  
  //private ArrayList<Double> W;
  private TDoubleArrayList W;
  public WeightVector() {
    this(1000);
  }
  public WeightVector(int initSize) {
    //W = new ArrayList<Double>(initSize);
    W = new TDoubleArrayList(initSize);
  }
  public int size() {
    return W.size();
  }
  public double get(int featureIdx) {
    if (featureIdx < 0) return 0; // assuming this is unknown feature at test time
    if (W.size() <= featureIdx) return 0;
    return W.get(featureIdx);
  }
  public void set(int featureIdx, double newWeight) {
    if (W.size() <= featureIdx) {
      int addingNum = featureIdx + 1 - W.size();
      for (int i = 0; i < addingNum; ++i) W.add(0);
      //Util.resizeListDouble(W, featureIdx + 1);
    }
    W.setQuick(featureIdx, newWeight);
  }
  public void add(int featureIdx, double diff) {
    set(featureIdx, get(featureIdx) + diff);
  }
}