package enju.ccg.util;

/**
 * 
 * @author Katsuhiko Hayashi
 * 
 */
public class Triple<T1, T2, T3> {
  private T1 p1;
  private T2 p2;
  private T3 p3;

  public Triple(T1 p1, T2 p2, T3 p3) {
    this.p1 = p1;
    this.p2 = p2;
    this.p3 = p3;
  }

  public T1 getP1() {
    return p1;
  }

  public T2 getP2() {
    return p2;
  }

  public T3 getP3() {
    return p3;
  }
}
