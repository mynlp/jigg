#!/bin/sh

beta=$1
beam=$2
iters=$3

HEAP=50g

taggerModel=ja.tagger.out
parserModel=ja.parser.beta=$beta.beam=$beam.out
parseOutput=ja.develop.pred.beta=$beta.beam=$beam

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType tagger \
    -actionType train -bankDirPath ccgbank-20130828 -saveModelPath $taggerModel \
    -numIters 15 -lookupMethod surfaceAndSecondWithConj

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train \
    -bankDirPath ccgbank-20130828 -saveModelPath $parserModel \
    -numIters $3 -loadModelPath $taggerModel -beam $beam -beta $beta \
    -parserFeaturePath ja.features.parser.beta=$beta.beam=$beam.txt

java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -modelType parser -actionType evaluate \
    -loadModelPath $parserModel -beam $beam -beta $beta -outputPath $parseOutput \
    -bankDirPath ccgbank-20130828 -cabochaPath ./data/devel.removed
