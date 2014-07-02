#!/bin/sh

beta=$1
beam=$2
iters=$3

HEAP=50g

taggerModel=en.tagger.out
parserModel=en.parser.beta=$beta.beam=$beam.out
parseOutput=en.develop.pred.beta=$beta.beam=$beam

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType tagger \
    -actionType train -trainPath data/english.train.removed -language english \
    -saveModelPath $taggerModel -numIters 15 -unkThreathold 20

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train \
    -trainPath data/english.train.removed -language english \
    -saveModelPath $parserModel -numIters $3 \
    -loadModelPath $taggerModel -beam $beam -beta $beta \
    -parserFeaturePath en.features.parser.beta=$beta.beam=$beam.txt

java -Xmx8g -jar target/enju-ccg-assembly-0.1.jar -modelType parser -actionType evaluate \
    -loadModelPath $parserModel -beam $beam -beta $beta -outputPath $parseOutput \
    -language english -developPath data/english.devel

