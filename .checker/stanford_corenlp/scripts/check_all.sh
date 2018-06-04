#!/bin/bash

set -e

# the file for version check
TEXT_DIR_NAME=".checker/stanford_corenlp/text"
INPUT_FILE="input.txt"
OUTPUT_FILE="input.txt.out"

# jar file of stanford-corenlp
CORENLP_JAR="stanford-corenlp-${CORENLP_VERSION}.jar"
CORENLP_MODEL_JAR="stanford-corenlp-${CORENLP_VERSION}-models.jar"

if [ ! -e ${CORENLP_JAR} ]; then
    ln -s "${IVY2_CACHE_DIR}/edu.stanford.nlp/stanford-corenlp/jars/${CORENLP_JAR}" ./
fi

if [ ! -e ${CORENLP_MODEL_JAR} ]; then
    # download stanford-corenlp models.
    ./script/download_corenlp_models.sh
    # get the download file name.
    DOWNLOAD_JAR="`pwd`/`ls -clt stanford*models*.jar | head -n 1 | gawk '{ print $9 }' `"
    # change file name.
    mv ${DOWNLOAD_JAR} ${CORENLP_MODEL_JAR}
fi

JAR_FILE="${CORENLP_JAR}:${CORENLP_MODEL_JAR}"

./.checker/stanford_corenlp/scripts/tokenize.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/stanford_corenlp/scripts/ssplit.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/stanford_corenlp/scripts/pos.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/stanford_corenlp/scripts/lemma.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/stanford_corenlp/scripts/ner.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/stanford_corenlp/scripts/parse.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/stanford_corenlp/scripts/dcoref.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
