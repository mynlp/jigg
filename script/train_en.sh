#!/bin/sh

beta=$1
beam=$2
iters=$3

HEAP=100g

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType tagger -actionType train -trainPath data/english.train.removed -language english -saveModelPath en.tagger.out -numIters 15 -unkThreathold 20

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train -trainPath data/english.train.removed -language english -saveModelPath en.parser.out.beta=$beta.beam=$beam -numIters $3 -loadModelPath en.tagger.out -beam $beam -beta $beta -removeZero

