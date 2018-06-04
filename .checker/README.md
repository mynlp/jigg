# Version checker

This is the version checker of the external software on jigg.

## Usage (local)

The first is the install of jar files. You run the following command:

```bash
./.checker/scripts/install-jar.sh
```

The four files are generated:
stanford-corenlp-x.x.x.jar, stanford-corenlp-x.x.x-models.jar, jigg-models.jar,
target/jigg-assembly-y.y.y.jar. x.x.x and y.y.y mean the version number of
the stanford-corenlp and the jigg, respectively.

Next, execute the following command.

```bash
./.checker/scripts/run-test.sh
```

This script checks a difference between a target file and a output file.
The output file: The result when you run stanford-corenlp or jigg of the version written on `build.sbt`.
The target file: The correct answer of the output file.
When the version of stanford-corenlp is updated in `build.sbt`
and the result is different from the previous version, the script will output an error.

