package enju.ccg.ml;

public class L1LogisticFOBOS<L> extends LogisticFOBOS<L> {
  public L1LogisticFOBOS(double C, int N, WeightVector weight, StepSize stepSize) {
    super(C, N, weight, stepSize);
  }

  public double getReguralizedWeight(double w) {
    if (w > 0) {
      return Math.max(0, w - (C / N) * getStepSize());
    } else {
      return Math.min(0, w + (C / N) * getStepSize());
    }
  }
}