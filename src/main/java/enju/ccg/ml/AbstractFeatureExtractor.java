package enju.ccg.ml;
import java.util.List;

/**
 * @author Hiroshi Noji
 */
public abstract class AbstractFeatureExtractor<E> {
  protected FeatureIndexer featIndexer;

  public abstract List<Integer> extract(E e);
}
