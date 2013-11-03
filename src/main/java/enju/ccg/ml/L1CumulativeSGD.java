package enju.ccg.ml;
import java.util.List;

/**
 * An implementation of Stochastic gradient descent with cumulative L1 penalty (*).
 * 
 * (*) Tsuruoka, Tsujii and Ananiadou (2009)
 *     Stochastic Gradient Descent Training for L1-regularized Log-linear
 *     Models with Cumulative Penalty, IJCNLP
 *
 * @author Hiroshi Noji
 */
class L1CumulativeSGD<L> extends LogisticSGD<L> {
  double C;
  double u;
  WeightVector q;
  
  public L1CumulativeSGD(double C, int N, WeightVector weight, StepSize stepSize) {
    super(N, weight, stepSize);
    this.C = C;
    this.u = 0;
    this.q = new WeightVector();
  }

  @Override
  public void reguralize(Example<L>[] examples) {
    double eta = (C / N) * getStepSize();
    u += eta;
    for (Example<L> e : examples) {
      for (int f : e.getFeature()) {
        double w = weight.get(f);
        double z = w;
        double q_f = q.get(f);
        if (w > 0) {
          weight.set(f, Math.max(0, w - (u + q_f)));
        } else {
          weight.set(f, Math.min(0, w + (u - q_f)));
        }
        q.set(f, q_f + (w - z));
      }
    }
  }
}