#!/bin/bash

jar_dir="jar"

# chinese model jar file
wget http://nlp.stanford.edu/software/stanford-chinese-corenlp-2018-10-05-models.jar
mv stanford-chinese-corenlp-2018-10-05-models.jar ${jar_dir}

# french model jar file
wget http://nlp.stanford.edu/software/stanford-french-corenlp-2018-10-05-models.jar
mv stanford-french-corenlp-2018-10-05-models.jar ${jar_dir}


