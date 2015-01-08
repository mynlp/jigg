# TransCCG

This is an integrated framework of Scala for CCG parser and other natural language processing components. The features include:

- Accurate CCG parser based on shift-reduce actions for Japanese and English.
- Extensible pipeline framework similar to Stanford CoreNLP.

The project name (transccg) is temporary and probably changed. I'm looking for other fancy name.

## How to use

The current version is 0.3. You can use `jar/transccg.jar`, which is a self-contained jar of the current version.

### Preparing a pre-trained model

To run a CCG parser for a raw text, a pre-trained model file is required, which can be downloaded with the script:

```bash
./script/download_model.sh
```

This downloads a jar file containing several models with differnt speed/accuracy in `jar` directory.

### Command line usage

To get CCG parses from raw sentences, a pipeline module `enju.pipeline.Pipeline` is useful. You can use it both from your Java or Scala code or command line. For example, the command below reads sentences from `sample.txt` and output to `sample.txt.xml` in XML format.

```bash
$ cat sample.txt
Scalaは良い言語です。
文法がとても簡潔です。
$ java -Xmx4g -cp "jar/*" enju.pipeline.Pipeline -annotators ssplit,kuromoji,ccg -file sample.txt -ccg.numKbest 3
```

The syntax is very similar to Stanford CoreNLP. The required arguments are as follows:

  * `-annotators`: A list of annotator names. See below for the currently supported annotators.
  * `-file`: Input file. If you omit this, it runs as an interactive shell mode.

In the above command, `-annotators ssplit,kuromoji,ccg` means it first splits input string into sentences, tokenizes, assigns part-of-speech tags, and then runs the CCG parser on that tokenized sentences. The CCG parser requires the tokenized sentence as input. The pipeline automatically solves these dependencies between annotators.

#### Annotator list

  * `ssplit`: This is always the first annotator. It segments a give text with some rule. You can customize the behavior by giving a regex with an argument `-ssplit.pattern`. See Argument list for more details.
  * `kuromoji`: This annotator requires the input has been segmented with `ssplit`. It is morphological analyzer, which tokenizes each sentence and assign a PoS to each token.
  * `ccg`: This annotator requires the input has been tokenized. It gives CCG derivation for each sentence.

#### Argument list

Some annotator's behavior can be customized by giving some arguments. These arguments are prefixed with each annotator name. For example, an argument for the CCG parser starts with `-ccg.`. The below are examples of arguments.

  * `-ssplit.pattern`: You can write some regular expression here for a rule of sentence segmentation. The default value is `"\n+|。\n*"` that means it segments by each new line or a period (。).
  * `-ssplit.method`: When `-split.pattern` is ommited, you can pick up some pre-defined pattern by setting this value. Currently `newLine`, `point`, and `pointAndNewLine` are supported. For example, when `-spplit.method newLine` is given, it segments sentences by each new line. `point` only segments by each period. The default value is `pointAndNewLine`, which is explained in `-ssplit.pattern` above.
  * `-ccg.model`: Please set this if you want to use your own trained model for the CCG parser. If not set, a default model of `model/jaccg-0.2-beam64.ser.gz` is selected, which is a pre-trained model with beam size of 64. Its accuracy is 88% on Kyoto-university corpus and the speed is 2~3 sentences per second.
  * `-ccg.beam`: If you use the default model, you don't have to change this value. It is the beam size of the parser. The default value is 64 and is compatible with the default model of the above.
  * `-ccg.numKbest`: If you set this value > 1, k-best derivations of the CCG parser are annotated with those scores.

### Output format

The result is written in `sample.txt.xml`, which looks like this:

```bash
$ cat sample.txt.xml
<?xml version='1.0' encoding='UTF-8'?>
<root>
  <document>
    <sentences>
      <sentence id="s0">
        Scalaは良い言語です。
        <tokens>
          <token base="*" inflectionForm="*" inflectionType="*" pos3="*" pos2="組織" pos1="固有名詞" pos="名詞" surf="Scala" id="s0_0"/>
          <token reading="ワ" base="は" inflectionForm="*" inflectionType="*" pos3="*" pos2="*" pos1="係助詞" pos="助詞" surf="は" id="s0_1"/>
          <token reading="ヨイ" base="良い" inflectionForm="基本形" inflectionType="形容詞・アウオ段" pos3="*" pos2="*" pos1="自立" pos="形容詞" surf="良い" id="s0_2"/>
          <token reading="ゲンゴ" base="言語" inflectionForm="*" inflectionType="*" pos3="*" pos2="*" pos1="一般" pos="名詞" surf="言語" id="s0_3"/>
          <token reading="デス" base="です" inflectionForm="基本形" inflectionType="特殊・デス" pos3="*" pos2="*" pos1="*" pos="助動詞" surf="です" id="s0_4"/>
          <token reading="。" base="。" inflectionForm="*" inflectionType="*" pos3="*" pos2="*" pos1="句点" pos="記号" surf="。" id="s0_5"/>
        </tokens>
        <ccg score="617.4788799285889" id="s0_ccg0" root="s0_sp0">
          <span child="s0_sp1 s0_sp11" rule="&lt;" category="S[mod=nm,form=base]" end="6" begin="0" id="s0_sp0"/>
          <span child="s0_sp2 s0_sp10" rule="&lt;" category="S[mod=nm,form=base]" end="5" begin="0" id="s0_sp1"/>
          <span child="s0_sp3 s0_sp9" rule="&gt;" category="NP[mod=nm,case=nc]" end="4" begin="0" id="s0_sp2"/>
          <span child="s0_sp4" rule="ADN" category="NP[case=nc]/NP[case=nc]" end="3" begin="0" id="s0_sp3"/>
          <span child="s0_sp5 s0_sp8" rule="&lt;" category="S[mod=adn,form=base]" end="3" begin="0" id="s0_sp4"/>
          <span child="s0_sp6 s0_sp7" rule="&lt;" category="NP[mod=nm,case=ga]" end="2" begin="0" id="s0_sp5"/>
          <span terminal="s0_0" category="NP[mod=nm,case=nc]" end="1" begin="0" id="s0_sp6"/>
          <span terminal="s0_1" category="NP[mod=nm,case=ga]\NP[mod=nm,case=nc]" end="2" begin="1" id="s0_sp7"/>
          <span terminal="s0_2" category="S[mod=adn,form=base]\NP[mod=nm,case=ga]" end="3" begin="2" id="s0_sp8"/>
          <span terminal="s0_3" category="NP[mod=nm,case=nc]" end="4" begin="3" id="s0_sp9"/>
          <span terminal="s0_4" category="S[mod=nm,form=base]\NP[mod=nm,case=nc]" end="5" begin="4" id="s0_sp10"/>
          <span terminal="s0_5" category="S\S" end="6" begin="5" id="s0_sp11"/>
        </ccg>
        <ccg score="615.8624420166016" id="s0_ccg1" root="s0_sp12">
          ...
        </ccg>
      </sentence>
      <sentence id="s1">
      ...
      </sentence>
    </sentences>
  </document>
</root>
```

Again, the result looks very similar to the output of Stanford CoreNLP. Version 0.2 supports k-best output. Each `ccg` element has `score` attribute. Higher value is better for the model. Each ccg tree has a `root` attribute, which points to the span id of the root node. Sometimes the parser returns a parse forest, i.e., fragmental trees, in which case, `root` is a list of root nodes of all fragments.

The CCG parse tree is represented as a set of spans. Each span has following attributes:

  * `begin, end`: The range of the span is `[begin, end)`. `end` is exclusive, e.g., a span of `begin="4" end="5"` is a leaf (pre-terminal) node for the word of index 4 in the sentence.
  * `rule`: Used rule. For example, `"&lt;"` (<) indicates forward application is used.
  * `category`: CCG category which corresponds to non-terminal label in each span.
  * `child`: If a node is non-terminal, `child` lists child nodes' ids. Two children are separated with a space if the rule is binary.
  * `terminal`: If a node is pre-terminal (leaf in the derivation tree), `terminal` points to the id of corresponding token in the sentence.

### Server mode

See `script/pipeline_server.py` for running a server. An example of client is found in `script/client.py`.


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
