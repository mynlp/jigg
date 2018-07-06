#!/bin/bash

set -e

source ./.checker/scripts/set-env.sh

home_dir=`pwd ./`
jar_dir="jar/"


# download stanford corenlp
url=http://nlp.stanford.edu/software/stanford-corenlp-full-2018-02-27.zip
zip=stanford-corenlp-full-2018-02-27.zip
dir=stanford-corenlp-full-2018-02-27
file=stanford-corenlp-3.9.1.jar
file_model=stanford-corenlp-3.9.1-models.jar

# download Stanford CoreNLP models
wget ${url}

# unpack
unzip ${zip}

cp ${dir}"/"${file} ${jar_dir}
cp ${dir}"/"${file_model} ${jar_dir}


# create jigg jar file
cd ${home_dir}
jigg_file="target/jigg-assembly-0.8.0.jar"
./bin/sbt assembly
cp ${jigg_file} ${jar_dir}
