# PyJigg

PyJigg provides a Python wrapper to Jigg through Jigg's PipelineServer APIs.

### Install

``` shell
python setup.py install
```

### Usage

Before using it, the PipelineServer should be started in a command line.

``` shell
$ cd jigg/
$ java -Xmx4g -cp "*" jigg.pipeline.PipelineServer
```

This will start the server at `localhost:8080` in default. See

``` shell
$ java -Xmx4g -cp "*" jigg.pipeline.PipelineServer -help
```

for details.

To start annotations, first setup the Pipeline instance:

``` python
>>> from pyjigg import Pipeline
>>> pipeline = Pipeline('http://localhost:8080')
```

Then, use `annotate` with specific annotators.
The default output format is XML.

``` python
>>> annotation = pipeline.annotate(
  'This is the first sentence. This is the second sentence',
  properties = {
    'annotators': 'corenlp[tokenize,ssplit]'})
>>> print annotation
<Element 'root' at 0x103293810>
>>> import xml.etree.ElementTree as ET
>>> print(ET.tostring(annotation))
<root>
  <document id="d4">
    <sentences>
      <sentence characterOffsetBegin="0" characterOffsetEnd="27" id="s8">
        This is the first sentence.
        <tokens annotators="corenlp">
          <token characterOffsetBegin="0" characterOffsetEnd="4" form="This" id="t48" />
          <token characterOffsetBegin="5" characterOffsetEnd="7" form="is" id="t49" />
          <token characterOffsetBegin="8" characterOffsetEnd="11" form="the" id="t50" />
          <token characterOffsetBegin="12" characterOffsetEnd="17" form="first" id="t51" />
          <token characterOffsetBegin="18" characterOffsetEnd="26" form="sentence" id="t52" />
          <token characterOffsetBegin="26" characterOffsetEnd="27" form="." id="t53" />
        </tokens>
      </sentence>
      <sentence characterOffsetBegin="28" characterOffsetEnd="55" id="s9">
        This is the second sentence
        <tokens annotators="corenlp">
          <token characterOffsetBegin="0" characterOffsetEnd="4" form="This" id="t54" />
          <token characterOffsetBegin="5" characterOffsetEnd="7" form="is" id="t55" />
          <token characterOffsetBegin="8" characterOffsetEnd="11" form="the" id="t56" />
          <token characterOffsetBegin="12" characterOffsetEnd="18" form="second" id="t57" />
          <token characterOffsetBegin="19" characterOffsetEnd="27" form="sentence" id="t58" />
        </tokens>
      </sentence>
    </sentences>
  </document>
</root>
```

The annotation for the first input might be slow for setupping the pipeline by loading models, etc.
After that, if you pass the same same properties to the followed inputs, no model load will be required and the annotation should be fast.
But if you give different properties, the pipeline is updated, and the model load will be again required.

Note that some properties not specific to the pipeline itself, such as `outputFormat`, do not cause reconstruction of the pipeline.
For example, if you annotate the second input with different `outputFormat`, say `json`, the same pipeline is used and so the annotation is fast:

``` python
>>> annotation = pipeline.annotate(
  'This is the third sentence.',
  properties = {
    'annotators': 'corenlp[tokenize,ssplit]',
    'outputFormat': 'json'})
>>> import json
>>> print(json.dumps(annotation))
'{".tag": "root", ".child": [{".tag": "document", "id": "d5", ".child": [{".tag": "sentences", ".child": [{"text": "This is the third sentence.", "characterOffsetEnd": "27", ".tag": "sentence", ".child": [{".tag": "tokens", "annotators": "corenlp", ".child": [{"characterOffsetEnd": "4", ".tag": "token", "id": "t59", "form": "This", "characterOffsetBegin": "0"}, {"characterOffsetEnd": "7", ".tag": "token", "id": "t60", "form": "is", "characterOffsetBegin": "5"}, {"characterOffsetEnd": "11", ".tag": "token", "id": "t61", "form": "the", "characterOffsetBegin": "8"}, {"characterOffsetEnd": "17", ".tag": "token", "id": "t62", "form": "third", "characterOffsetBegin": "12"}, {"characterOffsetEnd": "26", ".tag": "token", "id": "t63", "form": "sentence", "characterOffsetBegin": "18"}, {"characterOffsetEnd": "27", ".tag": "token", "id": "t64", "form": ".", "characterOffsetBegin": "26"}]}], "characterOffsetBegin": "0", "id": "s10"}]}]}]}'
```

`pipeline_example.py` contains almost the same code.
