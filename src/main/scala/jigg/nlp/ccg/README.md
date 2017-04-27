## Japanese CCG parser

After obtaining a model, I recommend to use Jigg interface for obtaining the output parses for new sentences. Here I describe how to train and evaluate new model given the Japanese CCGBank (Uematsu et al. 2013).

### Train Super tagger

``` shell
$ java -Xmx8g -cp jigg.jar jigg.nlp.ccg.TrainSuperTagger \
  -bank.lang ja \
  -bank.dir ccgbank \
  -model tagger.ser.gz
```

Here `ccgbank` is the path to CCBank directory, which contains, e.g., `train.ccgbank` and `Japanese.lexicon`.
`jigg.jar` should point to the correct path to the assembled jar.
All parameters are set to the defualt values, which work reasonablly well, but if you wish to customize the parameters, the following command outputs the list of all parameters.
(This behavior is common across all classes described below.)

```sh
$ java -Xmx8g -cp jigg.jar jigg.nlp.ccg.TrainSuperTagger -help
```


### Train parser

After obtaining the super tagger, the parser model can be obtained with the following command:

``` shell
$ java -Xmx10g -cp jigg.jar jigg.nlp.ccg.TrainParser \
  -taggerModel tagger.ser.gz
  -model parser.ser.gz
  -beam 64 \
  -bank.lang ja \
  -bank.dir ccgbank \
```

Here `beam` controls the beam size of the model.
`64` achieves good balance between accuracy and speed, but you can obtain a faster model (~2x) with `-beam 32` by sacrificing accuracy a bit.
It may require relatively larger memory.
Here `-Xmx10g` might be too much but it is safe if the machine allows.
This command may take around half or one day to finish.

After obtaining parser model (`parser.ser.gz`), you can use it throw Jigg in the following way.

``` shell
$ java -Xmx8g -cp jigg.jar -annotators "ssplit,kuromoji,ccg" -ccg.model parser.ser.gz -ccg.beam 64
```

Here I recommend to set the same value of `ccg.beam` as the setting in training.


### Evaluation

Currently the main evaluation metric for this parser is syntactic dependency accuracy against bunsetsu dependencies.
    Though this is not perfect for evaluating CCG outputs, it is easy, and in future we plan to support evaluation in more semantically oritented metric, e.g., CCG predicate-argument dependencies.

For evaluation, first, one needs to obtain the file `test.cabocha`, which is the output of CaboCha on test sentences of CCGBank.

``` shell
$ head test.cabocha
* 0 1D 2/3 2.170270
東京	名詞,固有名詞,地域,一般,*,*,東京,トウキョウ,トーキョー
・	記号,一般,*,*,*,*,・,・,・
上野	名詞,固有名詞,地域,一般,*,*,上野,ウエノ,ウエノ
の	助詞,連体化,*,*,*,*,の,ノ,ノ
* 1 5D 2/3 -1.570997
不	接頭詞,名詞接続,*,*,*,*,不,フ,フ
...
```

If you don't have this file, it can be obtained with the following commannd:

``` shell
$ java -cp jigg.jar jigg.nlp.ccg.CCGBankToCabochaFormat -ccgbank ccgbank/test.ccgbank -output test.cabocha
```

Note that this command internally calls CaboCha, so it must be installed in the system.
Then, the command below performs evaluation.

``` shell
$ java -Xmx8g -cp jigg.jar jigg.nlp.ccg.EvalJapaneseParser \
       -model parser.ser.gz \
       -decoder.beam 64 \
       -output out \
       -bank.dir ccgbank \
       -useTest true \
       -cabocha test.cabocha'
```

#### CoNLL format output

The command above reports the accuracies on the standard output.
`out` is the output directory, which contains the result parse in several formats.
Among them, `out/pred.conll` is the result in [CoNLL dependency format](http://ilk.uvt.nl/conll/#dataformat) on bunsetsu-unit, rather than word.

One can compare this output with the gold dependency data using the standard tools for CoNLL format data.
For this, the gold treebank data (e.g., `test.ccgbank`) should also be converted into CoNLL format.
This can be done by:

``` shell
$ cat ccgbank/test.ccgbank | java -cp jigg.jar jigg.nlp.ccg.GoldBunsetsuDepInCoNLL -cabocha test.cabocha > test.conll
```

Then, `test.conll` is the CoNLL format file on bunsetsu unit, which can be compared with `out/pred.conll`.

``` shell
$ head test.conll
1	東京・上野の	_	_	名詞-固有名詞-地域-一般/_|記号-一般/_|名詞-固有名詞-地域-一般/_|助詞-連体化/_	_	2	_	_	_
2	不忍池で、	_	_	接頭詞-名詞接続/_|名詞-一般/_|名詞-一般/_|助動詞/連用形|記号-読点/_	_	6	_	_	_
3	無残な	_	_	名詞-形容動詞語幹/_|助動詞/体言接続	_	4	_	_	_
4	姿の	_	_	名詞-一般/_|助詞-連体化/_	_	5	_	_	_
5	鳥が	_	_	名詞-一般/_|助詞-格助詞-一般/_	_	6	_	_	_
6	目立つ。	_	_	動詞-自立/基本形|記号-句点/_	_	0	_	_	_
...
```

### References
* Sumire Uematsu, Takuya Matsuzaki, Hiroki Hanaoka, Yusuke Miyao, and Hideki Mima. 2013. [Integrating Multiple Dependency Corpora for Inducing Wide-coverage Japanese CCG Resources](http://www.aclweb.org/anthology/P13-1103). In ACL.

