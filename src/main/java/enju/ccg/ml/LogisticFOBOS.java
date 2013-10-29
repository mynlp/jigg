package enju.ccg.ml;
import java.util.List;

/**
 * L1LogisticFOBOS and L2LogisticFOBOS implements this abstract classifier
 */
public abstract class LogisticFOBOS<L> extends LogisticSGD<L> {
  double C;
  
  public LogisticFOBOS(double C, int N, WeightVector weight, StepSize stepSize) {
    super(N, weight, stepSize);
    this.C = C;
  }

  @Override
  public void reguralize(List<Example<L>> examples) {
    for (Example<L> e : examples) {
      for (int f : e.getFeature()) {
        double w = weight.get(f);
        double eta = getStepSize();
        weight.set(f, getReguralizedWeight(w));
      }
    }
  }

  public abstract double getReguralizedWeight(double w);
}