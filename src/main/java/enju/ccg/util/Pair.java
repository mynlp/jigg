/*
 * Created on 2009/06/23
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package enju.ccg.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 
 * @author Katsuhiko Hayashi
 * modified by Hiroshi Noji
 */
public class Pair<T1, T2> implements Serializable {
  private static final long serialVersionUID = 1737883993425874l;
  private static final int CURRENT_SERIAL_VERSION = 0;
  private T1 p1;
  private T2 p2;

  public Pair(T1 p1, T2 p2) {
    this.p1 = p1;
    this.p2 = p2;
  }

  public T1 getP1() {
    return p1;
  }

  public T2 getP2() {
    return p2;
  }

  public void setP1(T1 p1) {
    this.p1 = p1;
  }

  public void setP2(T2 p2) {
    this.p2 = p2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Pair)) return false;
    Pair pair = (Pair)o;
    if (p1 == null) {
      if (pair.p1 != null) return false;
    } else {
      if (!p1.equals(pair.p1)) return false;
    }
    if (p2 == null) {
      if (pair.p2 != null) return false;
    } else {
      if (!p2.equals(pair.p2)) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
		int result = (p1 != null ? p1.hashCode() : 0);
		result = 31 * result + (p2 != null ? p2.hashCode() : 0);
		return result;
  }
  
  @Override
  public String toString() {
    return "(" + getP1() + ", " + getP2() + ")";
  }

  public static class FirstComparator<S extends Comparable<? super S>, T>
      implements Comparator<Pair<S, T>> {
    public int compare(Pair<S, T> lhs, Pair<S, T> rhs) {
      return lhs.getP1().compareTo(rhs.getP1());
    }
  }
  
  public static <T1,T2> Pair<T1,T2> makePair(T1 p1, T2 p2) {
    return new Pair<T1,T2>(p1, p2);
  }
}
