package enju.ccg.ml;

import java.io.Serializable;
import java.util.List;
import enju.ccg.util.Pair;
import enju.ccg.util.Util;

/**
 * Multiclass logistic classifier (a.k.a MaxEnt) with Stochastic gradient descent updates.
 * This classifier consider no reguralization terms;
 * LogisticCumulativeSGD or LogisticL1FOBOS, LogsiticL2FOBOS are subclasses with reguralizations.
 * 
 * @author Hiroshi Noji
 */
public class LogisticSGD<L> extends AbstractClassifier<L> {
  public static abstract class StepSize {
    public abstract double get(int k);
  }
  public static class StepSize1 extends StepSize {
    double a;
    int N;
    public StepSize1(double a, int N) { this.a = a; this.N = N; }
    public double get(int k) {
      return a / (1 + (double)k / N);
    }
  }
  public static class StepSize2 extends StepSize {
    double a;
    double b;
    int N;
    public StepSize2(double a, double b, int N) { this.a = a; this.b = b; this.N = N; }
    public double get(int k) {
      int iter = k / N;
      double x = a / (b + iter);
      return x;
    }
  }
  public static class StepSize3 extends StepSize {
    double a;
    public StepSize3(double a) { this.a = a; }
    public double get(int k) {
      double x = Math.pow(k, -a);
      return x;
    }
  }
  
  protected int N;
  protected int k;
  private StepSize stepSize;
  
  public LogisticSGD(int N, WeightVector weight, StepSize stepSize) {
    super(weight);
    this.N = N;
    this.k = 0;
    this.stepSize = stepSize;
  }

  /**
   * for debugging purpose
   */
  public void setK(int k) {
    this.k = k;
  }

  public double getStepSize() {
    return stepSize.get(k);
  }
  
  public double[] calcLabelProbs(List<Example<L>> examples) {
    double[] dist = new double[examples.size()];
    for (int l = 0; l < examples.size(); ++l) {
      dist[l] = Math.exp(calcScore(examples.get(l).getFeature()));
      if (dist[l] < 1e-10) dist[l] = 1e-10;
    }
    Util.normalize(dist);
    return dist;
  }

  public void update(List<Example<L>> examples, L gold) {
    k++;    
    updateBody(examples, gold);
    reguralize(examples);
  }
  public void updateBody(List<Example<L>> examples, L gold) {
    double[] dist = calcLabelProbs(examples);
    for (int l = 0; l < examples.size(); ++l) {
      Example<L> e = examples.get(l);
      double d = calcDerivative(e.getLabel(), gold, dist[l]);
      for (int f : e.getFeature()) {
        weight.add(f, getStepSize() * d);
      }
    }
  }
  // do nothing; please override
  public void reguralize(List<Example<L>> examples) {}

  public double calcDerivative(L predict, L gold, double p) {
    return predict.equals(gold) ? (1 - p) : -p;
  }
}