package enju.ccg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Katsuhiko Hayashi
 * some are appended by Hiroshi Noji
 * 
 */
public class Util {
  public static int[] stringsToInts(String[] stringreps) {
    int[] nums = new int[stringreps.length];
    for (int i = 0; i < stringreps.length; i++)
      nums[i] = Integer.parseInt(stringreps[i]);
    return nums;
  }

  public static String join(List<String> a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a.get(0));
    for (int i = 1; i < a.size(); ++i)
      sb.append(sep).append(a.get(i));
    return sb.toString();
  }

  public static String joinObj(List<Object> a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a.get(0).toString());
    for (int i = 1; i < a.size(); ++i)
      sb.append(sep).append(a.get(i).toString());
    return sb.toString();
  }

  public static String join(String[] a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a[0]);
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(a[i]);
    return sb.toString();
  }

  public static String join(int[] a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a[0]);
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(a[i]);
    return sb.toString();
  }

  public static int[] toArrayInt(List<Integer> list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; ++i) {
      array[i] = list.get(i);
    }
    return array;
  }

  public static double[] toArrayDouble(List<Double> list) {
    double[] array = new double[list.size()];
    for (int i = 0; i < array.length; ++i) {
      array[i] = list.get(i);
    }
    return array;
  }
  
  public static String[] toArrayString(List<String> list) {
    String[] array = new String[list.size()];
    for (int i = 0; i < array.length; ++i) {
      array[i] = list.get(i);
    }
    return array;
  }

  public static ArrayList<Integer> toListInt(int[] array) {
    ArrayList<Integer> list = new ArrayList<Integer>(array.length);
    for (int i = 0; i < array.length; ++i) {
      list.add(array[i]);
    }
    return list;
  }
  
  public static ArrayList<Double> toListDouble(double[] array) {
    ArrayList<Double> list = new ArrayList<Double>(array.length);
    for (int i = 0; i < array.length; ++i) {
      list.add(array[i]);
    }
    return list;
  }

  public static <K extends Comparable<K>, V> int pairListLowerBound(
      int low, int high, K key, List<Pair<K, V>> list) {
    while (low < high) {
      int mid = low + (high - low) / 2;
      K midKey = list.get(mid).getP1();
      int comp = midKey.compareTo(key);
      if (comp < 0) low = mid + 1;
      else if (comp > 0) high = mid;
      else break;
      // if (midKey < key) low = mid + 1;
      // else if (midKey > key) high = mid;
      // else break;
    }
    return low + (high - low) / 2;
  }

  // A range of [from, to) (python-like)
  public static int[] range(int from, int to) {
    int[] r = new int[to - from];
    for (int i = from; i < to; ++i) r[i] = from+i;
    return r;
  }
  public static int[] range(int to) {
    return range(0, to);
  }

  public static void resizeListInt(ArrayList<Integer> list, int newSize) {
    int addingNum = newSize - list.size();
    for (int i = 0; i < addingNum; ++i) {
      list.add(0);
    }
  }
  public static void resizeListDouble(ArrayList<Double> list, int newSize) {
    int addingNum = newSize - list.size();
    for (int i = 0; i < addingNum; ++i) {
      list.add(0.0);
    }
  }
  public static void resizeListString(ArrayList<String> list, int newSize) {
    int addingNum = newSize - list.size();
    for (int i = 0; i < addingNum; ++i) {
      list.add("");
    }
  }
  public static <E> void resizeListList(List<ArrayList<E>> list, int newSize) {
    int addingNum = newSize - list.size();
    for (int i = 0; i < addingNum; ++i) {
      list.add(new ArrayList<E>());
    }
  }
  public static <E> void resizeListWithNull(List<E> list, int newSize) {
    int addingNum = newSize - list.size();
    for (int i = 0; i < addingNum; ++i) {
      list.add(null);
    }
  }

  public static void normalize(double[] dist) {
    double A = 0;
    for (double p : dist) A += p;
    for (int i = 0; i < dist.length; ++i) dist[i] /= A;
  }

  public static int argmax(double[] dist) {
    double max = 0;
    int argmax = 0;
    for (int i = 0; i < dist.length; ++i) {
      if (dist[i] > max) {
        argmax = i;
        max = dist[i];
      }
    }
    return argmax;
  }
}
