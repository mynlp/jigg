#!/bin/bash

set -e

# text file
TEXT_DIR_NAME=".checker/jigg/text"
INPUT_FILE="input.txt"
OUTPUT_FILE="input.txt.xml"

# jar file
JIGG_JAR="target/jigg-assembly-${JIGG_VERSION}.jar"
JIGG_MODEL_JAR="jigg-models.jar"
CORENLP_MODEL_JAR="stanford-corenlp-${CORENLP_VERSION}-models.jar"

if [ ! -e ${JIGG_JAR} ]; then
    ./bin/sbt assembly
fi

if [ ! -e ${JIGG_MODEL_JAE} ]; then
    wget https://github.com/mynlp/jigg-models/raw/master/jigg-models.jar
fi

if [ ! -e ${CORENLP_MODEL_JAR} ]; then
    ./script/download_corenlp_models.sh
    DOWNLOAD_JAR="`pwd`/`ls -clt stanford*models*.jar | head -n 1 | gawk '{ print $9 }' `"
    mv ${DOWNLOAD_JAR} ${CORENLP_MODEL_JAR}
fi

JAR_FILE="${JIGG_JAR}:${JIGG_MODEL_JAE}:${CORENLP_MODEL_JAR}"

if [ ! -e "${TEXT_DIR_NAME}/${INPUT_FILE}" ]; then
    exit 1
fi

./.checker/jigg/scripts/tokenize.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/jigg/scripts/ssplit.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/jigg/scripts/pos.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/jigg/scripts/lemma.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/jigg/scripts/ner.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/jigg/scripts/parse.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
./.checker/jigg/scripts/dcoref.sh ${JAR_FILE} ${TEXT_DIR_NAME} ${INPUT_FILE}
