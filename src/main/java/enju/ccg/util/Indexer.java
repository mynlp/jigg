package enju.ccg.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import gnu.trove.THashMap;

/**
 * 
 * @author Katsuhiko Hayashi
 * @author Hiroshi Noji
 * 
 */
public class Indexer<E> implements Serializable {
  protected static final long serialVersionUID = 55887485075874094l;
  protected static final int CURRENT_SERIAL_VERSION = 0;

  protected List<E> objects;
  //protected Map<E, Integer> indexes;
  protected THashMap<E,Integer> indexes;
      
  private boolean stop = false;

  public Indexer() {
    objects = new ArrayList<E>();
    //indexes = new ConcurrentHashMap<E, Integer>();
    indexes = new THashMap<E, Integer>();
  }

  public Indexer(int n) {
    objects = new ArrayList<E>(n * 4 / 3);
    //indexes = new ConcurrentHashMap<E, Integer>(n * 4 / 3);
    indexes = new THashMap<E, Integer>(n * 4 / 3);
  }

  public boolean contains(Object obj) {
    return indexes.containsKey(obj);
  }

  public boolean add(E object) {
    if (stop) throw new IllegalStateException("Tried to add to locked indexer.");
    if (contains(object)) return false;
    indexes.put(object, size());
    objects.add(object);
    return true;
  }
  
  // /**
  //  * WARNING: take time proportional to the num of elems O(N)
  //  */
  // public void remove(E object) {
  //   int idx = indexOf(object);
  //   if (idx == -1) return;
  //   indexes.remove(object);
  //   objects.remove(idx);
  // }

  public void clear() {
    objects.clear();
    indexes.clear();
  }

  public int indexOf(Object obj) {
    Integer index = indexes.get(obj);
    if (index == null) return -1;
    return index;
  }

  public int size() {
    return objects.size();
  }

  public boolean isLocked() {
    return stop;
  }

  public void lock() {
    stop = true;
  }

  public void unlock() {
    stop = false;
  }

  public int getIndex(E object) {
    if (object == null) return -1;
    Integer index = indexes.get(object);
    if (index == null) {
      if (stop) return -1;
      index = size();
      add(object);
    }
    return index;
  }

  public E get(int index) {
    return objects.get(index);
  }

  public Iterator<E> iterator() {
    return indexes.keySet().iterator();
  }

  public Collection<Integer> getValues() {
    return indexes.values();
  }

  public Collection<E> getKeySet() {
    return indexes.keySet();
  }
}
