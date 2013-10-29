// package enju.ccg.ml;

// import enju.ccg.util.Option;
// import enju.ccg.util.OptionParser;
// import enju.ccg.util.Indexer;
// import enju.ccg.util.Pair;
// import enju.ccg.util.Util;
// import enju.ccg.util.FileUtility;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.Collections;

// /**
//  * A simple test for classifiers in ml package
//  */
// public class MultiClassClassificationProblem {
//   public static class Options {
//     @Option(name = "-train_file", required = true, usage = "Training data from file \n")
//     public String trainfilename;
//     @Option(name = "-test_file", required = true, usage = "Test data from file \n")
//     public String testfilename;
//   }
//   static class WrappedExamples {
//     String gold;
//     List<Example<String>> examples;
//     WrappedExamples(String gold, List<Example<String>> examples) {
//       this.gold = gold; this.examples = examples;
//     }
//   }
//   static class Learner {
//     FeatureIndexer indexer;
//     HashSet<String> labels;
//     AbstractClassifier<String> classifier;

//     Learner() {
//       indexer = new FeatureIndexer();
//     }
//     void setClassifier(AbstractClassifier<String> classifier) { this.classifier = classifier; }
    
//     // assuming each line corresponds to a example of the format:
//     // labelName feat1 feat2 feat3 ...
//     List<WrappedExamples> readExamples(String fn, boolean train) throws IOException {
//       List<WrappedExamples> target = new ArrayList<WrappedExamples>();
//       List<Pair<String, List<String>>> data = readData(fn);
//       if (train) {
//         labels = getLabels(data);
//       }
//       for (Pair<String, List<String>> datum : data) {
//         List<Example<String>> examples = new ArrayList<Example<String>>();
//         for (String label : labels) {
//           Example<String> e = new Example<String>(label);
//           List<Integer> feature = new ArrayList<Integer>();
//           feature.add(indexer.getIndex(label)); // bias
//           for (String f : datum.getP2()) {
//             feature.add(indexer.getIndex(f, label));
//           }
//           e.setFeature(feature);
//           examples.add(e);
//         }
//         target.add(new WrappedExamples(datum.getP1(), examples));
//       }
//       return target;
//     }
//     List<Pair<String, List<String>>> readData(String fn) throws IOException {
//       List<Pair<String, List<String>>> data = new ArrayList<Pair<String, List<String>>>();
//       BufferedReader br = FileUtility.getReadFileStream(fn, "UTF-8");
//       String line;
//       while ((line = br.readLine()) != null) {
//         line = line.trim();
//         if (line.isEmpty()) continue;
//         String[] seg = line.split("\\s");
//         String label = seg[0];
//         List<String> feats = new ArrayList<String>();
//         for (int i = 1; i < seg.length; ++i) feats.add(seg[i]);
//         data.add(Pair.makePair(label, feats));
//       }
//       return data;
//     }
//     HashSet<String> getLabels(List<Pair<String, List<String>>> data) {
//       HashSet<String> labels = new HashSet<String>();
//       for (Pair<String, List<String>> datum : data) {
//         labels.add(datum.getP1());
//       }
//       return labels;
//     }
//     void trainWithTest(List<WrappedExamples> train,
//                        List<WrappedExamples> test) {
//       for (int i = 0; i < 100; ++i) {
//         double trainloss = predict(train, true);
//         double testloss = predict(test, false);
//         System.out.println(i + "\ttrain: " + trainloss + "\ttest: " + testloss);
//       }
//     }
//     /**
//      * @return loss
//      */
//     double predict(List<WrappedExamples> data, boolean train) {
//       int wrong = 0;
//       ArrayList<Integer> order = Util.toList(Util.range(data.size()));
//       Collections.shuffle(order);
//       for (Integer k : order) {
//         String gold = data.get(k).gold;
//         List<Example<String>> examples = data.get(k).examples;
//         String pred = classifier.predict(examples).getP1();
//         if (!gold.equals(pred)) wrong++;
//         if (train) {
//           classifier.update(examples, gold);
//         }
//       }
//       return wrong / (double)data.size();
//     }
//   }
    
//   public static void main(String[] args) {
//     OptionParser optionparser = new OptionParser(Options.class);
//     Options options = (Options) optionparser.parse(args);

//     Learner learner = new Learner();
//     List<WrappedExamples> train = null;
//     List<WrappedExamples> test = null;
//     try {
//       train = learner.readExamples(options.trainfilename, true);
//       test = learner.readExamples(options.testfilename, false);
//     } catch (IOException e) {
//       e.printStackTrace();
//       System.exit(0);
//     }
//     int N = train.size();

//     System.out.println("# training examples:" + N);
//     System.out.println("# labels:" + learner.labels.size());
//     System.out.println("# features:" + learner.indexer.size());

//     learner.setClassifier(getLogisticSGD(N));
//     //learner.setClassifier(getPerceptron());
//     learner.trainWithTest(train, test);
//   }

//   static AbstractClassifier<String> getLogisticSGD(int N) {
//     WeightVector weight = new WeightVector();
//     //WeightVector weight = new Feature2LabelWeightVector();
//     //WeightVector weight = new Label2FeatureWeightVector();
//     LogisticSGD.StepSize stepSize =
//         new LogisticSGD.StepSize2(1, 3, N); // stole from Liang, Daume and Klein (2008)
//     return new LogisticSGD<String>(N, weight, stepSize);
//     //return new L1LogisticFOBOS(1, N, weight, stepSize);
//   }
  
//   static AbstractClassifier<String> getPerceptron() {
//     WeightVector weight = new WeightVector();
//     return new Perceptron<String>(weight);
//   }
// }