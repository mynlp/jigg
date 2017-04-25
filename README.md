# Jigg

Jigg is a natural language processing pipeline framework on JVM languages (mainly for Scala), which is easy to use and extensible. Using Jigg, one can obtain several linguistic annotations on a given input from POS tagging, parsing, and coreference resolution from command-lines. The main features include:

- Easy to install: basic components are included in a distributed single jar, so no need to install;
- Similar interface to [Stanford CoreNLP](http://nlp.stanford.edu/software/corenlp.shtml);
- Extensible: easy to add new component is a pipeline;
- Parallel processing: sentence-level annotation is automatically parallelized.

Jigg is distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

The core ideas and software designs are described in detail in [our paper](http://mynlp.github.io/jigg/data/jigg-acl2016.pdf).

## Install

There is no need to install. Just download the package!

```bash
$ wget https://github.com/mynlp/jigg/releases/download/v-0.6.2/jigg-0.6.2.tar.gz
$ tar xzf jigg-0.6.2.tar.gz
$ cd jigg-0.6.2
```

## Usage

The following command launches Jigg in a shell mode, which parses a given input with Berkeley parser after preprocessing (tokenization and sentence splitting).

```bash
$ java -cp "*" jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit],berkeleyparser"
[main] INFO edu.stanford.nlp.pipeline.StanfordCoreNLP - Adding annotator tokenize
...
>
```

Let's write some sentences in a line.

```xml
> Hello Jigg! This is the first sentence.
<root>
  <document id="d0">
    <sentences>
      <sentence id="s0" characterOffsetBegin="0" characterOffsetEnd="11">
        Hello Jigg!
        <tokens annotators="corenlp berkeleyparser">
          <token pos="UH" characterOffsetEnd="5" characterOffsetBegin="0" id="t0" form="Hello"/>
          <token pos="PRP$" characterOffsetEnd="10" characterOffsetBegin="6" id="t1" form="Jigg"/>
          <token pos="." characterOffsetEnd="11" characterOffsetBegin="10" id="t2" form="!"/>
        </tokens>
        <parse annotators="berkeleyparser" root="s0_berksp0">
          <span id="s0_berksp0" symbol="INTJ" children="t0 t1 t2"/>
        </parse>
      </sentence>
      <sentence id="s1" characterOffsetBegin="12" characterOffsetEnd="39">
        This is the first sentence.
        <tokens annotators="corenlp berkeleyparser">
          <token pos="DT" characterOffsetEnd="4" characterOffsetBegin="0" id="t3" form="This"/>
          <token pos="VBZ" characterOffsetEnd="7" characterOffsetBegin="5" id="t4" form="is"/>
          <token pos="DT" characterOffsetEnd="11" characterOffsetBegin="8" id="t5" form="the"/>
          <token pos="JJ" characterOffsetEnd="17" characterOffsetBegin="12" id="t6" form="first"/>
          <token pos="NN" characterOffsetEnd="26" characterOffsetBegin="18" id="t7" form="sentence"/>
          <token pos="." characterOffsetEnd="27" characterOffsetBegin="26" id="t8" form="."/>
        </tokens>
        <parse annotators="berkeleyparser" root="s1_berksp0">
          <span id="s1_berksp0" symbol="S" children="s1_berksp1 s1_berksp2 t8"/>
          <span id="s1_berksp1" symbol="NP" children="t3"/>
          <span id="s1_berksp2" symbol="VP" children="t4 s1_berksp3"/>
          <span id="s1_berksp3" symbol="NP" children="t5 t6 t7"/>
        </parse>
      </sentence>
    </sentences>
  </document>
</root>
>
```

The default output format of Jigg is XML (Jigg also supports JSON output, but it still has some issues). One can see that Jigg automatically detects sentence boundaries (there are two sentences), and performs tokenization (e.g, period . is recognized as a single word), on which parse tree (`<parse>`) is built.

In Jigg, each NLP tool such as `corenlp` (Stanford CoreNLP) or `berkeleyparser` (Berkeley parser) is called annotator. Jigg helps to construct easily a NLP pipeline by combining several annotators. In the example above, the pipeline is constructed by combining Stanford CoreNLP (which performs tokenization and sentence-splitting) and Berkeley parser (which performs parsing on tokenized sentences).

### Command-line usage

Basic usage is described in the help message:

```bash
$ java -cp "*" jigg.pipeline.Pipeline -help
Usage:
  outputFormat                   < str>: Output format, [xml/json]. Default value is 'xml'. [xml]
  annotators                     < str>: List of annotator names, e.g., corenlp[tokenize,ssplit],berkeleyparser (required) [ssplit,kuromoji,jaccg]
  checkRequirement               < str>: Check requirement, [true/false/warn]. Default value is 'true'. [true]
  file                           < str>: Input file; if omitted, read from stdin []
  props                          < str>: Property file []
  nThreads                       < int>: Number of threads for parallel annotation (use all if <= 0) [-1]
  output                         < str>: Output file; if omitted, `file`.xml is used. Gzipped if suffix is .gz. If JSON mode is selected, suffix is .json []
  customAnnotatorClass           < str>: You can add an abbreviation for a custom annotator class with "-customAnnotatorClass.xxx path.package" []
  help                           < str>: Print this message and descriptions of specified annotators, e.g., -help ssplit,mecab [true]

Currently the annotators listed below are installed. See the detail of each annotator with "-help annotator_name".

  mecab, ssplit, jaccg, cabocha, berkeleyparser, spaceTokenize, kuromoji, syntaxnetpos, dsplit, knp, corenlp, knpDoc, juman, syntaxnetparse, syntaxnet
```

Some annotators, such as mecab, jaccg, kuromoji, etc. are specific for Japanese processing. As shown here, more specific description for each annotator is described by giving argument to `-help` option:

```bash
$ java -cp "*" jigg.pipeline.Pipeline -help berkeleyparser
...
berkeleyparser:
  requires                             : [Tokenize]
  requirementsSatisfied                : [POS, Parse]

  berkeleyparser.variational     <bool>: Use variational rule score approximation instead of max-rule (Default: false) [false]
  berkeleyparser.grFileName      < str>: Grammar file []
  berkeleyparser.accurate        <bool>: Set thresholds for accuracy. (Default: set thresholds for efficiency) [false]
  berkeleyparser.usePOS          <bool>: Use annotated POS (by another annotator) [false]
  berkeleyparser.viterbi         <bool>: Compute viterbi derivation instead of max-rule tree (Default: max-rule) [false]

  A wrapper for Berkeley parser. The feature is that this wrapper is implemented to be
  thread-safe. To do this, the wrapper keeps many parser instances (the number can be
  specified by customizing -nThreads).

  The path to the model file can be changed by setting -berkeleyparser.grFileName.

  If -berkeleyparser.usePOS is true, the annotator assumes the POS annotation is already
  performed, and the parser builds a tree based on the assigned POS tags.
  Otherwise, the parser performs joint inference of POS tagging and parsing, which
  is the default behavior.
```

#### Requirements

Here, `requires` and `reqruiementsSatisfied` describe the role of this annotator (berkeleyparser). Intuitively, the above description says `berkeleyparser` requires that the input text is already tokenized (`Tokenize`), and after the annotation, part-of-speech tags (`POS`) and parse tree (`Parse`) are annotated on each sentence.

Jigg checks with these kinds of information whether the given pipeline can be performed safely. For example, the following command will be failed:

```bash
$ java -cp "*" jigg.pipeline.Pipeline -annotators berkeleyparser
annotator berkeleyparser requires Tokenize
  annotators                     < str>: List of annotator names, e.g., corenlp[tokenize,ssplit],berkeleyparser (required) [berkeleyparser]
```

The error message says `tokenize` should be performed before running `berkeleyparser`.

#### Parallel processing

In the help message above, we can see that `berkeleyparser` is implemented to be thread-safe. This means we can run Berkeley parser in parallel, which is not supported in the original software. The most of supported annotators in Jigg are implemented as thread-safe, meaning that annotation can be very efficient in multi-core environment.

To perform parallel annotation, first prepare an input document (whatever you want to analyze).

```bash
$ head input.txt
John Blair & Co. is close to an agreement to sell its TV station advertising representation operation and program production unit to an investor group led by James H. Rosenfield, a former CBS Inc. executive, industry sources said. Industry sources put the value of the proposed acquisition at more than $100 million. John Blair was acquired last year by Reliance Capital Group Inc., which has been divesting itself of John Blair's major assets. ...
```

Then run Jigg as follows:
```bash
$ java -cp "*" jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit],berkeleyparser" -file input.txt
```

Or you can run Jigg in pipe:
```bash
$ cat input.txt | java -cp "*" jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit],berkeleyparser" > output.xml
```

Parallelization can be prohibited by giving `-nThreads 1` option:
```bash
$ cat input.txt | java -cp "*" jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit],berkeleyparser" -nThreads 1 > output.xml
```

By default, Jigg tries to use as many threads as the machine can use. On my laptop (with 4 cores), when annotating about 1000 sentences, annotation with `-nThreads 1` takes about 154 seconds, which is reduced to 79 seconds with parallel annotation.

#### Full pipeline

For English, currently the main components in Jigg are Stanford CoreNLP. To run the full pipeline in Stanford CoreNLP, you need to download the model file of it first (if you don't have):
```bash
$ wget http://nlp.stanford.edu/software/stanford-english-corenlp-2016-01-10-models.jar
```

Then, a pipeline to the coreference resolution, for example, can be constructed as follows:

```bash
$ java -cp "*" jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit,parse,lemma,ner,dcoref]"
```

This is the usage of Jigg just as a wrapper of Stanford CoreNLP, which may not be interesting. More interesting example is to insert the Berkeley parser into a pipeline of Stanford CoreNLP:

```bash
$ java -cp "*" jigg.pipeline.Pipeline -annotators "corenlp[tokenize,ssplit],berkeleyparser,corenlp[lemma,ner,dcoref]"
```

This command replaces the parser component in a CoreNLP pipeline with Berkeley parser. Jigg alleviates to include a NLP tool into a pipeline. In future Jigg will support many existing NLP tools, and the goal is to provide a platform on which a user can freely connect the tools to construct several NLP pipelines.

### Programmatic usage

Jigg pipeline can also be incorporated another Java or Scala project. The easiest way to do this is add a dependency to Maven.

In Scala, add the following line in the project `build.sbt`.

```scala
libraryDependencies += "com.github.mynlp" % "jigg" % "0.6.2"
```

In Java, add the following lines on `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.github.mynlp</groupId>
    <artifactId>jigg</artifactId>
    <version>0.6.2</version>
  </dependency>
</dependencies>
```

Jigg is written in Scala, so Scala is the most preferable choice for a programmatic usage. Jigg provides a very similar interface to Stanford CoreNLP:

```scala
import jigg.pipeline.Pipeline
import java.util.Properties
import scala.xml.Node

// The behavior of pipeline can be customized with Properties object, which consists of the same options used in command-line usages.
val props = new Properties

props.setProperty("annotators", "corenlp[tokenize,ssplit],berkeleyparser,corenlp[lemma,ner,dcoref]")

// The path to the model to the Berkeley parser may be necessary.
props.setProperty("berkeleyparser.grFileName", "/path/to/eng_sm6.gr")

// Pipeline is the main class, which eats Properties object.
val pipeline = new Pipeline(props)

// Set the input text to be analyzed here.
val text: String = ...

// Get the annotation result in Scala's XML object (Node).
val annotation: Node = pipeline.annotate(text)
```

The annotation result is obtained in Scala XML object, on which elements can be searched intuitively with expressions similar to X-path. The followings are an example:

```scala
val sentences: Seq[Node] = annotation \\ "sentence" // Get all <sentence> elements.
for (sentence <- sentences) { // for each sentence
  val tokens = sentence \\ "token"  // get all tokens
  val nes = sentence \\ "NE"        // get all named entities
  for (ne <- nes) {
    val tokenIds = ne \@ "tokens"   // get the "tokens" attribute in a NE.

    val neTokens = tokenIds map { id =>
      tokens.find(_ \@ "id" == id).get \@ "form" // get surface form of each token consisting the NE
    }
    println(neTokens mkString " ")  // print the detected NE
  }
}
```

On the result XML, all annotated elements (e.g., `sentence`, `token`, and `NE`) are assigned unique ids. So element search is basically based on these ids.


## Supported annotators

TBA


## Implementing new annotator

TBA

## Citing in papers

If you use any of the parser models in research publications, please cite:

> Hiroshi Noji and Yusuke Miayo. 2016. [Jigg: A Framework for an Easy Natural Language Processing Pipeline](http://mynlp.github.io/jigg/data/jigg-acl2016.pdf). In Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics: System Demonstrations.

## Acknowledgements
Following sample files of SsplitKerasAnnotator/BunsetsuKerasAnnotator is generated by using [BCCWJ corpus](http://pj.ninjal.ac.jp/corpus_center/bccwj/).
- Trained model file
  - src/test/resources/data/keras/ssplit_model.h5
  - src/test/resources/data/keras/bunsetsu_model.h5
- Lookup table file
  - src/test/resources/data/keras/jpnLookupCharacter.json
  - src/test/resources/data/keras/jpnLookupWords.json

## Release note

- 0.6.1: Bug fixes.
- 0.6.1: New annotators (syntaxnet, coref in corenlp, etc); JSON output (still incomplete); bug fixes.
- 0.6.0: The initial official release.
