#!/bin/bash

set -e

source ./.checker/scripts/set-env.sh

## jigg
JIGG_JAR="target/jigg-assembly-${JIGG_VERSION}.jar"
JIGG_MODEL_JAR="jigg-models.jar"

# Make the jar file `target/jigg-assembly-0.7.2.jar`.
# At the same time, the stanford-corenlp is downloated.
./bin/sbt assembly

# download jigg models
wget "https://github.com/mynlp/jigg-models/raw/master/${JIGG_MODEL_JAR}"

## stanford-corenlp
CORENLP_MODEL_JAR="stanford-corenlp-${CORENLP_VERSION}-models.jar"

# download Stanford CoreNLP models
./script/download_corenlp_models.sh
DOWNLOAD_JAR="`pwd`/`ls -clt stanford*models*.jar | head -n 1 | gawk '{ print $9 }'`"
mv ${DOWNLOAD_JAR} ${CORENLP_MODEL_JAR}
