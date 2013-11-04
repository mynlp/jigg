package enju.ccg.ml;

import java.util.List;
import enju.ccg.util.Util;
import enju.ccg.util.Pair;

public class Perceptron<L> extends AbstractClassifier<L> {
  public Perceptron(WeightVector weight) {
    super(weight);
  }

  // multiclass classification
  public void update(Example<L>[] examples, L gold) {
    L pred = predict(examples).getP1();
    if (pred != gold) {
      updateBody(examples, pred, gold);
    }
  }
  // multiclass classification
  public void updateBody(Example<L>[] examples, L pred, L gold) {
    for (Example<L> e : examples) {
      if (e.getLabel().equals(pred)) {
        for (int f : e.getFeature()) {
          weight.add(f, -1);
        }
      } else if (e.getLabel().equals(gold)) {
        for (int f : e.getFeature()) {
          weight.add(f, 1);
        }
      }
    }
  }
  // structured perceptron
  public void update(int[] predFeatures, int[] goldFeatures) {
    for (int f : predFeatures) {
      weight.add(f, -1);
    }
    for (int f : goldFeatures) {
      weight.add(f, 1);
    }
  }
}