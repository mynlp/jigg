#!/bin/bash

JIGG_JAR="target/jigg-assembly-${JIGG_VERSION}.jar"
JIGG_MODEL_JAR="jigg-models.jar"
CORENLP_MODEL_JAR="stanford-corenlp-${CORENLP_VERSION}-models.jar"

INPUT_FILE=".checker/jigg/text/input.txt"
OUTPUT_DIR=".checker/jigg/text"

if [ ! -e ${JIGG_JAR} ]; then
    ./bin/sbt assembly
fi

if [ ! -e ${JIGG_MODEL_JAR} ]; then
    wget https://github.com/mynlp/jigg-models/raw/master/jigg-models.jar
fi

if [ ! -e ${CORENLP_MODEL_JAR} ]; then
    ./script/download_corenlp_models.sh
    DOWNLOAD_JAR="`pwd`/`ls -clt stanford*models*.jar | head -n 1 | gawk '{ print $9 }' `"
    mv ${DOWNLOAD_JAR} ${CORENLP_MODEL_JAR}
fi

JAR_FILE="${JIGG_JAR}:${JIGG_MODEL_JAR}:${CORENLP_MODEL_JAR}"

Type=(tokenize ssplit pos lemma ner parse dcoref)
Annotation=(tokenize \
		tokenize,ssplit \
		tokenize,ssplit,pos \
		tokenize,ssplit,pos,lemma \
		tokenize,ssplit,pos,lemma,ner \
		tokenize,ssplit,parse \
		tokenize,ssplit,pos,lemma,ner,parse,dcoref)

i=0
for t in ${Type[@]}; do
    echo ${i} ${t} ${Annotation[i]}
    java -cp ${JAR_FILE} jigg.pipeline.Pipeline \
	 -annotators "corenlp[${Annotation[i]}]" \
	 -file ${INPUT_FILE}
    mv "${INPUT_FILE}.xml" "${OUTPUT_DIR}/${t}-target.xml"
    i=`expr ${i} + 1`
done
