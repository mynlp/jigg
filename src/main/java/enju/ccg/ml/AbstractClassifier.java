package enju.ccg.ml;

import java.util.List;
import enju.ccg.util.Pair;
import enju.ccg.util.Util;

/**
 * @author Hiroshi Noji
 */
public abstract class AbstractClassifier<L> {
  protected WeightVector weight;
  public AbstractClassifier(WeightVector weight) {
    this.weight = weight;
  }
  
  /**
   * @return argmax label and that score
   */
  public Pair<L, Double> predict(List<Example<L>> examples) {
    double max = -Double.POSITIVE_INFINITY;
    L argMax = null;
    for (Example<L> e : examples) {
      double s = calcScore(e.getFeature());
      if (s > max) {
        max = s;
        argMax = e.getLabel();
      }
    }
    return Pair.makePair(argMax, max);
  }

  public double calcScore(int[] feature) {
    double score = 0;
    for (int f : feature) {
      score += weight.get(f);
    }
    return score;
  }

  public abstract void update(List<Example<L>> examples, L gold);
}