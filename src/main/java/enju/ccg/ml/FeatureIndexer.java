package enju.ccg.ml;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.Externalizable;
import java.util.*;
import gnu.trove.THashMap;
import enju.ccg.util.Indexer;

public class FeatureIndexer implements Serializable {
  protected static final long serialVersionUID = 42L;
  
  static class ArrayedFeature implements Serializable {
    protected static final long serialVersionUID = 42L;
    
    Object[] local_values;
    byte size;
    int hash;

    public ArrayedFeature(int size, Object[] values) {
      this.local_values = values;
      this.size = (byte)size;
    }
    private ArrayedFeature() {}

    public ArrayedFeature clone() {
      ArrayedFeature f = new ArrayedFeature();
      f.size = size;
      f.local_values = new Object[size];
      for (byte i = 0; i < size; i++) {
        f.local_values[i] = local_values[i];
      }
      return f;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ArrayedFeature)) return false;
      ArrayedFeature f = (ArrayedFeature) o;
      if (size != f.size) return false;
      for (byte i = 0; i < size; i++) {
        if (!local_values[i].equals(f.local_values[i])) return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      if (hash != 0) return hash;
      hash = 5;
      for (byte i = 0; i < size; ++i) {
        hash = hash * 31 + local_values[i].hashCode();
      }
      return hash;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (byte i = 0; i < size; i++) {
        if (i > 0) sb.append("_###_");
        sb.append(local_values[i].toString());
      }
      return sb.toString();
    }
  }

  static class IndexerImpl extends Indexer<ArrayedFeature> implements Externalizable {
    protected static final long serialVersionUID = 42L;
    public IndexerImpl() { super(); }
    public IndexerImpl(int n) { super(n); }
    public boolean add(ArrayedFeature feature) {
      if (isLocked()) throw new IllegalStateException("Tried to add to locked indexer.");
      if (contains(feature)) return false;
      ArrayedFeature cloned = feature.clone();
      indexes.put(cloned, size());
      objects.add(cloned);
      return true;
    }

    // this is for faster deserialize
    public void addQuick(Object[] lv, int i, int hash) {
      ArrayedFeature f = new ArrayedFeature(lv.length, lv);
      f.hash = hash;
      indexes.put(f, i);
      objects.add(f);
    }
    public void writeExternal(ObjectOutput out) throws IOException {
      THashMap<Object, Object> canonicalFeature = new THashMap<Object,Object>();
      for (int i = 0; i < size(); ++i) {
        ArrayedFeature f = get(i);
        for (int j = 0; j < f.size; ++j) {
          Object o = f.local_values[j];
          if (!canonicalFeature.contains(o)) {
            canonicalFeature.put(o, o);
          }
          f.local_values[j] = o;
        }
      }
      indexes.writeExternal(out);
    }
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      indexes.readExternal(in);
      objects = new ArrayList<ArrayedFeature>(indexes.size());
      for (int i = 0; i < indexes.size(); ++i) objects.add(null);
      for (Map.Entry<ArrayedFeature,Integer> e : indexes.entrySet()) {
        objects.set(e.getValue(), e.getKey());
      }
    }
  }

  private Object[] globalValues;
  IndexerImpl impl;

  public FeatureIndexer() {
    impl = new IndexerImpl();
    globalValues = new Object[100];
  }

  public String getString(int index) {
    ArrayedFeature f = impl.get(index);
    return f.toString();
  }

  public int size() {
    return impl.size();
  }
  
  private ArrayedFeature generateFeature(int size) {
    ArrayedFeature f = new ArrayedFeature(size, globalValues);
    //for (int i = 0; i < size; ++i) globalValues[i] = globalValues[i].toString();
    return f;
  }
  public int getIndex(Object o0) {
    globalValues[0] = o0;
    return impl.getIndex(generateFeature(1));
  }
  public int getIndex(Object o0, Object o1) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    return impl.getIndex(generateFeature(2));
  }
  public int getIndex(Object o0, Object o1, Object o2) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    return impl.getIndex(generateFeature(3));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    return impl.getIndex(generateFeature(4));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3, Object o4) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    globalValues[4] = o4;
    return impl.getIndex(generateFeature(5));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3, Object o4, Object o5) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    globalValues[4] = o4;
    globalValues[5] = o5;
    return impl.getIndex(generateFeature(6));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3, Object o4, Object o5,
                      Object o6) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    globalValues[4] = o4;
    globalValues[5] = o5;
    globalValues[6] = o6;
    return impl.getIndex(generateFeature(7));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3, Object o4, Object o5,
                      Object o6, Object o7) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    globalValues[4] = o4;
    globalValues[5] = o5;
    globalValues[6] = o6;
    globalValues[7] = o7;
    return impl.getIndex(generateFeature(8));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3, Object o4, Object o5,
                      Object o6, Object o7, Object o8) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    globalValues[4] = o4;
    globalValues[5] = o5;
    globalValues[6] = o6;
    globalValues[7] = o7;
    globalValues[8] = o8;
    return impl.getIndex(generateFeature(9));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3, Object o4, Object o5,
                      Object o6, Object o7, Object o8, Object o9) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    globalValues[4] = o4;
    globalValues[5] = o5;
    globalValues[6] = o6;
    globalValues[7] = o7;
    globalValues[8] = o8;
    globalValues[9] = o9;
    return impl.getIndex(generateFeature(10));
  }
  public int getIndex(Object o0, Object o1, Object o2, Object o3, Object o4, Object o5,
                      Object o6, Object o7, Object o8, Object o9, Object o10) {
    globalValues[0] = o0;
    globalValues[1] = o1;
    globalValues[2] = o2;
    globalValues[3] = o3;
    globalValues[4] = o4;
    globalValues[5] = o5;
    globalValues[6] = o6;
    globalValues[7] = o7;
    globalValues[8] = o8;
    globalValues[9] = o9;
    globalValues[10] = o10;
    return impl.getIndex(generateFeature(11));
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    impl.writeExternal(out);
    
    // Indexer<Object> objectIndexer = new Indexer<Object>();
    // int[][] idx2arrayedFeatureInts = new int[impl.size()][];
    // int[] hashes = new int[impl.size()];
    // for (int i = 0; i < impl.size(); ++i) {
    //   ArrayedFeature f = impl.get(i);
    //   int[] fInts = new int[f.size];
    //   for (int j = 0; j < f.size; ++j) {
    //     fInts[j] = objectIndexer.getIndex(f.local_values[j]);
    //   }
    //   idx2arrayedFeatureInts[i] = fInts;
    //   hashes[i] = f.hash;
    // }
    // out.writeObject(objectIndexer);
    // //out.writeObject(idx2arrayedFeatureInts);
    
    // int serialedSize = 0;
    // for (int[] arrayedFeatureInts : idx2arrayedFeatureInts)
    //   serialedSize += 1 + arrayedFeatureInts.length;
    // int[] serialed = new int[serialedSize];
    // int i = 0;
    // for (int[] arrayedFeatureInts : idx2arrayedFeatureInts) {
    //   serialed[i++] = arrayedFeatureInts.length;
    //   for (int f : arrayedFeatureInts) {
    //     serialed[i++] = f;
    //   }
    // }
    // out.writeInt(idx2arrayedFeatureInts.length);
    // out.writeObject(serialed);
    
    // out.writeObject(hashes);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    System.out.println("feature loading...");
    globalValues = new Object[100];

    impl = new IndexerImpl();
    impl.readExternal(in);
    
    // Indexer<Object> objectIndexer = (Indexer<Object>)in.readObject();

    // int featureSize = in.readInt();
    // int[] serialed = (int[])in.readObject();
    // int[] hashes = (int[])in.readObject();
    // impl = new IndexerImpl(featureSize);

    // int i = 0;
    // for (int fi = 0; fi < featureSize; ++fi) {
    //   if (fi % 1000000 == 0) System.out.print(".");
    //   int size = serialed[i++];
    //   Object[] lv = new Object[size];
    //   for (int j = 0; j < size; ++j) {
    //     lv[j] = objectIndexer.get(serialed[i++]);
    //   }
    //   impl.addQuick(lv, fi, hashes[fi]);
    // }
    
    // // int[][] idx2arrayedFeatureInts = (int[][])in.readObject();
    // // int[] hashes = (int[])in.readObject();
    // // impl = new IndexerImpl(idx2arrayedFeatureInts.length);

    // // int i = 0;
    // // for (int[] arrayedFeatureInts : idx2arrayedFeatureInts) {
    // //   if (i % 1000000 == 0) System.out.print(".");
    // //   Object[] lv = new Object[arrayedFeatureInts.length];
    // //   for (int j = 0; j < lv.length; ++j) {
    // //     lv[j] = objectIndexer.get(arrayedFeatureInts[j]);
    // //   }
    // //   impl.addQuick(lv, i, hashes[i]);
    // //   ++i;
    // // }
    System.out.println("\ndone.");
  }
}