#!/bin/bash

set -e

home_dir=`pwd ./`
jar_dir="jar/"


# download stanford corenlp
url=http://nlp.stanford.edu/software/stanford-corenlp-full-2018-10-05.zip
zip=stanford-corenlp-full-2018-10-05.zip
dir=stanford-corenlp-full-2018-10-05
file=stanford-corenlp-3.9.2.jar
file_model=stanford-corenlp-3.9.2-models.jar

# download Stanford CoreNLP models
wget ${url}

# unpack
unzip ${zip}

cp ${dir}"/"${file} ${jar_dir}
cp ${dir}"/"${file_model} ${jar_dir}


# create jigg jar file
jigg_file="target/jigg-assembly-0.8.0.jar"
./bin/sbt assembly
cp ${jigg_file} ${jar_dir}


# download jigg-models
jigg_models="jigg-models.jar"
wget https://github.com/mynlp/jigg-models/raw/master/jigg-models.jar
mv ${jigg_models} ${jar_dir}
