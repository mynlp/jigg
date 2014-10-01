# TransCCG

This is a integrated framework of Scala for CCG parser and other natural language processing components. The features include:

- Accurate CCG parser based on shift-reduce actions for Japanese and English.
- Extensible pipeline framework similar to Stanford CoreNLP.

The project name (transccg) is temporary and probably changed. I'm looking for other fancy name.

## How to use

The current version is 0.1. You can use `transccg-0.1.jar` in the project root directory, which is a self-contained jar of the current version.

### Praparing pre-trained models

To run a CCG parser for new text, a pre-trained model file is required, which can be downloaded with the script:

```bash
./script/download_model.sh
```

This downloads some models in the `model` directory.

### Command line usage

To get CCG parses from raw sentences, a pipeline module `enju.pipeline.Pipeline` is useful. You can use it both from your Java or Scala code or command line. For example, the command below reads sentences from `sample.txt` and output as XML format in `sample.txt.xml`.

```bash
$ cat sample.txt
Scalaは良い言語です。
文法がとても簡潔です。
$ java -Xmx4g -cp transccg-0.1.jar enju.pipeline.Pipeline -annotators kuromoji,ccg -file sample.txt -ccg.model model/jaccg-0.1-beam64.ser.gz -ccg.beam 64
```

The syntax is very similar to Stanford CoreNLP. The required arguments are as follows:

  * `-annotators`: The list of annotator names. Currently, only `kuromoji` and `ccg` is supported.
  * `-file`: Input file, in which each sentence corresponds to one line.

In the above command, `-annotators kuromoji,ccg` means it first tokenizes and assigns part-of-speech tags, then runs the CCG parser on that tokenized sentence. The CCG parser requires the tokenized sentence as input and the pipeline automatically solve these dependencies between annotators.

`-ccg.*` is argument for the CCG parser. `-ccg.model` is necessary. `./model/jaccg-0.1-beam64.ser.gz` is a pre-trained model with beam size of 64. Another model, `./model/jaccg-0.1-beam128.ser.gz`, is trained with beam size of 128. You can change the beam size at run time by `-ccg.beam`. It is *highly recommended* to set the same beam size as training. So if you use `./model/jaccg-0.1-beam128.ser.gz`, please set `-ccg.beam 128`. The accuracy of the model of beam size 128 is slightly high, but is about 2 times slower. In Kyoto-university-corpus experiment, the accuracy of beam size 64 is 0.880, which become 0.884 with beam size 128.

### Output format

The result is written in `sample.txt.xml`, which is like this:

```bash
$ cat sample.txt.xml
<?xml version='1.0' encoding='UTF-8'?>
<sentences>
  <sentence id="s0">
    <tokens>
      <token base="*" pos="名詞-固有名詞-組織" surf="Scala" id="t0_0"/>
      <token base="は" pos="助詞-係助詞" surf="は" id="t0_1"/>
      <token base="良い" katsuyou="基本形" pos="形容詞-自立" surf="良い" id="t0_2"/>
      <token base="言語" pos="名詞-一般" surf="言語" id="t0_3"/>
      <token base="です" katsuyou="基本形" pos="助動詞" surf="です" id="t0_4"/>
      <token base="。" pos="記号-句点" surf="。" id="t0_5"/>
    </tokens>
    <ccg>
      <span child="sp0-1,sp0-11" rule="&lt;" category="S[mod=nm,form=base]" end="6" begin="0" id="sp0-0"/>
      <span child="sp0-2,sp0-5" rule="&gt;" category="S[mod=nm,form=base]" end="5" begin="0" id="sp0-1"/>
      <span child="sp0-3,sp0-4" rule="&lt;" category="S/S" end="2" begin="0" id="sp0-2"/>
      <span category="NP[mod=nm,case=nc]" end="1" begin="0" id="sp0-3"/>
      <span category="(S/S)\NP[mod=nm,case=nc]" end="2" begin="1" id="sp0-4"/>
      <span child="sp0-6,sp0-10" rule="&lt;" category="S[mod=nm,form=base]" end="5" begin="2" id="sp0-5"/>
      <span child="sp0-7,sp0-9" rule="&gt;" category="NP[mod=nm,case=nc]" end="4" begin="2" id="sp0-6"/>
      <span child="sp0-8" rule="ADN" category="NP[case=nc]/NP[case=nc]" end="3" begin="2" id="sp0-7"/>
      <span category="S[mod=adn,form=base]" end="3" begin="2" id="sp0-8"/>
      <span category="NP[mod=nm,case=nc]" end="4" begin="3" id="sp0-9"/>
      <span category="S[mod=nm,form=base]\NP[mod=nm,case=nc]" end="5" begin="4" id="sp0-10"/>
      <span category="S\S" end="6" begin="5" id="sp0-11"/>
    </ccg>
  </sentence>
  <sentence id="s1">
  ...
  </sentence>
</sentences>
```

Again, the result looks very similar to the output of Stanford CoreNLP. The CCG parse tree is represented as a set of spans. Each span has following attributes:

  * `begin, end`: The range of the span is `[begin, end)`. `end` is exclusive, e.g., a span of `begin="4" end="5"` is a leaf (pre-terminal) node for the word of index 4 in the sentence.
  * `rule`: Used rule. For example, `"&lt;"` indicates forward application is used.
  * `category`: CCG category which corresponds to non-terminal label in each span.
  * `child`: If a node is non-terminal, `child` lists child nodes' ids. If a rule is binary, two children are concatenated with comma.


The below is explanations for program build and training new model, but is out of date. I will revise it soon.

ビルド方法
-----

ビルドシステムはsbtを使用しています。
http://www.scala-sbt.org
sbtの実行ファイルは添付していますので、特に用意は必要なくビルドが可能です。
Scalaのバージョンは2.10.2が必要です。以下の方法で付属のsbtを使用する場合、問題はありませんが、マシンにインストールされたScalaを用いる場合は、2.10系のアップデートしてください。

プロジェクトのルートディレクトリで `bin/sbt` と実行すると、sbtのプロンプトが開きます。そのから、以下のようなコマンドで操作します。

    > compile  # main部分のソースのコンパイル
    > test     # 全てのテストを実行
    > assembly # 実行可能なjarをproject/ 以下に生成

Tagger, Parserなど全て、Driverと呼ばれるクラスを通して実行されます。このクラスのメイン関数は、assemblyで作られたjarファイルでデフォルトで呼ばれるので、今のコマンドで、全ての操作ができます。

    > java -Xmx8g -jar /path/to/jar

現在コマンドラインの解析に、fig (https://github.com/percyliang/fig) を使用しています。有効なオプションは、`-help`をつけて

    > java -Xmx8g -jar /path/to/jar

で一覧を見ることができます。

使い方
-----

以下、CCGBankから、Super-taggerの訓練、モデルの保存、Shift-reduce parserの訓練、評価の手順を説明します。

まずレポジトリのCCGBankを解凍してください。その上で、以下のコマンドで、CCGBankの訓練データからSuper-taggerを訓練します。

    > java -Xmx8g -jar /path/to/jar -modelType tagger -actionType train -bankDirPath ccgbank-20130828 -saveModelPath tagger.out -numIters 10 -lookupMethod surfaceAndSecondWithConj

引数は以下のようになります。

 - modelType, actionType: これらは必須で、プログラムの動作を決定します。現在はSuper-taggerの訓練を行うため、それぞれtagger,trainを選択します。
 - bankDirPath: これが設定されていれば、このディレクトリの中にあるtrain/dev/辞書ファイルを自動で読み込みます。
 - lookupMethod: 訓練時の設定の一つで、辞書の引き方を決めます。辞書ファイルから、訓練文の各単語に対してカテゴリの候補を集めますが、その際に何をキーとして辞書を引くか、を設定します。'surfaceAndSecondWithConj' は、各単語について、(表層系（タイプ）, 品詞階層の二階層目, 活用)の三つ組みを使用して候補を引くことを表しています。他の設定は、'-help'で確認できます。

以上により、Super-taggerのモデルが、'tagger.out' に出力されます。このモデルを用い、Super-taggerの性能を評価する場合、以下を行います。

    > java -Xmx8g -jar /path/to/jar -modelType tagger -actionType evaluate -bankDirPath ccgbank-20130828 -numIters 10 -beta 0.1 -loadModelPath tagger.out -outputPath develop.tagged.txt

loadModelPathで、先ほど保存したモデルを指定しています。'-beta 0.1' は、カテゴリの候補を複数決める場合の閾値を設定するための変数です。各予測は、ロジスティック回帰の局所的な多値分類で行われますが、 "最も高い確率の予測*beta" まで候補を取り出します。'-outputPath' は、予測したカテゴリの候補をテキスト形式で出力します。

保存したTaggerのモデルを用いて、Parserの訓練を行います。

    > java -Xmx8g -jar /path/to/jar -modelType parser -actionType train -bankDirPath ccgbank-20130828 -saveModelPath parser.out -numIters 5 -loadModelPath tagger.out -beam 8 -beta 0.1

モデルはBeam searchに基づく大域的なStructured perceptronです。'-beam 8' は、beam幅を表します。現在素性は、Yue Zhangの論文のものの一部のみを用いています。素性は自分で設計して簡単に追加することができます。(TBD)

訓練したモデルを用いて、デベロップ用データで評価します。現在、Dependencyへの変換が行えないので、CCGBankに似た形式で、予測した木を出力しています。その際、カテゴリは、Parserが内部で保持する情報以外は失われています。例えば、"S1\S1"というカテゴリは、"S\S"と出力されます。また、導出した木に割り当てられたカテゴリの精度を出力します。

    > java -Xmx8g -jar /path/to/jar -modelType parser -actionType evaluate -loadModelPath parser.out -beam 8 -beta 0.1 -outputPath develop.parsed.txt -bankDirPath ccgbank-20130828

ここでも 'outputPath' で、最終的な出力のファイルを指定しています。
