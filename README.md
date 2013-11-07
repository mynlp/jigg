Enju/CCG
========

Shift-reduceに基づくCCG Parserです。

ビルドシステムはsbtを使用しています。
http://www.scala-sbt.org
sbtの実行ファイルは添付していますので、特に用意は必要なくビルドが可能です。
Scalaのバージョンは2.10.2が必要です。以下の方法で付属のsbtを使用する場合、問題はありませんが、マシンにインストールされたScalaを用いる場合は、2.10系のアップデートしてください。

プロジェクトのルートディレクトリで `bin/sbt` と実行すると、sbtのプロンプトが開きます。そのから、以下のようなコマンドで操作します。

    > compile  # main部分のソースのコンパイル
    > test     # 全てのテストを実行
    > assembly # 実行可能なjarをproject/ 以下に生成

Tagger, Parserなど全て、Driverと呼ばれるクラスを通して実行されます。このクラスのメイン関数は、assemblyで作られたjarファイルでデフォルトで呼ばれるので、今のコマンドで、全ての操作ができます。

    > java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar

現在コマンドラインの解析に、fig (https://github.com/percyliang/fig) を使用しています。有効なオプションは、`-help`をつけて

    > java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -help

で一覧を見ることができます。

使い方
-----

以下、CCGBankから、Super-taggerの訓練、モデルの保存、Shift-reduce parserの訓練、評価の手順を説明します。

まずレポジトリのCCGBankを解凍してください。その上で、以下のコマンドで、CCGBankの訓練データからSuper-taggerを訓練します。

    > java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -modelType tagger -actionType train -bankDirPath ccgbank-20130828 -saveModelPath tagger.out -numIters 10 -lookupMethod surfaceAndSecondWithConj

引数は以下のようになります。

 - modelType, actionType: これらは必須で、プログラムの動作を決定します。現在はSuper-taggerの訓練を行うため、それぞれtagger,trainを選択します。
 - bankDirPath: これが設定されていれば、このディレクトリの中にあるtrain/dev/辞書ファイルを自動で読み込みます。
 - lookupMethod: 訓練時の設定の一つで、辞書の引き方を決めます。辞書ファイルから、訓練文の各単語に対してカテゴリの候補を集めますが、その際に何をキーとして辞書を引くか、を設定します。'surfaceAndSecondWithConj' は、各単語について、(表層系（タイプ）, 品詞階層の二階層目, 活用)の三つ組みを使用して候補を引くことを表しています。他の設定は、'-help'で確認できます。

以上により、Super-taggerのモデルが、'tagger.out' に出力されます。このモデルを用い、Super-taggerの性能を評価する場合、以下を行います。

    > java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -modelType tagger -actionType evaluate -bankDirPath ccgbank-20130828 -numIters 10 -beta 0.1 -loadModelPath tagger.out -outputPath develop.tagged.txt

loadModelPathで、先ほど保存したモデルを指定しています。'-beta 0.1' は、カテゴリの候補を複数決める場合の閾値を設定するための変数です。各予測は、ロジスティック回帰の局所的な多値分類で行われますが、 "最も高い確率の予測*beta" まで候補を取り出します。'-outputPath' は、予測したカテゴリの候補をテキスト形式で出力します。

保存したTaggerのモデルを用いて、Parserの訓練を行います。

    > java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train -bankDirPath ccgbank-20130828 -saveModelPath parser.out -numIters 5 -loadModelPath tagger.out -beam 8 -beta 0.1

モデルはBeam searchに基づく大域的なStructured perceptronです。'-beam 8' は、beam幅を表します。現在素性は、Yue Zhangの論文のものの一部のみを用いています。素性は自分で設計して簡単に追加することができます。(TBD)

訓練したモデルを用いて、デベロップ用データで評価します。現在、Dependencyへの変換が行えないので、CCGBankに似た形式で、予測した木を出力しています。その際、カテゴリは、Parserが内部で保持する情報以外は失われています。例えば、"S1\S1"というカテゴリは、"S\S"と出力されます。また、導出した木に割り当てられたカテゴリの精度を出力します。

    > java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -modelType parser -actionType evaluate -loadModelPath parser.out -beam 8 -beta 0.1 -outputPath develop.parsed.txt -bankDirPath ccgbank-20130828

ここでも 'outputPath' で、最終的な出力のファイルを指定しています。


TODO
----

 - いじれる箇所とその方法の説明
 - 素性の設計
 - 結合するカテゴリ/品詞のペアが与えられたとき、headを決めるルールの実装