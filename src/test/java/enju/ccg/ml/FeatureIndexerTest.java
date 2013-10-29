package enju.ccg.ml;

import org.junit.*;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class FeatureIndexerTest {
  @Test
  public void toStringTest() throws Exception {
    FeatureIndexer indexer = new FeatureIndexer();
    indexer.getIndex(1,2,3);
    assertThat(indexer.getString(0), is("1_###_2_###_3"));
  }

  @Test
  public void hashTest() throws Exception {
    FeatureIndexer indexer = new FeatureIndexer();
    String template1 = "pos_0_-1_1";
    String template2 = "pos_0_-1_1";
    
    indexer.getIndex(template1, 1, 10, 5);
    indexer.getIndex(template1, 3, 2, 2);
    assertThat(indexer.size(), is(2));
    indexer.getIndex(template2, 1, 10, 5);
    assertThat(indexer.size(), is(2));
  }
}