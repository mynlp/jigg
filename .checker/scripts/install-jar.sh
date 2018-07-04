#!/bin/bash

set -e

source ./.checker/scripts/set-env.sh

jar_dir="jar/"

## stanford-corenlp
CORENLP_MODEL_JAR="stanford-corenlp-${CORENLP_VERSION}-models.jar"

# download Stanford CoreNLP models
./script/download_corenlp_models.sh
DOWNLOAD_JAR="`pwd`/`ls -clt stanford*models*.jar | head -n 1 | gawk '{ print $9 }'`"
mv ${DOWNLOAD_JAR} ${jar_dir}"/"${CORENLP_MODEL_JAR}
