package enju.ccg.ml;

import java.util.List;
import enju.ccg.util.Util;
import enju.ccg.util.Pair;

public class Perceptron<L> extends AbstractClassifier<L> {
  public Perceptron(WeightVector weight) {
    super(weight);
  }
  
  public void update(Example<L>[] examples, L gold) {
    L pred = predict(examples).getP1();
    if (pred != gold) {
      updateBody(examples, pred, gold);
    }
  }

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
}