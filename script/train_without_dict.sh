#!/bin/sh

#############################################################
#
# Super-tagging dictionary is constructed from statistics
# extracted from trees of training corpus, not from existing
# lexical dictionary. 
# 
#############################################################

beta=$1
beam=$2
iters=$3

lookup=$4 # candidate are: surfaceOnly, surfaceAndSecondWithConj, etc

HEAP=50g

taggerModel=ja.tagger.nodict.out
parserModel=ja.parser.nodict.beta=$beta.beam=$beam.out
parseOutput=ja.nodict.develop.pred.beta=$beta.beam=$beam

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType tagger \
    -actionType train -bankDirPath ccgbank-20130828 -saveModelPath $taggerModel \
    -numIters 20 -lookupMethod $lookup -useLexiconFiles false -unkThreathold 30

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train \
    -bankDirPath ccgbank-20130828 -saveModelPath $parserModel -numIters $3 \
    -loadModelPath $taggerModel -beam $beam -beta $beta \
    -parserFeaturePath ja.features.parser.nodict.beta=$beta.beam=$beam.txt

java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -modelType parser -actionType evaluate \
    -loadModelPath $parserModel -beam $beam -beta $beta -outputPath $parseOutput \
    -bankDirPath ccgbank-20130828 -cabochaPath ./data/devel.removed

