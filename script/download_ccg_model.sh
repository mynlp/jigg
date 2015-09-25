#!/bin/sh

jar=`dirname $0`/../jar

# modelurl="http://kmcs.nii.ac.jp/~noji/transccg/models/ccg-models-0.4.jar"
modelurl="http://kmcs.nii.ac.jp/~noji/transccg/models/ccg-models-without-lexicon-0.4.jar" # If you want to try the model trained without manually constructed lexicon, please download this file and replace it with ccg-models-0.4.jar.

wget $modelurl -P $jar
