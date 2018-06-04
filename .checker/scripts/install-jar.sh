#!/bin/bash

set -e

source ./.checker/scripts/set-env.sh

## jigg
JIGG_JAR="target/jigg-assembly-${JIGG_VERSION}.jar"
JIGG_MODEL_JAR="jigg-models.jar"

# Make the jar file `target/jigg-assembly-0.7.2.jar`.
# At the same time, the stanford-corenlp is also downloated.
./bin/sbt assembly

# download jigg models
wget "https://github.com/mynlp/jigg-models/raw/master/${JIGG_MODEL_JAR}"

## stanford-corenlp
CORENLP_JAR="stanford-corenlp-${CORENLP_VERSION}.jar"
CORENLP_MODEL_JAR="stanford-corenlp-${CORENLP_VERSION}-models.jar"
IVY2_CORENLP_JAR="${IVY2_CACHE_DIR}/edu.stanford.nlp/stanford-corenlp/jars/${CORENLP_JAR}"

# make a symbolic link.
ln -s ${IVY2_CORENLP_JAR} ./

# download Stanford CoreNLP models
./script/download_corenlp_models.sh
DOWNLOAD_JAR="`pwd`/`ls -clt stanford*models*.jar | head -n 1 | gawk '{ print $9 }'`"
mv ${DOWNLOAD_JAR} ${CORENLP_MODEL_JAR}
