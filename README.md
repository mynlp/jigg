# Jigg: NLPパイプラインのための簡易フレームワーク

Jigg は自然言語処理 (NLP) におけるパイプライン処理を簡単に行うためのフレームワークです。
現在は日本語を主にサポートしていますが、他の言語も扱うことができます。
以下のような特徴を持ちます。

- JVM 上で動作するため、 Jar ファイル一つをダウンロードするだけで使用可能。
- 既存の様々なソフトウェアを、 [Stanford CoreNLP](http://nlp.stanford.edu/software/corenlp.shtml) と似たインターフェースによってパイプライン上で組み合わせることができる。
- Scala もしくは Java により既存ソフトのラッパーなどを用意することで、パイプラインを拡張できる。数十行程度の実装で可能。
- 日本語 CCG パーザが利用可能。

## 使い方

### 準備

現在のバージョンは 0.4 です。
レポジトリ内の `jar/jigg-0.4.jar` をパスに通せば、必要な機能を全て使用することができます。

注意: Jigg は Java 1.7 以上のバージョンが必要です。実行に失敗する場合、 Java 1.7 をインストールしてみて下さい。
また環境によっては Java の文字コードを設定する必要があります。
必要であれば、事前に

```bash
$ export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
```

と実行するか、 `~/.bashrc` などに、これと同じコマンドを貼付けておいて下さい。
後者の場合、設定を読み込むためにターミナルの再起動が必要です。

以下の例では CCG パーザを使います。
CCG パーザは事前にモデルをダウンロードしておく必要があります。
以下のコマンドを実行しておいて下さい。

```bash
./script/download_ccg_model.sh
```

これで ./jar ディレクトリにモデルファイルがダウンロードされ、使えるようになります。

### 使用例

以下の例では、パイプで与えられた入力に、 ssplit (文分割)、 [kuromoji](http://www.atilika.org) (形態素解析)、 ccg (CCG 構文解析) を順番に適用し、結果が XML で得られます。

```bash
$ echo "東京は晴れです。 京都はどうですか。" | java -cp "./jar/*" jigg.pipeline.Pipeline -annotators ssplit,kuromoji,ccg > annotated.xml
```

これは正しく動作しますが、次のコマンドは失敗します。

```bash
$ echo "東京は晴れです。 京都はどうですか。" | java -cp "./jar/*" jigg.pipeline.Pipeline -annotators ssplit,ccg > annotated.xml
annotator ccg requires TokenizeWithIPA
  annotators                     < str>: List of annotator names, e.g., ssplit,mecab ssplit|kuromoji|mecab|cabocha|juman|knp|ccg (required) [ssplit,ccg]
```

これは、 CCG パーザが入力として、その手前までで形態素解析が行われていることを前提とするからです。
Jigg はこのように、コンポーネント (アノテータ) 間のパイプラインの接続がうまくいかない場合には警告を出して終了します。

基本的な使用法は、ヘルプメッセージを参照してください。

```bash
$ java -cp target/jigg-assembly-0.4.jar jigg.pipeline.Pipeline -help
Usage:
  annotators                     < str>: List of annotator names, e.g., ssplit,mecab ssplit|kuromoji|mecab|cabocha|juman|knp|ccg (required) []
  file                           < str>: Input file; if omitted, read from stdin []
  props                          < str>: Property file []
  customAnnotatorClass           < str>: You can add an abbreviation for a custom annotator class with "-customAnnotatorClass.xxx path.package" []
  output                         < str>: Output file; if omitted, `file`.xml is used. Gzipped if suffix is .gz []
  help                           < str>: Print this message and descriptions of specified annotators, e.g., -help ssplit,mecab [true]
```

`-help xxx` でアノテータ名を指定することで、その使用法や仕様などを確認することができます。

```bash
$ java -cp target/jigg-assembly-0.4.jar jigg.pipeline.Pipeline -help ssplit,kuromoji
...
ssplit:
  requires                             : []
  requirementsSatisfied                : [Sentence]
 
  ssplit.pattern                 < str>: Regular expression to segment lines (if omitted, specified method is used) []
  ssplit.method                  < str>: Use predefined segment pattern newLine|point|pointAndNewLine [pointAndNewLine]
 
kuromoji:
  requires                             : [Sentence]
  requirementsSatisfied                : [TokenizeWithIPA]
```

各アノテータには、 `requires` と `requirementsSatisfied` というフィールドが設定されています。
`ssplit` の `requirementsSatisfied` と、 `kuromoji` の `requires` はともに `Sentence` で共通となっています。
この `requires` は、ここに示した要素が、それまでのアノテータにより満たされてないといけないことを示します。
この場合、 `Sentence` が満たされたという条件が `ssplit` により与えられ、 `kuromoji` はその条件を前提とします。
従って、 `-annotators kuromoji` などとすると、 `Sentence` がそれまでに与えられていないため、先ほどの `ssplit,ccg` のときのように、実行に失敗します。

## 既存のアノテータ

現在次のようなアノテータが使用可能です。
それぞれのアノテータは、 `-x.option yyy` という形式で、オプションを指定できます。
詳細は `-help xxx` で確認してください。
また `mecab` などのアノテータを使うには、ソフトウェアがシステムにインストールされている必要があります。

  * `ssplit`: 文分割を行います。シンプルな正規表現により実装されており、どのようなパターンで分割を行うかを指定できます。デフォルトでは `。` もしくは改行により分割を行いますが、例えばすでに分割済みの入力を扱う場合、 `-ssplit.method newLine` とすれば、改行のみを文の区切りと見なして処理を行います。
  * `kuromoji`: インストールせずに使える形態素解析器です。
  * `mecab`: MeCab で形態素解析を行います。 `kuromoji` の代わりに用いることができます。
  * `cabocha`: `mecab` で形態素解析済みの文に、文節区切りと文節係り受けを付与します。
  * `juman`: Juman で形態素解析を行います。
  * `knp`: KNP の出力を XML に変換して得ます。
  * `ccg`: CCG の導出木を XML で出力します。各単語には CCG のカテゴリが付与され、それらがどのように繋がるかを辿ることができます。

## TODO

- 説明を追加（拡張方法など）
- 論文へのリンク
- スライドへのリンク
