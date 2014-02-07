#!/bin/sh

beta=$1
beam=$2
iters=$3

HEAP=100g

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType tagger -actionType train -bankDirPath ccgbank-20130828 -saveModelPath tagger.out -numIters 15 -lookupMethod surfaceAndSecondWithConj -taggerTrainAlg adaGradL1 -lambda 0.000000005 -eta 0.1

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train -bankDirPath ccgbank-20130828 -saveModelPath parser.out.beta=$beta.beam=$beam -numIters $3 -loadModelPath tagger.out -beam $beam -beta $beta -removeZero

