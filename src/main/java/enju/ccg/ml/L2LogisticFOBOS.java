package enju.ccg.ml;

public class L2LogisticFOBOS<L> extends LogisticFOBOS<L> {
  public L2LogisticFOBOS(double C, int N, WeightVector weight, StepSize stepSize) {
    super(C, N, weight, stepSize);
  }

  public double getReguralizedWeight(double w) {
    return w / (1 + getStepSize());
  }
}