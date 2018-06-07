# Version checker

This is the version checker of the external software on jigg.

## Usage (local)

### Install the jar files

The first is the install of jar files. You run the following command:

```bash
./.checker/scripts/install-jar.sh
```

The four files are generated:
stanford-corenlp-x.x.x.jar, stanford-corenlp-x.x.x-models.jar, jigg-models.jar,
target/jigg-assembly-y.y.y.jar. x.x.x and y.y.y mean the version number of
the stanford-corenlp and the jigg, respectively.

### Python unit test

Next, execute the following command.

```bash
python -m unittest discover -s .checker/tests/
```

This run the unittest of the file name `test*.py` under the directory `.checker/tests/`.
For example in the [test_tokenize.py](./tests/test_tokenize.py)
, the annotation `tokenize` of jigg.pipeline is run.
When you input some text, this checks that the expected text and the result text are equal.


## How to Travis CI

Host of the repository can use Travis CI.
For details on registering with Travis CI, please see Travis CI
[Getting Stated](https://docs.travis-ci.com/user/getting-started/).