package enju.ccg.ml;

import java.util.List;
import enju.ccg.util.Util;
import enju.ccg.util.Pair;

public class Perceptron<L> extends AbstractClassifier<L> {
  private WeightVector averageWeight;
  private double C; // count for averaging
  public Perceptron(WeightVector weight) {
    super(weight);    
    averageWeight = new WeightVector();
    C = 1;
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
          averageWeight.add(f, -C);
        }
      } else if (e.getLabel().equals(gold)) {
        for (int f : e.getFeature()) {
          weight.add(f, 1);
          averageWeight.add(f, C);
        }
      }
    }
    C += 1;
  }
  // structured perceptron
  public void update(int[] predFeatures, int[] goldFeatures) {
    for (int f : predFeatures) {
      weight.add(f, -1);
      averageWeight.add(f, -C);
    }
    for (int f : goldFeatures) {
      weight.add(f, 1);
      averageWeight.add(f, C);
    }
    C += 1;
  }
  public void finalize() {
    System.out.println(C);
    for (int i = 0; i < weight.size(); ++i) {
      weight.set(i, weight.get(i) - averageWeight.get(i) / C);
    }
  }
}