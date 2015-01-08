#!/bin/sh

jar=`dirname $0`/../jar

modelurl="http://kmcs.nii.ac.jp/~noji/transccg/models/ccg-models-0.3.jar"

wget $modelurl -P $jar
