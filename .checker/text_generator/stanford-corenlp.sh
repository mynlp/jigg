#!/bin/bash

CORENLP_JAR="stanford-corenlp-${CORENLP_VERSION}.jar"
CORENLP_MODEL_JAR="stanford-corenlp-${CORENLP_VERSION}-models.jar"

INPUT_FILE=".checker/stanford_corenlp/text/input.txt"
OUTPUT_DIR=".checker/stanford_corenlp/text"

if [ ! -e ${CORENLP_JAR} ]; then
    ln -s "${IVY2_CACHE_DIR}/edu.stanford.nlp/stanford-corenlp/jars/${CORENLP_JAR}" ./
fi

if [ ! -e ${CORENLP_MODEL_JAR} ]; then
    ./script/download_corenlp_models.sh
    DOWNLOAD_JAR="`pwd`/`ls -clt stanford*models*.jar | head -n 1 | gawk '{ print $9 }' `"
    mv ${DOWNLOAD_JAR} ${CORENLP_MODEL_JAR}
fi

if [ ! -e ${INPUT_FILE} ]; then
    echo ${INPUT_FILE}
    exit 1
fi

JAR_FILE="${CORENLP_JAR}:${CORENLP_MODEL_JAR}"

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
    java -cp ${JAR_FILE} edu.stanford.nlp.pipeline.StanfordCoreNLP \
	 -annotators ${Annotation[i]} -file ${INPUT_FILE}
    mv input.txt.out "${OUTPUT_DIR}/${t}-target.txt"
    i=`expr ${i} + 1`
done
