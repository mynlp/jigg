#!/bin/sh

HEAP=100g

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train -trainPath ccgbank-20130828/devel.ccgbank -saveModelPath parser.out -numIters 10 -loadModelPath tagger.out -beam 16 -beta 0.001

java -Xmx${HEAP} -jar ./target/enju-ccg-assembly-0.1.jar -modelType parser -actionType train -bankDirPath ccgbank-20130828 -saveModelPath parser.out -numIters 40 -loadModelPath tagger.out -beam 16 -beta 0.001

