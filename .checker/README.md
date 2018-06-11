# Version checker

This is the version checker of the external software on jigg.

## Usage (local)

### Install the jar files

The first is the installation of jar files. You run the following command:

```bash
./.checker/scripts/install-jar.sh
```

After the run, three files are generated:
stanford-corenlp-x.x.x-models.jar, jigg-models.jar and
target/jigg-assembly-y.y.y.jar. `x.x.x` and `y.y.y` mean the version number of
the stanford-corenlp and the jigg, respectively.

### Python unittest

Next, execute the following command.

```bash
python -m unittest discover -s .checker/tests/
```

This command runs the unittest of the file name `test*.py` under the directory `.checker/tests/`.
For example in the [test_tokenize.py](./tests/test_tokenize.py)
, the annotation `tokenize` of jigg.pipeline is run.
When you input some text (variable self.input_text in the file test_tokenize.py),
the run checks that expected text (variable self.expected_text in the file)
and result text are equal.

## How to add new test

Please, move the current direcotry `jigg/` to the `.checker/tests/`.  
```
cd .cheker/tests/
```

You need to rename and edit the `example_test.py` file according to test case.

1. Change the file name  
   In order to run the unittest, the file like the name 'test***.py must be in
   the `.checker/tests/` directory.
   ```
   cp exmaple_test.py test***.py
   ```
   
2. Change the class name  
   You open the copied file with any editor. Please, rename the class name `TestName(BaseTest)`
   `Test***(BaseTest)`. `***` is any name. 
   
3. Change four variables in the setUp() function with the class  
   You change the following variables.
   - `self.input_text` : a sample text using for test  
   - `self.expceted_text` : an expected output text by test run  
   - `self.exe` : an execution command  
     You change the term `-annotators` according to the test case.

4. Change the function name  
   For each the test case, you also change the function name form `test_name(self)` to
   `test_***(self)`. `***` is any name, for example, `tokenize`, `ssplite`, ... etc.
   Note that the head of the functino name must give the `test`.

Finally, you run the unittest `python -m unittest discover -s .checker/tests/`
in the root directory `jigg/`.

## Update

If you update the stanford-corenlp version in JIGG, you need to edit some file in `.checker`.

- [`.checker/scripts/set-env.sh`](./scripts/set-env.sh)  
  Please, change the value of `CORENLP_VERSION` with the stanford-corenlp verison in JIGG.
- [`.checker/tests/constant.py`](./tests/constant.py)  
  Please, change the value of `CORENLP_VERSION` with the stanford-corenlp version in JIGG.

## How to use Travis CI

Host user of the repository can use Travis CI.
For details on registering with Travis CI, please see Travis CI
[Getting Stated](https://docs.travis-ci.com/user/getting-started/).

When you push or pull request in github, 
Travis CI runs the process in [`.travis.yml`](../.travis.yml) file in the root directory. 
The main processes are `install` and `script`, 
and these are the same processes as [Usage (local)](#usage-local).

### Select the branches

To specify the branches that Travis CI is active, in [`.travis.yml`](../.travis.yml) file, 
```yaml
branches:
  only:
    - feature/ci
```
include/exculde the branch names.

### Skipping

If you don't want to run Travis CI, add `[ci skip]` or `[skip ci]` to the git commit message.

### Cron jobs

Travis CI cron jobs can run at regular scheduled intervals `daily`, `weekly` or `monthly`.
Configure cron jobs from the `Cron jobs` settings tab on your Travis CI page.
For details, please see [here](https://docs.travis-ci.com/user/cron-jobs/).
