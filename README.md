Shift-reduceに基づくCCG Parserです。
見通しをよくするために、大部分をScalaで書き直し中です。現在の予定では、機械学習の分類器のライブラリがモジュールとして分離しているため、これをJavaのまま残し、他の部分はScalaになる予定です。

ビルドシステムはsbtを使用しています。
http://www.scala-sbt.org
sbtの実行ファイルは添付していますので、特に用意は必要なくビルドできるかと思います。

プロジェクトのルートディレクトリで `bin/sbt` と実行すると、sbtのプロンプトが開きます。そのから、以下のようなコマンドで操作します。

    > compile  # main部分のソースのコンパイル
    > test     # 全てのテストを実行
    > assembly # 実行可能なjarをproject/ 以下に生成

タガー, パーザーなど全て、Driverと呼ばれるクラスを通して実行されます。このクラスのメイン関数は、assemblyで作られたjarファイルでデフォルトで呼ばれるので、今のコマンドで、全ての操作ができます。

    > java -Xmx8g -jar target/shift-reduce-enju-assembly-0.0.1.jar

現在コマンドラインの解析に、fig (https://github.com/percyliang/fig) を使用しています。有効なオプションは、`-help`をつけて

    > java -Xmx8g -jar target/shift-reduce-enju-assembly-0.0.1.jar -help

で一覧を見ることができます。figの他の機能で使われる無関係のオプションがいくつか表示されてしまうのですが、後ほど対応するかもしれません。たとえば、タガーの訓練を行い、モデルを保存する場合のコマンドは以下になります。

    > java -Xmx8g -jar target/shift-reduce-enju-assembly-0.0.1.jar -modelType tagger -actionType train -avmPath avm_settings.txt -bankDirPath ccgbank-20130828

ここで、`modelType`でタガーを、`actionType`で、訓練を行うことを選択していることになります。読み込みファイルは`bankDirPathde`を設定すれば、CCGBankのディレクトリ上のファイルを自動で読み込みます。現在スーパータガーの訓練とモデルの保存まで対応しているはずです。

