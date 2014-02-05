#!/bin/sh

HEAP=100g

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType tagger -actionType train -bankDirPath ccgbank-20130828 -saveModelPath tagger.out -numIters 15 -lookupMethod surfaceAndSecondWithConj -taggerTrainAlg adaGradL1 -lambda 0.000000005 -eta 0.1

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train -bankDirPath ccgbank-20130828 -saveModelPath parser.out -numIters 40 -loadModelPath tagger.out -beam 16 -beta 0.001

